package de.hsrm.arocclusion;

public class ARSubScene {

    private VirtualScene scene;
    private Environment environment;
    private String name;

    public ARSubScene() {
        this(new VirtualScene(), new Environment());
    }

    public ARSubScene(VirtualScene scene, Environment environment) {
        this.scene = scene;
        this.environment = environment;
    }

    public VirtualScene getScene() {
        return scene;
    }

    public void setScene(VirtualScene scene) {
        this.scene = scene;
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
