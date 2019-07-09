package de.hsrm.arocclusion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ARScene {

    private List<ARSubScene> subScenes = new ArrayList<>();
    private String name;

    /**
     * Get a read-only list of sub scenes. Use appropriate methods on the scene to modify.
     *
     * @return an unmodifiable list of subscenes
     */
    public List<ARSubScene> getSubScenes() {
        return Collections.unmodifiableList(subScenes);
    }

    public boolean hasSubScenes() {
        return !subScenes.isEmpty();
    }

    public void setSubScenes(List<ARSubScene> subScenes) {
        this.subScenes = subScenes;
        for (ARSubScene subScene : subScenes) {
            subScene.setParent(this);
        }
    }

    public void addSubscene(ARSubScene subScene) {
        subScene.setParent(this);
        subScenes.add(subScene);
    }

    public void removeSubscene(ARSubScene subScene) {
        subScene.setParent(null);
        subScenes.remove(subScene);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
