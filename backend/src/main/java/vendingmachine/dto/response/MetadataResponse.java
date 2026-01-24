package vendingmachine.dto.response;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.util.HashMap;
import java.util.Map;

public class MetadataResponse {

    private String name;
    private String image;
    private Map<String, String> dynamicAttributes;

    public MetadataResponse(String name, String image, String attributes) {
        this.name = name;
        this.image = image;
        this.dynamicAttributes = parseJsonToMap(attributes);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Map<String, String> getDynamicAttributes() {
        return dynamicAttributes;
    }

    public void setDynamicAttributes(Map<String, String> dynamicAttributes) {
        this.dynamicAttributes = dynamicAttributes;
    }

    private Map<String, String> parseJsonToMap(String jsonStr) {
        Map<String, String> map = new HashMap<>();
        try {
            JSONObject jsonObject = (JSONObject) new JSONParser().parse(jsonStr);
            for (Object key : jsonObject.keySet()) {
                map.put((String) key, (String) jsonObject.get(key));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }
}
