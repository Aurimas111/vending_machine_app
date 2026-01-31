package vendingmachine;

import com.bloxbean.cardano.client.account.Account;
import com.bloxbean.cardano.client.common.model.Network;
import com.bloxbean.cardano.client.common.model.Networks;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import vendingmachine.model.NftMetadata;
import vendingmachine.services.ReadMetadataService;
import com.bloxbean.cardano.client.crypto.Keys;
import com.bloxbean.cardano.client.crypto.VerificationKey;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.bloxbean.cardano.client.transaction.spec.Policy;
import com.bloxbean.cardano.client.transaction.spec.script.RequireTimeBefore;
import com.bloxbean.cardano.client.transaction.spec.script.ScriptAll;
import com.bloxbean.cardano.client.transaction.spec.script.ScriptPubkey;
import io.blockfrost.sdk.api.exception.APIException;
import org.json.simple.parser.ParseException;
import vendingmachine.utils.DbOperations;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;

@Component
public class Start implements CommandLineRunner {
    private static final long SLOTS_PER_EPOCH = 5 * 24 * 60 * 60;
    private final DbOperations dbOperations;
    private final ReadMetadataService readMetadataService;

    @Value("${recovery.phrase}")
    private String recoveryPhrase;
    @Value("${network.id}")
    private int networkId;
    @Value("${network.magic}")
    private long networkMagic;

    public Start(DbOperations dbOperations, ReadMetadataService readMetadataService) {
        this.dbOperations = dbOperations;
        this.readMetadataService = readMetadataService;
    }

    @Override
    public void run(String[] args) throws IOException, ParseException, SQLException, CborSerializationException, APIException {

        // Just to upload images to ipfs storage and save ipfs hashes to database
/*
        Account sender = new Account(new Network(networkId, networkMagic), recoveryPhrase);
        String mintingAddress = sender.baseAddress();

        System.out.println("Wallet address: " + mintingAddress);

        Scanner myObj = new Scanner(System.in);

        Policy policy = dbOperations.getPolicyByWallet(mintingAddress);
        ArrayList<NftMetadata> metadataList;
        metadataList = dbOperations.readWalletAssociatedMetadata(policy.getPolicyId()); //check if metadata is already saved in db
        if (metadataList.isEmpty()) {
            System.out.println("Enter the directory of metadata.json file: ");
            String metadataDirectory = myObj.nextLine(); // folder in path must contain metadata.json

            metadataList = readMetadataService.read(metadataDirectory);
            System.out.println(metadataList.get(0));

            ArrayList<String> ipfsUrl = readMetadataService.uploadImages(metadataList);
            for (int i = 0; i < metadataList.size(); i++)
                metadataList.get(i).setIpfsHash(ipfsUrl.get(i));

            //Collections.shuffle(metadataList); // shuffle to not mint all NFTs in a row

            dbOperations.saveMetadata(metadataList, policy.getPolicyId());
        }*/
    }

    public static Policy createEpochPolicy(String name, long currentSlot, long epochs, Keys keys) {
        VerificationKey verificationKey = keys.getVkey();
        ScriptPubkey scriptPubkey = ScriptPubkey.create(verificationKey);
        RequireTimeBefore requireTimeBefore = new RequireTimeBefore(currentSlot + SLOTS_PER_EPOCH * epochs);
        ScriptAll scriptAll = new ScriptAll().addScript(requireTimeBefore).addScript(scriptPubkey);
        return new Policy(name, scriptAll).addKey(keys.getSkey());
    }
}