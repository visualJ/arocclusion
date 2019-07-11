package de.hsrm.arocclusion;

import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;

public class ReferencePoint {

    private String type;
    private Vector3 position = Vector3.zero();
    private Quaternion rotation = Quaternion.identity();
    private boolean positionKnown;

    public ReferencePoint(String type) {
        this.type = type;
    }

    public Vector3 getPosition() {
        return position;
    }

    public void setPosition(Vector3 position) {
        this.position = position;
    }

    public Quaternion getRotation() {
        return rotation;
    }

    public void setRotation(Quaternion rotation) {
        this.rotation = rotation;
    }

    public String getType() {
        return type;
    }

    public boolean isPositionKnown() {
        return positionKnown;
    }

    public void setPositionKnown(boolean positionKnown) {
        this.positionKnown = positionKnown;
    }
}
