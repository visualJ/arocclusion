package de.hsrm.arocclusion;

import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Vertex;

import java.util.ArrayList;
import java.util.List;

public class ProxyModel {

    private List<Vertex> vertices = new ArrayList<>();
    private List<Integer> triangleIndices = new ArrayList<>();
    private Vector3 position = Vector3.zero();
    private Quaternion rotation = Quaternion.identity();

    public ProxyModel() {
    }

    public ProxyModel(List<Vertex> vertices, List<Integer> triangleIndices) {
        this.vertices = vertices;
        this.triangleIndices = triangleIndices;
    }

    public List<Vertex> getVertices() {
        return vertices;
    }

    public void setVertices(List<Vertex> vertices) {
        this.vertices = vertices;
    }

    public List<Integer> getTriangleIndices() {
        return triangleIndices;
    }

    public void setTriangleIndices(List<Integer> triangleIndices) {
        this.triangleIndices = triangleIndices;
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
}
