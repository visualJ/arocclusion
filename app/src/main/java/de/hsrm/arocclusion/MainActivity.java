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
package de.hsrm.arocclusion;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.ar.core.Anchor;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.rendering.Material;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.RenderableDefinition;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * This is an example activity that uses the Sceneform UX package to make common AR tasks easier.
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final double MIN_OPENGL_VERSION = 3.0;

    @BindView(R.id.proxy_gen_button)
    Button proxyGenButton;
    @BindView(R.id.toggleProxyMaterialButton)
    Button toggleProxyMaterialButton;

    private ArFragment arFragment;
    private ModelRenderable andyRenderable;
    private Material proxyMat;
    private Material proxyVisualMat;
    private List<Node> proxyNodes = new ArrayList<>();
    private boolean showProxies = true;

    private PoindCloudProxyGenerator poindCloudProxyGenerator = new PoindCloudProxyGenerator();
    private ARSceneRepository arSceneRepository;
    private ARScene currentScene;
    private ARSubScene currentSubScene;

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
        ButterKnife.bind(this);

        arSceneRepository = new ARSceneRepository(this);

        ARScene arScene = new ARScene();
        arScene.setName("test");
        arScene.getSubScenes().add(new ARSubScene());
        arSceneRepository.saveARScene(arScene);
        loadAndActivateScene("test");

        // When you build a Renderable, Sceneform loads its resources in the background while returning
        // a CompletableFuture. Call thenAccept(), handle(), or check isDone() before calling get().
        ModelRenderable.builder()
                .setSource(this, R.raw.andy)
                .build()
                .thenAccept(renderable -> andyRenderable = renderable)
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

        arFragment.getArSceneView().setCameraStreamRenderPriority(1);
    }

    @OnClick(R.id.toggleProxyMaterialButton)
    void onToggleProxyMaterialButtonClick(View v) {
        showProxies = !showProxies;
        for (Node proxyNode : proxyNodes) {
            proxyNode.getRenderable().setMaterial(showProxies ? proxyVisualMat : proxyMat);
        }
    }

    @OnClick(R.id.proxy_gen_button)
    void onProxyGenButtonClick(View v) {
        Frame arFrame = arFragment.getArSceneView().getArFrame();
        if (arFrame != null) {
            ProxyModel proxy = poindCloudProxyGenerator.generateProxyModel(arFrame);
            addProxy(proxy, showProxies ? proxyVisualMat : proxyMat);
            arSceneRepository.saveARScene(currentScene);
            arSceneRepository.debugPrintJson(currentScene);
        }
    }

    private void activateSubScene(@Nullable ARSubScene subScene) {
        currentSubScene = subScene;
        removeProxies();
        if (subScene != null) {
            for (ProxyModel proxyModel : currentSubScene.getEnvironment().getProxies()) {
                addProxy(proxyModel, showProxies ? proxyVisualMat : proxyMat);
            }
        }
    }

    private void loadAndActivateScene(String name) {
        try {
            currentScene = arSceneRepository.getARScene(name);
            activateSubScene(currentScene.hasSubScenes() ? currentScene.getSubScenes().get(0) : null);
            arSceneRepository.debugPrintJson(currentScene);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "loadAndActivateScene: could not load ar scene " + name);
        }
    }

    private void removeProxies() {
        for (Node proxyNode : proxyNodes) {
            proxyNode.setParent(null);
        }
    }

    private void addProxy(ProxyModel proxy, Material material) {
        currentSubScene.getEnvironment().getProxies().add(proxy);

        List<RenderableDefinition.Submesh> subMeshes = new ArrayList<>();
        subMeshes.add(new RenderableDefinition.Submesh.Builder().setName("proxy").setMaterial(material).setTriangleIndices(proxy.getTriangleIndices()).build());

        RenderableDefinition renderableDefinition = RenderableDefinition.builder()
                .setVertices(proxy.getVertices())
                .setSubmeshes(subMeshes)
                .build();
        ModelRenderable.builder()
                .setSource(renderableDefinition)
                .build()
                .thenAccept(modelRenderable -> {
                    modelRenderable.setMaterial(material);
                    // place model in scene
                    Node node = new Node();
                    node.setParent(arFragment.getArSceneView().getScene());
                    node.setRenderable(modelRenderable);
                    node.setWorldPosition(proxy.getPosition());
                    node.setWorldRotation(proxy.getRotation());
                    proxyNodes.add(node);
                })
                .exceptionally(throwable -> {
                    throwable.printStackTrace();
                    return null;
                });
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
