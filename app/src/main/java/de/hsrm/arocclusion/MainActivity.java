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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.AugmentedImage;
import com.google.ar.core.AugmentedImageDatabase;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.rendering.Material;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.RenderableDefinition;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import javax.annotation.Nullable;
import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dagger.android.support.DaggerAppCompatActivity;

/**
 * This is an example activity that uses the Sceneform UX package to make common AR tasks easier.
 */
public class MainActivity extends DaggerAppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final double MIN_OPENGL_VERSION = 3.0;

    @BindView(R.id.proxy_gen_button)
    Button proxyGenButton;
    @BindView(R.id.toggle_proxy_material_button)
    Button toggleProxyMaterialButton;
    @BindView(R.id.new_scene_button)
    Button newSceneButton;
    @BindView(R.id.scenes_view)
    ScenesView scenesView;

    @Inject
    ARSceneRepository arSceneRepository;

    private ArFragment arFragment;
    private ModelRenderable andyRenderable;
    private Material proxyMat;
    private Material proxyVisualMat;
    private List<Node> proxyNodes = new ArrayList<>();
    private boolean showProxies = true;

    private PoindCloudProxyGenerator poindCloudProxyGenerator = new PoindCloudProxyGenerator();
    private ARScene currentScene;
    private ARSubScene currentSubScene;
    private AnchorNode environmentNode = new AnchorNode();

    private Set<AugmentedImage> augmentedImages = new HashSet<>();
    private Anchor referencePointAnchor;

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

        arFragment.getArSceneView().getScene().addOnUpdateListener(this::onUpdateFrame);
        arFragment.getArSceneView().getScene().addChild(environmentNode);
        scenesView.setScenes(arSceneRepository.getARSceneNames());

        // add a test scene, if not yet available
        if (!arSceneRepository.getARSceneNames().contains("test")) {
            ARScene arScene = new ARScene();
            arScene.setName("test");
            arScene.getSubScenes().add(new ARSubScene());
            arSceneRepository.saveARScene(arScene);
        }

        loadModelRenderable(R.raw.andy, renderable -> andyRenderable = renderable, "Unable to load andy renderable");
        loadModelRenderable(R.raw.proxy, renderable -> {
            proxyMat = renderable.getMaterial();
            proxyMat.setFloat4("baseColor", 0f, 0.8f, 1f, 0.6f);
        }, "Unable to load andy renderable");
        loadModelRenderable(R.raw.proxy_visual, renderable -> {
            proxyVisualMat = renderable.getMaterial();
            proxyVisualMat.setFloat4("baseColor", 0f, 0.8f, 1f, 0.6f);
            // TODO: 27.06.2019 move this somewhere sensible
            loadAndActivateScene("test");
        }, "Unable to load proxy visual renderable");

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

    private void loadModelRenderable(int resource, Consumer<ModelRenderable> accept, String errorMessage) {
        ModelRenderable.builder()
                .setSource(this, resource)
                .build()
                .thenAccept(accept)
                .exceptionally(
                        throwable -> {
                            Toast toast =
                                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            throwable.printStackTrace();
                            return null;
                        });
    }

    @OnClick(R.id.toggle_proxy_material_button)
    void onToggleProxyMaterialButtonClick(View v) {
        showProxies = !showProxies;
        for (Node proxyNode : proxyNodes) {
            proxyNode.getRenderable().setMaterial(showProxies ? proxyVisualMat : proxyMat);
        }
    }

    @OnClick(R.id.new_scene_button)
    void onNewSceneButtonClick(View v) {
        ARScene arScene = new ARScene();
        arScene.setName("test");
        arScene.getSubScenes().add(new ARSubScene());
        arSceneRepository.saveARScene(arScene);
        loadAndActivateScene("test");
    }

    @OnClick(R.id.scenes_panel_button)
    void onScenesPanelButtonClick(View v) {
        if (scenesView.getVisibility() == View.VISIBLE) {
            scenesView.setVisibility(View.GONE);
        } else {
            scenesView.setVisibility(View.VISIBLE);
        }
    }

    @OnClick(R.id.proxy_gen_button)
    void onProxyGenButtonClick(View v) {
        Frame arFrame = arFragment.getArSceneView().getArFrame();
        if (arFrame != null) {
            ProxyModel proxy = poindCloudProxyGenerator.generateProxyModel(arFrame, referencePointAnchor);
            if (proxy != null) {
                addProxyToCurrentSubScene(proxy, showProxies ? proxyVisualMat : proxyMat);
                arSceneRepository.saveARScene(currentScene);
                arSceneRepository.debugPrintJson(currentScene);
            } else {
                Log.e(TAG, "onProxyGenButtonClick: es konnte kein proxy erstellt werden");
            }
        }
    }

    private void activateSubScene(@Nullable ARSubScene subScene) {
        currentSubScene = subScene;
        removeProxiesFromScenegraph();
        if (subScene != null) {
            for (ProxyModel proxyModel : currentSubScene.getEnvironment().getProxies()) {
                addProxyToScenegraph(proxyModel, showProxies ? proxyVisualMat : proxyMat);
            }
        }
    }

    private void onUpdateFrame(FrameTime frameTime) {
        Frame frame = arFragment.getArSceneView().getArFrame();

        // If there is no frame or ARCore is not tracking yet, just return.
        if (frame == null || frame.getCamera().getTrackingState() != TrackingState.TRACKING) {
            return;
        }

        Collection<AugmentedImage> updatedAugmentedImages = frame.getUpdatedTrackables(AugmentedImage.class);
        for (AugmentedImage updatedAugmentedImage : updatedAugmentedImages) {
            switch (updatedAugmentedImage.getTrackingState()) {
                case TRACKING:
                    if (!augmentedImages.contains(updatedAugmentedImage)) {
                        augmentedImages.add(updatedAugmentedImage);
                        imageReferencePointDetected(updatedAugmentedImage);
                    }
                    break;
                case STOPPED:
                    augmentedImages.remove(updatedAugmentedImage);
                    break;
            }
        }
    }

    private void loadAndActivateScene(String name) {
        try {
            currentScene = arSceneRepository.getARScene(name);
            setupImageReferencePointRecognition(currentScene);
            activateSubScene(currentScene.hasSubScenes() ? currentScene.getSubScenes().get(0) : null);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "loadAndActivateScene: could not load ar scene " + name);
        }
    }

    private void removeProxiesFromScenegraph() {
        for (Node proxyNode : proxyNodes) {
            proxyNode.setParent(null);
        }
    }

    private void addProxyToCurrentSubScene(ProxyModel proxy, Material material) {
        if (currentSubScene == null) {
            return;
        }
        currentSubScene.getEnvironment().getProxies().add(proxy);
        addProxyToScenegraph(proxy, material);
    }

    private void addProxyToScenegraph(ProxyModel proxy, Material material) {
        List<RenderableDefinition.Submesh> subMeshes = new ArrayList<>();
        Log.e(TAG, "addProxyToScenegraph: " + material + " : " + proxy.getTriangleIndices());
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
                    node.setParent(environmentNode);
                    node.setRenderable(modelRenderable);
                    node.setLocalPosition(proxy.getPosition());
                    node.setLocalRotation(proxy.getRotation());
                    proxyNodes.add(node);
                })
                .exceptionally(throwable -> {
                    throwable.printStackTrace();
                    return null;
                });
    }

    private void setupImageReferencePointRecognition(ARScene scene) {
        // TODO: 28.06.2019 add all images from the scenes reference points
        Session session = arFragment.getArSceneView().getSession();
        Config config = Objects.requireNonNull(session).getConfig();
        AugmentedImageDatabase augmentedImageDatabase = new AugmentedImageDatabase(session);
        try (InputStream is = getAssets().open("default_marker.jpg")) {
            Bitmap augmentedImageBitmap = BitmapFactory.decodeStream(is);
            augmentedImageDatabase.addImage("default_marker", augmentedImageBitmap);
        } catch (IOException e) {
            Log.e(TAG, "IO exception loading augmented image bitmap.", e);
        }
        config.setAugmentedImageDatabase(augmentedImageDatabase);
        session.configure(config);
    }

    private void imageReferencePointDetected(AugmentedImage image) {
        Log.d(TAG, "imageReferencePointDetected: " + image.getName());
        if (referencePointAnchor != null) {
            referencePointAnchor.detach();
        }
        referencePointAnchor = arFragment.getArSceneView().getSession().createAnchor(image.getCenterPose());
        environmentNode.setAnchor(referencePointAnchor);

        AnchorNode anchorNode = new AnchorNode(referencePointAnchor);
        anchorNode.setParent(arFragment.getArSceneView().getScene());

        // Create the transformable andy and add it to the anchor.
        TransformableNode andy = new TransformableNode(arFragment.getTransformationSystem());
        andy.setParent(anchorNode);
        andy.setRenderable(andyRenderable);
    }

    private boolean isReferencePointActive() {
        return referencePointAnchor != null;
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
