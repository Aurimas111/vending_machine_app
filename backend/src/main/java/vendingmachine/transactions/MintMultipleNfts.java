package vendingmachine.transactions;

import com.bloxbean.cardano.client.backend.api.BackendService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import vendingmachine.model.*;
import vendingmachine.repository.NftMetadataRepository;
import vendingmachine.repository.TransactionDataRepository;
import vendingmachine.services.StatusPublisher;

import vendingmachine.services.ReadTransactionsService;
import com.bloxbean.cardano.client.account.Account;
import com.bloxbean.cardano.client.api.exception.ApiException;
import com.bloxbean.cardano.client.api.model.Result;
import com.bloxbean.cardano.client.cip.cip25.NFT;
import com.bloxbean.cardano.client.cip.cip25.NFTMetadata;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.bloxbean.cardano.client.function.helper.SignerProviders;
import com.bloxbean.cardano.client.quicktx.QuickTxBuilder;
import com.bloxbean.cardano.client.quicktx.Tx;
import com.bloxbean.cardano.client.transaction.spec.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class MintMultipleNfts {
    private static final Logger log = LoggerFactory.getLogger(MintMultipleNfts.class);
    private final ReadTransactionsService readTransactionsService;
    private final TransactionDataRepository transactionDataRepository;
    private final NftMetadataRepository nftMetadataRepository;
    private final BackendService backendService;
    private final StatusPublisher statusPublisher;

    public MintMultipleNfts(ReadTransactionsService readTransactionsService, TransactionDataRepository transactionDataRepository, NftMetadataRepository nftMetadataRepository, BackendService backendService, StatusPublisher statusPublisher) {
        this.readTransactionsService = readTransactionsService;
        this.transactionDataRepository = transactionDataRepository;
        this.nftMetadataRepository = nftMetadataRepository;
        this.backendService = backendService;
        this.statusPublisher = statusPublisher;
    }

    // This method queues the minting of NFTs based on the provided parameters
    // checks the number of NFTs already minted and continues minting until the limit is reached or stopped externally
    // handles the minting process in batches defined by mintLimitPerTx
    public void queue(Account sender, Policy policy, String slot, int mintLimitPerTx, int amountOfNftsNotToMint, AtomicBoolean stopFlag, String ownerWalletAddress) throws SQLException, CborSerializationException, InterruptedException, ApiException {

        ArrayList<TransactionData> mintTransactions = new ArrayList<>();
        ArrayList<NftMetadata> metadataNotMinted = new ArrayList<>();
        ArrayList<String> txHashNotMinted = new ArrayList<>();
        ArrayList<String> notMintedAddress = new ArrayList<>();
        ArrayList<Integer> amountsToMint = new ArrayList<>();

        int collectionSize = nftMetadataRepository.getCollectionSizeByPolicyId(policy.getPolicyId());

        Integer mintCounter = transactionDataRepository.getMintedNftCountByPolicyId(policy.getPolicyId());
        mintCounter = (mintCounter == null) ? 0 : mintCounter;
        log.info("Minted NFT count: {}", mintCounter);

        if (mintCounter <= collectionSize - amountOfNftsNotToMint) {
            while (!stopFlag.get()) {
                log.info("trying to mint {} NFTs ", mintLimitPerTx);
                statusPublisher.sendMintStatus(ownerWalletAddress, new MintStatusMessage(policy.getPolicyId(), MintStatus.STARTED, "", "Trying to mint " + mintLimitPerTx + " NFTs"));
                mintTransactions.clear();
                notMintedAddress.clear();
                txHashNotMinted.clear();
                amountsToMint.clear();
                metadataNotMinted.clear();

                mintTransactions = transactionDataRepository.findAllByPolicyIdForMinting(policy.getPolicyId());

                log.info("{} transactions received for minting", mintTransactions.size());

                if (mintTransactions.isEmpty()) {
                    log.info("No transactions received, waiting for 5 seconds");
                    TimeUnit.SECONDS.sleep(5);
                    continue;
                }

                metadataNotMinted = nftMetadataRepository.findAllNotMintedByPolicyId(policy.getPolicyId());

                if (metadataNotMinted.size() < mintLimitPerTx + amountOfNftsNotToMint) {
                    log.info("Not enough metadata available to mint {} NFTs", mintLimitPerTx);
                    stopFlag.set(true);
                    return;
                }

                for (int i = 0; i < mintTransactions.size(); i++) {

                    // save hashes and addresses in different variables
                    txHashNotMinted.add(mintTransactions.get(i).getTxHash());
                    amountsToMint.add(mintTransactions.get(i).getAmountToMint());
                    for (int j = 0; j < mintTransactions.get(i).getAmountToMint(); j++) {
                        if (notMintedAddress.size() == mintLimitPerTx) {
                            break;
                        } else {
                            notMintedAddress.add(mintTransactions.get(i).getSenderAddress());
                        }
                    }
                }

                log.info("Amounts to mint: {}", amountsToMint);

                // loop until we have enough addresses
                if (notMintedAddress.size() == mintLimitPerTx) {
                    mintNfts(policy, metadataNotMinted, slot, sender, notMintedAddress, txHashNotMinted, amountsToMint, mintLimitPerTx, ownerWalletAddress, mintTransactions);

                    log.info("Sleeping for 10 seconds before next minting batch");
                    TimeUnit.SECONDS.sleep(10);
                }
            }

        } else {
            log.info("All NFTs have been minted for policyId: {}", policy.getPolicyId());
            stopFlag.set(true);
        }
    }

    // This method mints NFTs based on the provided policy, metadata, slot, sender account, receivers, transaction hashes, and amounts to mint
    // creates a transaction for minting the NFTs and attaches the metadata
    private void mintNfts(Policy policy, ArrayList<NftMetadata> metadataNotMinted, String slot, Account sender, ArrayList<String> receivers, ArrayList<String> txHashes, ArrayList<Integer> amountToMint, int mintLimitPerTx, String ownerWalletAddress, ArrayList<TransactionData> mintTransactions) throws CborSerializationException, ApiException, InterruptedException {
        ArrayList<Asset> assets = new ArrayList<>();

        if (receivers.size() >= mintLimitPerTx) {
            NFTMetadata nftMetadata = NFTMetadata.create();

            for (int i = 0; i < receivers.size(); i++) {
                // for on chain nft images
                /*String encodedImage ="data:image/png;base64," + encodeFileToBase64Binary(metadataNotMinted.get(0).getFile_url());
                String cc = encodedImage.replaceAll("(.{64})", "$0\n");
                String[] base = cc.split("\\s+");*/

                Asset asset = new Asset(metadataNotMinted.get(i).getName(), BigInteger.valueOf(1));
                assets.add(asset);

                NFT nft = NFT.create()
                        .assetName(asset.getName())
                        .name(asset.getName())
                        .image(metadataNotMinted.get(i).getImage())
                        .mediaType("image/png");
                //.setImages(List.of(base)) // for on chain nft images

                // add all dynamic attributes as properties
                for (Map.Entry<String, String> entry : metadataNotMinted.get(i).getDynamicAttributes().entrySet()) {
                    nft.property(entry.getKey(), entry.getValue());
                }

                nftMetadata.addNFT(policy.getPolicyId(), nft);
            }

            long ttl = Long.parseLong(slot);

            Tx tx = new Tx();
            for (int i = 0; i < assets.size(); i++) {
                tx.mintAssets(policy.getPolicyScript(), assets.get(i), receivers.get(i));
            }

            tx.attachMetadata(nftMetadata).from(sender.baseAddress());

            Result<String> result = new QuickTxBuilder(backendService)
                    .compose(tx)
                    .withSigner(SignerProviders.signerFrom(sender))
                    .withSigner(SignerProviders.signerFrom(policy))
                    .validTo(ttl)  // policy is going to lock at the saved slot
                    .completeAndWait(log::info);

            if (result.isSuccessful()) {
                log.info("Transaction Id: {}", result.getValue());
                statusPublisher.sendMintStatus(ownerWalletAddress, new MintStatusMessage(
                        policy.getPolicyId(),
                        MintStatus.MINTED,
                        result.getValue(),
                        "Transaction confirmed on Cardano network"
                ));

                // update minted amount and amount to mint in db
                int mintedSoFar = 0;
                for (int i = 0; i < txHashes.size(); i++) {
                    int requested = amountToMint.get(i);
                    int remaining = receivers.size() - mintedSoFar;

                    int toMint = Math.min(remaining, requested);
                    transactionDataRepository.updateAmountToMintByTxHash(txHashes.get(i), requested - toMint);
                    transactionDataRepository.updateAmountMintedByTxHash(txHashes.get(i), toMint + mintTransactions.get(i).getAmountMinted());

                    mintedSoFar += toMint;
                    if (mintedSoFar >= receivers.size()) break;
                }

                TimeUnit.SECONDS.sleep(2); // wait for the transaction to be processed before fetching the block time
                long blockTime = readTransactionsService.getTxDate(result.getValue());
                // Mark all NFTs as minted in the database and save tx hash with block time for frontend display
                for (int i = 0; i < receivers.size(); i++) {
                    nftMetadataRepository.updateMetadataMinted(result.getValue(), blockTime, receivers.get(i), policy.getPolicyId(), metadataNotMinted.get(i).getName());
                }
            } else {
                log.info("Transaction failed: {}", result);
                statusPublisher.sendMintStatus(ownerWalletAddress, new MintStatusMessage(
                        policy.getPolicyId(),
                        MintStatus.FAILED,
                        "",
                        "Transaction failed on Cardano network: " + result
                ));
            }

        } else {
            log.info("Not enough receivers to make a transaction for {} NFTs", receivers.size());
        }
    }

    private String encodeFileToBase64Binary(String imageUrl) {
        File file = new File(imageUrl);
        String encodedfile = null;
        try {
            FileInputStream fileInputStreamReader = new FileInputStream(file);
            byte[] bytes = new byte[(int) file.length()];
            fileInputStreamReader.read(bytes);
            encodedfile = new String(Base64.getEncoder().encode(bytes), "UTF-8");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return encodedfile;
    }
}