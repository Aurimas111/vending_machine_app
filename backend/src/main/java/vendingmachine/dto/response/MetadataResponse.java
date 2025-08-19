package vendingmachine.dto.response;

import java.util.Map;

public class MetadataResponse {

    private String name;
    private String image;
    private Map<String, String> dynamicAttributes;

    public MetadataResponse(String name, String image, Map<String, String> dynamicAttributes) {
        this.name = name;
        this.image = image;
        this.dynamicAttributes = dynamicAttributes;
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
}
