package de.hsrm.arocclusion;

public class ARSubScene {

    private VirtualScene virtualScene;
    private Environment environment;
    private String name = "ARSubScene";

    public ARSubScene() {
        this(new VirtualScene(), new Environment());
    }

    public ARSubScene(VirtualScene virtualScene, Environment environment) {
        this.virtualScene = virtualScene;
        this.environment = environment;
    }

    public VirtualScene getVirtualScene() {
        return virtualScene;
    }

    public void setVirtualScene(VirtualScene virtualScene) {
        this.virtualScene = virtualScene;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
