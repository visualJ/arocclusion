/*
 * Copyright 2018 Google LLC. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.ar.sceneform.samples.hellosceneform;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.PointCloud;
import com.google.ar.core.Pose;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Material;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.RenderableDefinition;
import com.google.ar.sceneform.rendering.Vertex;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.github.jdiemke.triangulation.DelaunayTriangulator;
import io.github.jdiemke.triangulation.NotEnoughPointsException;
import io.github.jdiemke.triangulation.Triangle2D;
import io.github.jdiemke.triangulation.Vector2D;

/**
 * This is an example activity that uses the Sceneform UX package to make common AR tasks easier.
 */
public class HelloSceneformActivity extends AppCompatActivity {
    private static final String TAG = HelloSceneformActivity.class.getSimpleName();
    private static final double MIN_OPENGL_VERSION = 3.0;

    private ArFragment arFragment;
    private ModelRenderable andyRenderable;
    private Button proxyGenButton;
    private Button toggleProxyMaterialButton;
    private ModelRenderable proxyRenderable;
    private Material proxyMat;
    private Material proxyVisualMat;
    private List<Node> proxyNodes = new ArrayList<>();
    private boolean showProxies = true;

    @Override
    @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
    // CompletableFuture requires api level 24
    // FutureReturnValueIgnored is not valid
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!checkIsSupportedDeviceOrFinish(this)) {
            return;
        }

        setContentView(R.layout.activity_ux);
        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);
        proxyGenButton = findViewById(R.id.proxy_gen_button);
        toggleProxyMaterialButton = findViewById(R.id.toggleProxyMaterialButton);
        toggleProxyMaterialButton.setOnClickListener(this::onToggleProxyMaterialButtonClick);

        proxyGenButton.setOnClickListener(this::onProxyGenButtonClick);
//        MaterialFactory.makeOpaqueWithColor(this, new Color(.5f, .5f, .5f)).thenAccept(material -> proxyMat = material);

        // When you build a Renderable, Sceneform loads its resources in the background while returning
        // a CompletableFuture. Call thenAccept(), handle(), or check isDone() before calling get().
        ModelRenderable.builder()
                .setSource(this, R.raw.andy)
                .build()
                .thenAccept(renderable -> {
                    andyRenderable = renderable;
                    andyRenderable.setRenderPriority(7);
                })
                .exceptionally(
                        throwable -> {
                            Toast toast =
                                    Toast.makeText(this, "Unable to load andy renderable", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            return null;
                        });

        ModelRenderable.builder()
                .setSource(this, R.raw.proxy)
                .build()
                .thenAccept(renderable -> {
                    proxyMat = renderable.getMaterial();
                    proxyMat.setFloat4("baseColor", 0f, 0.8f, 1f, 0.6f);
                })
                .exceptionally(
                        throwable -> {
                            Toast toast =
                                    Toast.makeText(this, "Unable to load proxy renderable", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            return null;
                        });

        ModelRenderable.builder()
                .setSource(this, R.raw.proxy_visual)
                .build()
                .thenAccept(renderable -> {
                    proxyVisualMat = renderable.getMaterial();
                    proxyVisualMat.setFloat4("baseColor", 0f, 0.8f, 1f, 0.6f);
                })
                .exceptionally(
                        throwable -> {
                            Toast toast =
                                    Toast.makeText(this, "Unable to load proxy renderable", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            return null;
                        });

        arFragment.setOnTapArPlaneListener(
                (HitResult hitResult, Plane plane, MotionEvent motionEvent) -> {
                    if (andyRenderable == null) {
                        return;
                    }

                    // Create the Anchor.
                    Anchor anchor = hitResult.createAnchor();
                    AnchorNode anchorNode = new AnchorNode(anchor);
                    anchorNode.setParent(arFragment.getArSceneView().getScene());

                    // Create the transformable andy and add it to the anchor.
                    TransformableNode andy = new TransformableNode(arFragment.getTransformationSystem());
                    andy.setParent(anchorNode);
                    andy.setRenderable(andyRenderable);
                    andy.select();
                });
    }

    private void onToggleProxyMaterialButtonClick(View v) {
        showProxies = !showProxies;
        for (Node proxyNode : proxyNodes) {
            proxyNode.getRenderable().setMaterial(showProxies ? proxyVisualMat : proxyMat);
        }
    }

    private void onProxyGenButtonClick(View v) {
        Log.d(TAG, "onProxyGenButtonClick: clicked the button");
        ArSceneView arSceneView = arFragment.getArSceneView();
        Frame arFrame = arSceneView.getArFrame();

        if (arFrame == null) {
            return;
        }

        // aquire pointcloud and build vertex list
        PointCloud pointCloud = arFrame.acquirePointCloud();
        FloatBuffer points = pointCloud.getPoints();
        Log.d(TAG, "onProxyGenButtonClick: " + points.limit());

        List<Vertex> verts = new ArrayList<>();
        while (points.remaining() >= 4) {
            float[] point = new float[]{points.get(), points.get(), points.get()};
            float certainty = points.get(); // todo filter out uncertain points

            // transform copy with inverse camera pose
            point = arFrame.getAndroidSensorPose().inverse().transformPoint(point);

            verts.add(new Vertex.Builder().setPosition(new Vector3(point[0], point[1], point[2])).build());
        }
        pointCloud.release();

        // triangulate

        List<Vector2D> pointSet = new ArrayList<>();
        for (Vertex vert : verts) {
            pointSet.add(new Vector2D(vert.getPosition().x, vert.getPosition().y));
        }
        DelaunayTriangulator triangulator = new DelaunayTriangulator(pointSet);
        try {
            triangulator.triangulate();
        } catch (NotEnoughPointsException | NullPointerException e) {
            e.printStackTrace();
            return;
        }

        // build a lookup table for vertex indices
        HashMap<String, Integer> vertIndices = new HashMap<>();
        int vIndex = 0;
        for (Vector2D point : pointSet) {
            vertIndices.put(getVertIndexKey(point), vIndex++);
        }
        List<Integer> indices = new ArrayList<>();
        List<Triangle2D> triangles = triangulator.getTriangles();
        // lookup vertex indices and add them to the index list
        for (Triangle2D triangle : triangles) {
            // check, which way the triangle is facing to correct flipped normals
            if (getFaceOrientation(triangle) > 0) {
                indices.add(vertIndices.get(getVertIndexKey(triangle.a)));
                indices.add(vertIndices.get(getVertIndexKey(triangle.b)));
                indices.add(vertIndices.get(getVertIndexKey(triangle.c)));
            } else {
                // flip two vertices to flip the triangle normal
                indices.add(vertIndices.get(getVertIndexKey(triangle.a)));
                indices.add(vertIndices.get(getVertIndexKey(triangle.c)));
                indices.add(vertIndices.get(getVertIndexKey(triangle.b)));
            }
        }

        // build model

        List<RenderableDefinition.Submesh> subMeshes = new ArrayList<>();
        subMeshes.add(new RenderableDefinition.Submesh.Builder().setName("Hans").setMaterial(proxyMat).setTriangleIndices(indices).build());

        RenderableDefinition renderableDefinition = RenderableDefinition.builder()
                .setVertices(verts)
                .setSubmeshes(subMeshes)
                .build();
        ModelRenderable.builder()
                .setSource(renderableDefinition)
                .build()
                .thenAccept(modelRenderable -> {
                    proxyRenderable = modelRenderable;
                    proxyRenderable.setMaterial(showProxies ? proxyVisualMat : proxyMat);
                    proxyRenderable.setRenderPriority(7);
                })
                .exceptionally(throwable -> {
                    throwable.printStackTrace();
                    return null;
                });

        // place model in scene
        Node node = new Node();
        node.setParent(arSceneView.getScene());
        node.setRenderable(proxyRenderable);
        Pose androidSensorPose = arFrame.getAndroidSensorPose();
        node.setWorldPosition(new Vector3(androidSensorPose.tx(), androidSensorPose.ty(), androidSensorPose.tz()));
        node.setWorldRotation(new Quaternion(androidSensorPose.qx(), androidSensorPose.qy(), androidSensorPose.qz(), androidSensorPose.qw()));
        proxyNodes.add(node);

    }

    private String getVertIndexKey(Vector2D point) {
        return point.x + ":" + point.y;
    }

    /**
     * Check whether the triangle is defined clockwise or anti-clockwise
     *
     * @param t the triangle
     * @return Negative number: Clockwise, Positive: anti-clockwise, 0: Collinear
     */
    private double getFaceOrientation(Triangle2D t) {
        return (t.b.x * t.c.y + t.a.x * t.b.y + t.a.y * t.c.x) - (t.a.y * t.b.x + t.b.y * t.c.x + t.a.x * t.c.y);
    }

    /**
     * Returns false and displays an error message if Sceneform can not run, true if Sceneform can run
     * on this device.
     *
     * <p>Sceneform requires Android N on the device as well as OpenGL 3.0 capabilities.
     *
     * <p>Finishes the activity if Sceneform can not run
     */
    public static boolean checkIsSupportedDeviceOrFinish(final Activity activity) {
        if (Build.VERSION.SDK_INT < VERSION_CODES.N) {
            Log.e(TAG, "Sceneform requires Android N or later");
            Toast.makeText(activity, "Sceneform requires Android N or later", Toast.LENGTH_LONG).show();
            activity.finish();
            return false;
        }
        String openGlVersionString =
                ((ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE))
                        .getDeviceConfigurationInfo()
                        .getGlEsVersion();
        if (Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION) {
            Log.e(TAG, "Sceneform requires OpenGL ES 3.0 later");
            Toast.makeText(activity, "Sceneform requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG)
                    .show();
            activity.finish();
            return false;
        }
        return true;
    }
}
