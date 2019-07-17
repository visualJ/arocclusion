package de.hsrm.arocclusion;

import com.google.ar.core.Frame;
import com.google.ar.core.PointCloud;
import com.google.ar.core.Pose;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Vertex;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.github.jdiemke.triangulation.DelaunayTriangulator;
import io.github.jdiemke.triangulation.NotEnoughPointsException;
import io.github.jdiemke.triangulation.Triangle2D;
import io.github.jdiemke.triangulation.Vector2D;

public class PoindCloudProxyGenerator {

    public ProxyModel generateProxyModel(Frame frame, Pose anchorPose) {
        List<Vertex> vertices = new ArrayList<>();
        List<Integer> triangleIndices = new ArrayList<>();

        // aquire pointcloud and build vertex list
        PointCloud pointCloud = frame.acquirePointCloud();
        FloatBuffer points = pointCloud.getPoints();

        while (points.remaining() >= 4) {
            float[] point = new float[]{points.get(), points.get(), points.get()};
            float certainty = points.get(); // todo filter out uncertain points

            // transform point into camera coordinate system
            point = frame.getAndroidSensorPose().inverse().transformPoint(point);

            vertices.add(new Vertex.Builder().setPosition(new Vector3(point[0], point[1], point[2])).build());
        }
        pointCloud.release();

        // triangulate

        List<Vector2D> pointSet = new ArrayList<>();
        for (Vertex vert : vertices) {
            pointSet.add(new Vector2D(vert.getPosition().x, vert.getPosition().y));
        }
        DelaunayTriangulator triangulator = new DelaunayTriangulator(pointSet);
        try {
            triangulator.triangulate();
        } catch (NotEnoughPointsException | NullPointerException e) {
            e.printStackTrace();
            return null;
        }

        // build a lookup table for vertex indices
        HashMap<String, Integer> vertIndices = new HashMap<>();
        int vIndex = 0;
        for (Vector2D point : pointSet) {
            vertIndices.put(getVertIndexKey(point), vIndex++);
        }
        List<Triangle2D> triangles = triangulator.getTriangles();
        // lookup vertex indices and add them to the index list
        for (Triangle2D triangle : triangles) {
            triangleIndices.add(vertIndices.get(getVertIndexKey(triangle.a)));
            // check, which way the triangle is facing to correct flipped normals
            if (getFaceOrientation(triangle) > 0) {
                triangleIndices.add(vertIndices.get(getVertIndexKey(triangle.b)));
                triangleIndices.add(vertIndices.get(getVertIndexKey(triangle.c)));
            } else {
                // flip two vertices to flip the triangle normal
                triangleIndices.add(vertIndices.get(getVertIndexKey(triangle.c)));
                triangleIndices.add(vertIndices.get(getVertIndexKey(triangle.b)));
            }
        }

        ProxyModel proxyModel = new ProxyModel(vertices, triangleIndices);
        Pose androidSensorPose = frame.getAndroidSensorPose();
        Pose relativeToAnchorPose = anchorPose.inverse().compose(androidSensorPose);
        proxyModel.setPosition(new Vector3(relativeToAnchorPose.tx(), relativeToAnchorPose.ty(), relativeToAnchorPose.tz()));
        proxyModel.setRotation(new Quaternion(relativeToAnchorPose.qx(), relativeToAnchorPose.qy(), relativeToAnchorPose.qz(), relativeToAnchorPose.qw()));

        return proxyModel;
    }

    private static String getVertIndexKey(Vector2D point) {
        return point.x + ":" + point.y;
    }

    /**
     * Check whether the triangle is defined clockwise or anti-clockwise
     *
     * @param t the triangle
     * @return Negative number: Clockwise, Positive: anti-clockwise, 0: Collinear
     */
    private static double getFaceOrientation(Triangle2D t) {
        return (t.b.x * t.c.y + t.a.x * t.b.y + t.a.y * t.c.x) - (t.a.y * t.b.x + t.b.y * t.c.x + t.a.x * t.c.y);
    }
}
