package de.hsrm.arocclusion.util;

import android.util.Log;

import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Vertex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class MeshUtil {
    private static final String TAG = "MeshUtil";
    private static final Vector3 DEFAULT_NORMAL = new Vector3(0, 0, 1);

    public static void calculateVertexNormals(List<Vertex> vertices, List<Integer> triangleIndices) {
        Map<Vertex, List<Vector3>> vertexNormals = new HashMap<>();
        for (Vertex vertex : vertices) {
            vertexNormals.put(vertex, new ArrayList<>());
        }

        if (triangleIndices.size() < 3 || triangleIndices.size() % 3 != 0) {
            Log.e(TAG, "calculateVertexNormals: triangleIndices number of elements needs to be 3 or more and divisible by 3.");
            return;
        }

        // calculate each triangles normal and save for each triangle vertex
        for (int i = 0; i < triangleIndices.size(); i += 3) {
            List<Vertex> triangle = triangleIndices.subList(i, i + 3).stream().map(vertices::get).collect(Collectors.toList());
            Vector3 normal = getNormal(triangle);
            for (Vertex vertex : triangle) {
                vertexNormals.get(vertex).add(normal);
            }
        }

        // calculate each vertex' mean normal
        for (Vertex vertex : vertices) {
            vertex.setNormal(getMeanVector3(vertexNormals.getOrDefault(vertex, Collections.singletonList(DEFAULT_NORMAL))));
        }

    }

    private static Vector3 getNormal(List<Vertex> vertices) {
        Vector3 v1 = vertices.get(0).getPosition();
        Vector3 v2 = vertices.get(1).getPosition();
        Vector3 v3 = vertices.get(2).getPosition();
        Vector3 a = Vector3.subtract(v2, v1);
        Vector3 b = Vector3.subtract(v3, v1);
        return Vector3.cross(a, b).normalized();
    }

    private static Vector3 getMeanVector3(List<Vector3> vectors) {
        if (vectors == null || vectors.isEmpty()) {
            return DEFAULT_NORMAL;
        }

        Vector3 result = new Vector3();
        for (Vector3 vector : vectors) {
            result.x += vector.x;
            result.y += vector.y;
            result.z += vector.z;
        }
        return result.scaled(1f / vectors.size());
    }
}
