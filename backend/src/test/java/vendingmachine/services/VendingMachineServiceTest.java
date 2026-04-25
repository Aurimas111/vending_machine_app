package vendingmachine.services;

import com.bloxbean.cardano.client.transaction.spec.Policy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import vendingmachine.model.PolicyObject;
import vendingmachine.model.Session;
import vendingmachine.model.UserConfig;
import vendingmachine.repository.PolicyRepository;
import vendingmachine.repository.UserConfigRepository;
import vendingmachine.transactions.MintMultipleNfts;
import vendingmachine.transactions.Refunds;
import vendingmachine.utils.DbOperations;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VendingMachineServiceTest {

    @Mock
    private MintMultipleNfts mintMultipleNfts;
    @Mock
    private PolicyRepository policyRepository;
    @Mock
    private UserConfigRepository userConfigRepository;
    @Mock
    private DbOperations dbOperations;
    @Mock
    private Refunds refunds;
    @Mock
    private StatusPublisher statusPublisher;
    @Mock
    private Policy policy;
    private VendingMachineService vendingMachineService;

    private static final String TEST_ADDRESS = "addr_test1qz2fxv2umyhttkxyxp8x0dlpdt3k6cwng5pxj3jhsydzer3jcu5d8ps7zex2k2xt3uqxgjqnnj83ws8lhrn648jjxtwq2ytjqp";
    private static final String TEST_RECOVERY_PHRASE = "abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon about";
    private static final int TEST_NETWORK_ID = 0;
    private static final long TEST_NETWORK_MAGIC = 764824073L;

    @BeforeEach
    void setUp() throws Exception {
        vendingMachineService = new VendingMachineService(mintMultipleNfts, policyRepository, userConfigRepository, dbOperations, refunds, statusPublisher);

        ReflectionTestUtils.setField(vendingMachineService, "recoveryPhrase", TEST_RECOVERY_PHRASE);
        ReflectionTestUtils.setField(vendingMachineService, "networkId", TEST_NETWORK_ID);
        ReflectionTestUtils.setField(vendingMachineService, "networkMagic", TEST_NETWORK_MAGIC);

        clearActiveSessions();
    }

    private void clearActiveSessions() throws Exception {
        Field field = VendingMachineService.class.getDeclaredField("activeSessions");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, Session> activeSessions = (Map<String, Session>) field.get(null);
        activeSessions.clear();
    }

    private Map<String, Session> getActiveSessions() throws Exception {
        Field field = VendingMachineService.class.getDeclaredField("activeSessions");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, Session> activeSessions = (Map<String, Session>) field.get(null);
        return activeSessions;
    }

    @Test
    void startMinting_ShouldCreateNewSession() throws Exception {
        UserConfig userConfig = createTestUserConfig();
        when(dbOperations.getPolicyByWallet(TEST_ADDRESS)).thenReturn(policy);
        when(userConfigRepository.findByOwnerWalletAddress(TEST_ADDRESS)).thenReturn(userConfig);
        when(policy.getPolicyId()).thenReturn("testPolicyId");

        vendingMachineService.startMinting(TEST_ADDRESS);

        TimeUnit.MILLISECONDS.sleep(100);

        Map<String, Session> activeSessions = getActiveSessions();
        assertTrue(activeSessions.containsKey(TEST_ADDRESS));
    }

    @Test
    void startMinting_ShouldNotCreateDuplicateSession() throws Exception {
        UserConfig userConfig = createTestUserConfig();
        when(dbOperations.getPolicyByWallet(TEST_ADDRESS)).thenReturn(policy);
        when(userConfigRepository.findByOwnerWalletAddress(TEST_ADDRESS)).thenReturn(userConfig);
        when(policy.getPolicyId()).thenReturn("testPolicyId");

        vendingMachineService.startMinting(TEST_ADDRESS);
        TimeUnit.MILLISECONDS.sleep(100);

        vendingMachineService.startMinting(TEST_ADDRESS);
        TimeUnit.MILLISECONDS.sleep(100);

        Map<String, Session> activeSessions = getActiveSessions();
        assertEquals(1, activeSessions.size());
    }

    @Test
    void startMinting_ShouldAllowDifferentUsersSessions() throws Exception {
        String address2 = TEST_ADDRESS + "2";
        UserConfig userConfig1 = createTestUserConfig();
        UserConfig userConfig2 = createTestUserConfig();
        userConfig2.setOwnerWalletAddress(address2);

        when(dbOperations.getPolicyByWallet(TEST_ADDRESS)).thenReturn(policy);
        when(dbOperations.getPolicyByWallet(address2)).thenReturn(policy);
        when(userConfigRepository.findByOwnerWalletAddress(TEST_ADDRESS)).thenReturn(userConfig1);
        when(userConfigRepository.findByOwnerWalletAddress(address2)).thenReturn(userConfig2);
        when(policy.getPolicyId()).thenReturn("testPolicyId");

        vendingMachineService.startMinting(TEST_ADDRESS);
        vendingMachineService.startMinting(address2);

        TimeUnit.MILLISECONDS.sleep(100);

        Map<String, Session> activeSessions = getActiveSessions();
        assertEquals(2, activeSessions.size());
        assertTrue(activeSessions.containsKey(TEST_ADDRESS));
        assertTrue(activeSessions.containsKey(address2));
    }

    @Test
    void stopMinting_ShouldStopActiveSession() throws Exception {
        UserConfig userConfig = createTestUserConfig();
        PolicyObject policyObject = createTestPolicyObject();

        when(dbOperations.getPolicyByWallet(TEST_ADDRESS)).thenReturn(policy);
        when(userConfigRepository.findByOwnerWalletAddress(TEST_ADDRESS)).thenReturn(userConfig);
        when(policyRepository.findByOwnerWallet(TEST_ADDRESS)).thenReturn(policyObject);
        when(policy.getPolicyId()).thenReturn("testPolicyId");

        vendingMachineService.startMinting(TEST_ADDRESS);
        TimeUnit.MILLISECONDS.sleep(100);

        Map<String, Session> activeSessions = getActiveSessions();
        Session session = activeSessions.get(TEST_ADDRESS);
        assertNotNull(session);
        assertFalse(session.isStopped());

        vendingMachineService.stopMinting(TEST_ADDRESS);

        assertTrue(session.isStopped());
    }

    @Test
    void stopMinting_ShouldHandleNoActiveSession() {
        PolicyObject policyObject = createTestPolicyObject();
        UserConfig userConfig = createTestUserConfig();

        when(policyRepository.findByOwnerWallet(TEST_ADDRESS)).thenReturn(policyObject);
        when(userConfigRepository.findByOwnerWalletAddress(TEST_ADDRESS)).thenReturn(userConfig);

        assertDoesNotThrow(() -> vendingMachineService.stopMinting(TEST_ADDRESS));
    }

    @Test
    void startRefunding_ShouldCreateNewSession() throws Exception {
        UserConfig userConfig = createTestUserConfig();
        when(dbOperations.getPolicyByWallet(TEST_ADDRESS)).thenReturn(policy);
        when(userConfigRepository.findByOwnerWalletAddress(TEST_ADDRESS)).thenReturn(userConfig);
        when(policy.getPolicyId()).thenReturn("testPolicyId");

        vendingMachineService.startRefunding(TEST_ADDRESS);

        TimeUnit.MILLISECONDS.sleep(100);

        Map<String, Session> activeSessions = getActiveSessions();
        assertTrue(activeSessions.containsKey(TEST_ADDRESS));
    }

    @Test
    void startRefunding_ShouldNotCreateDuplicateSession() throws Exception {
        UserConfig userConfig = createTestUserConfig();
        when(dbOperations.getPolicyByWallet(TEST_ADDRESS)).thenReturn(policy);
        when(userConfigRepository.findByOwnerWalletAddress(TEST_ADDRESS)).thenReturn(userConfig);
        when(policy.getPolicyId()).thenReturn("testPolicyId");

        vendingMachineService.startRefunding(TEST_ADDRESS);
        TimeUnit.MILLISECONDS.sleep(100);

        vendingMachineService.startRefunding(TEST_ADDRESS);
        TimeUnit.MILLISECONDS.sleep(100);

        Map<String, Session> activeSessions = getActiveSessions();
        assertEquals(1, activeSessions.size());
    }

    @Test
    void stopRefunding_ShouldStopActiveSession() throws Exception {
        UserConfig userConfig = createTestUserConfig();
        PolicyObject policyObject = createTestPolicyObject();

        when(dbOperations.getPolicyByWallet(TEST_ADDRESS)).thenReturn(policy);
        when(userConfigRepository.findByOwnerWalletAddress(TEST_ADDRESS)).thenReturn(userConfig);
        when(policyRepository.findByOwnerWallet(TEST_ADDRESS)).thenReturn(policyObject);
        when(policy.getPolicyId()).thenReturn("testPolicyId");

        vendingMachineService.startRefunding(TEST_ADDRESS);
        TimeUnit.MILLISECONDS.sleep(100);

        Map<String, Session> activeSessions = getActiveSessions();
        Session session = activeSessions.get(TEST_ADDRESS);
        assertNotNull(session);
        assertFalse(session.isStopped());

        vendingMachineService.stopRefunding(TEST_ADDRESS);

        assertTrue(session.isStopped());
    }

    @Test
    void stopRefunding_ShouldHandleNoActiveSession() {
        PolicyObject policyObject = createTestPolicyObject();
        UserConfig userConfig = createTestUserConfig();

        when(policyRepository.findByOwnerWallet(TEST_ADDRESS)).thenReturn(policyObject);
        when(userConfigRepository.findByOwnerWalletAddress(TEST_ADDRESS)).thenReturn(userConfig);

        assertDoesNotThrow(() -> vendingMachineService.stopRefunding(TEST_ADDRESS));
    }

    @Test
    void startMinting_ShouldPublishStartedStatus() throws Exception {
        UserConfig userConfig = createTestUserConfig();
        when(dbOperations.getPolicyByWallet(TEST_ADDRESS)).thenReturn(policy);
        when(userConfigRepository.findByOwnerWalletAddress(TEST_ADDRESS)).thenReturn(userConfig);
        when(policy.getPolicyId()).thenReturn("testPolicyId");

        vendingMachineService.startMinting(TEST_ADDRESS);

        TimeUnit.MILLISECONDS.sleep(200);

        verify(statusPublisher, atLeastOnce()).sendMintStatus(eq(TEST_ADDRESS), any());
    }

    @Test
    void startRefunding_ShouldPublishInitiatedStatus() throws Exception {
        UserConfig userConfig = createTestUserConfig();
        when(dbOperations.getPolicyByWallet(TEST_ADDRESS)).thenReturn(policy);
        when(userConfigRepository.findByOwnerWalletAddress(TEST_ADDRESS)).thenReturn(userConfig);
        when(policy.getPolicyId()).thenReturn("testPolicyId");

        vendingMachineService.startRefunding(TEST_ADDRESS);

        TimeUnit.MILLISECONDS.sleep(200);

        verify(statusPublisher, atLeastOnce()).sendRefundStatus(eq(TEST_ADDRESS), any());
    }

    private UserConfig createTestUserConfig() {
        UserConfig config = new UserConfig();
        config.setOwnerWalletAddress(TEST_ADDRESS);
        config.setPolicySlot("12345");
        config.setNftsToMintPerTx(5);
        config.setAmountOfNFTsNotToMint(0);
        config.setRefundsPerTxLimit(3);
        return config;
    }

    private PolicyObject createTestPolicyObject() {
        PolicyObject policyObject = new PolicyObject();
        policyObject.setPolicyId("testPolicyId");
        policyObject.setOwnerWallet(TEST_ADDRESS);
        return policyObject;
    }
}
