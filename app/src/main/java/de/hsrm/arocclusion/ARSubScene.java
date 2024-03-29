package de.hsrm.arocclusion;

public class ARSubScene {

    private VirtualScene virtualScene;
    private Environment environment;
    private String name;
    private transient ARScene parent;

    public ARSubScene() {
        this("ARSubscene");
    }

    public ARSubScene(String name) {
        this(name, new VirtualScene(), new Environment());
    }

    public ARSubScene(String name, VirtualScene virtualScene, Environment environment) {
        setName(name);
        setVirtualScene(virtualScene);
        setEnvironment(environment);
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
        environment.setArSubScene(this);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ARScene getParent() {
        return parent;
    }

    public void setParent(ARScene parent) {
        this.parent = parent;
    }

    public void runPostDeserializationProcessing() {
        setEnvironment(environment);
        environment.runPostDeserializationProcessing();
    }

}
