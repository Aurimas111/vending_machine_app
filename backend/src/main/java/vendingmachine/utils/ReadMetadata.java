package vendingmachine.utils;

import vendingmachine.model.*;
import com.bloxbean.cardano.client.crypto.Keys;
import com.bloxbean.cardano.client.crypto.VerificationKey;
import com.bloxbean.cardano.client.transaction.spec.Policy;
import com.bloxbean.cardano.client.transaction.spec.script.RequireTimeBefore;
import com.bloxbean.cardano.client.transaction.spec.script.ScriptAll;
import com.bloxbean.cardano.client.transaction.spec.script.ScriptPubkey;
import io.blockfrost.sdk.api.IPFSService;
import io.blockfrost.sdk.api.exception.APIException;
import io.blockfrost.sdk.api.model.ipfs.IPFSObject;
import io.blockfrost.sdk.api.model.ipfs.PinItem;
import io.blockfrost.sdk.api.model.ipfs.PinResponse;
import io.blockfrost.sdk.api.util.OrderEnum;
import io.blockfrost.sdk.impl.IPFSServiceImpl;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ReadMetadata {
    private static final long SLOTS_PER_EPOCH = 5 * 24 * 60 * 60;
    public static ArrayList<NftMetadata> read(String folderPath) throws IOException, ParseException {
        JSONParser parser = new JSONParser();

        JSONArray jsonArray = (JSONArray) parser.parse(new FileReader(folderPath));

        ArrayList<NftMetadata> metadataList = new ArrayList<>();

        for (Object obj : jsonArray) {
            JSONObject jsonObject = (JSONObject) obj;
            NftMetadata metadata = parseMetadataObject(jsonObject);
            metadataList.add(metadata);
        }

        return metadataList;
    }

    private static NftMetadata parseMetadataObject(JSONObject jsonObject) {
        String name = (String) jsonObject.get("name");
        String fileUrl = (String) jsonObject.get("file_url");
        String image = (String) jsonObject.get("image");
        String ipfsHash = (String) jsonObject.get("ipfsHash");

        // dynamic attributes
        Map<String, String> attributes = new HashMap<>();
        JSONArray attributesArray = (JSONArray) jsonObject.get("attributes");
        if (attributesArray != null) {
            for (Object obj : attributesArray) {
                JSONObject attribute = (JSONObject) obj;
                String traitType = (String) attribute.get("trait_type");
                String value = (String) attribute.get("value");
                attributes.put(traitType, value);
            }
        }

        return new NftMetadata(name, fileUrl, image, ipfsHash, attributes);
    }


    public static ArrayList<String> uploadImages(ArrayList<NftMetadata> metadata) throws APIException, IOException {
        ArrayList<String> ipfs= new ArrayList<>();
        IPFSService ipfsService = new IPFSServiceImpl("https://ipfs.blockfrost.io/api/v0/", Constant.IPFS_KEY);
        IPFSObject ipfsObject =  new IPFSObject();
        PinResponse pinResponse =  new PinResponse();
        PinItem pin = new PinItem();
        for(int i = 0 ;i< metadata.size();i++) {
            ipfsObject = ipfsService.add(new File(metadata.get(i).getFile_url()));
            pinResponse = ipfsService.pinAdd(ipfsObject.getIpfsHash());
            pin = ipfsService.getPinnedObjectByIpfsPath(ipfsObject.getIpfsHash());

            ipfs.add(pin.getIpfsHash());
        }


        // unpin everything
        /*List<PinItem> pins = ipfsService.getAllPinnedObjects();
        for(int i = 0;i<pins.size();i++)
        ipfsService.removePinnedObject(String.valueOf(pins.get(i).getIpfsHash()));*/

        return ipfs;
    }

    public static ArrayList<String> getImages() throws APIException {
        ArrayList<String> ipfsHashes = new ArrayList<>();
        List<PinItem> pinnedObjects = new ArrayList<>();
        IPFSService ipfsService = new IPFSServiceImpl("https://ipfs.blockfrost.io/api/v0/", Constant.IPFS_KEY);
        pinnedObjects = ipfsService.getAllPinnedObjects(OrderEnum.asc);
        for(int i =0;i< pinnedObjects.size();i++) {
            ipfsHashes.add(pinnedObjects.get(i).getIpfsHash());
        }
        return ipfsHashes;
    }

    public static Policy createEpochPolicy(String name, long currentSlot, long epochs, Keys keys) {
        VerificationKey verificationKey = keys.getVkey();
        ScriptPubkey scriptPubkey = ScriptPubkey.create(verificationKey);
        RequireTimeBefore requireTimeBefore = new RequireTimeBefore(currentSlot + SLOTS_PER_EPOCH * epochs);
        ScriptAll scriptAll = new ScriptAll().addScript(requireTimeBefore).addScript(scriptPubkey);
        return new Policy(name, scriptAll).addKey(keys.getSkey());
    }
}
