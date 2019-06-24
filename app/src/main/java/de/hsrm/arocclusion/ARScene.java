package de.hsrm.arocclusion;

import java.util.ArrayList;
import java.util.List;

public class ARScene {

    private List<ARSubScene> subScenes = new ArrayList<>();
    private String name;

    public List<ARSubScene> getSubScenes() {
        return subScenes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}