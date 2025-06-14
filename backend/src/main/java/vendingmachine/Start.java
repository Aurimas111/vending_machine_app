package vendingmachine;

import vendingmachine.model.NftMetadata;
import vendingmachine.utils.Base;
import vendingmachine.utils.Constant;
import vendingmachine.utils.DbOperations;
import vendingmachine.utils.ReadMetadata;
import com.bloxbean.cardano.client.account.Account;
import com.bloxbean.cardano.client.api.exception.ApiException;
import com.bloxbean.cardano.client.common.model.Networks;
import com.bloxbean.cardano.client.crypto.Keys;
import com.bloxbean.cardano.client.crypto.VerificationKey;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.bloxbean.cardano.client.transaction.spec.Policy;
import com.bloxbean.cardano.client.transaction.spec.script.RequireTimeBefore;
import com.bloxbean.cardano.client.transaction.spec.script.ScriptAll;
import com.bloxbean.cardano.client.transaction.spec.script.ScriptPubkey;
import io.blockfrost.sdk.api.exception.APIException;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;


public class Start extends Base {
    private static final long SLOTS_PER_EPOCH = 5 * 24 * 60 * 60;

    public static void main(String[] args) throws IOException, ParseException, SQLException, CborSerializationException, ApiException, APIException {

        /*
        Just to upload images to ipfs storage and save ipfs hashes to database.
         */

        new Start(); // to initialize data in base class

        Account sender = new Account(Networks.preprod(), Constant.RECOVERY_PHRASE);
        String mintingAddress = sender.baseAddress();

        System.out.println("Wallet address: " + mintingAddress);

        Scanner myObj = new Scanner(System.in);


        Policy policy = DbOperations.getPolicyByWallet(mintingAddress);
        ArrayList<NftMetadata> metadataList;
        metadataList = DbOperations.readWalletAssociatedMetadata(policy.getPolicyId()); //check if metadata is already saved in db
        if(metadataList.isEmpty()){
            System.out.println("Enter the directory of metadata.json file: ");
            String metadataDirectory = myObj.nextLine(); // folder in path must contain metadata.json
            //C:/Users/aurim/OneDrive/Desktop/nft projektai/nft stuff/create-10k-nft-collection-2/build/json/_metadata.json

            metadataList = ReadMetadata.read(metadataDirectory);

            System.out.println(metadataList.get(0));

            ArrayList<String> ipfsUrl = ReadMetadata.uploadImages(metadataList);
            for(int i = 0;i< metadataList.size();i++)
                metadataList.get(i).setIpfsHash(ipfsUrl.get(i));

            //Collections.shuffle(metadataList); // shuffle to not mint all NFTs in a row

            DbOperations.saveMetadata(metadataList, policy.getPolicyId());

        }
    }
    public static Policy createEpochPolicy(String name, long currentSlot, long epochs, Keys keys) {
        VerificationKey verificationKey = keys.getVkey();
        ScriptPubkey scriptPubkey = ScriptPubkey.create(verificationKey);
        RequireTimeBefore requireTimeBefore = new RequireTimeBefore(currentSlot + SLOTS_PER_EPOCH * epochs);
        ScriptAll scriptAll = new ScriptAll().addScript(requireTimeBefore).addScript(scriptPubkey);
        return new Policy(name, scriptAll).addKey(keys.getSkey());
    }
}
