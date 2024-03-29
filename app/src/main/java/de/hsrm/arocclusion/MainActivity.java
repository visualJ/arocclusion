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

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.google.ar.core.Anchor;
import com.google.ar.core.AugmentedImage;
import com.google.ar.core.AugmentedImageDatabase;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Camera;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.rendering.Material;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.RenderableDefinition;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

import javax.annotation.Nullable;
import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dagger.android.support.DaggerAppCompatActivity;
import de.hsrm.arocclusion.util.MeshUtil;
import de.hsrm.arocclusion.util.PoseUtil;

/**
 * This is an example activity that uses the Sceneform UX package to make common AR tasks easier.
 */
public class MainActivity extends DaggerAppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final double MIN_OPENGL_VERSION = 3.0;
    public static final int ACTIVITY_SELECT_IMAGE = 1;
    private static final int READ_STORAGE_PERMISSION_REQUEST_CODE = 1;

    @BindView(R.id.proxy_gen_button)
    Button proxyGenButton;
    @BindView(R.id.toggle_proxy_material_button)
    Button toggleProxyMaterialButton;
    @BindView(R.id.scenes_view)
    ScenesView scenesView;
    @BindView(R.id.subscene_detail_view)
    SubsceneDetailView subsceneDetailView;

    @Inject
    ARSceneRepository arSceneRepository;

    private ArFragment arFragment;
    private ModelRenderable andyRenderable;
    private ModelRenderable refpointRenderable;
    private Material proxyMat;
    private Material proxyVisualMat;
    private List<Node> proxyNodes = new ArrayList<>();
    private boolean showProxies = true;
    private boolean realTimePointCloudProxiesEnabled = false;

    private PoindCloudProxyGenerator pointCloudProxyGenerator = new PoindCloudProxyGenerator();
    private ARScene currentScene;
    private ARSubScene currentSubScene;
    private ReferencePoint currentReferencePoint;
    private AnchorNode anchorNode = new AnchorNode();
    private Node environmentNode = new Node();

    private AugmentedImage augmentedImage = null;
    private Anchor referencePointAnchor;
    private Pose lastReferencePointPoseLocal;
    private Node rtPointCloudProxyNode = new Node();

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

        if (!checkPermissionForReadExtertalStorage()) {
            requestPermissionForReadExtertalStorage();
        }

        arFragment.getArSceneView().getScene().addOnUpdateListener(this::onUpdateFrame);
        arFragment.getArSceneView().getScene().addChild(anchorNode);
        anchorNode.addChild(environmentNode);
        scenesView.setArSceneRepository(arSceneRepository);
        scenesView.setScenesViewCallback(new ScenesView.ScenesViewCallback() {
            @Override
            public void onSceneSelect(ARScene scene) {
                activateScene(scene);
            }

            @Override
            public void onSubsceneSelect(ARSubScene subScene) {
                activateSubScene(subScene);
            }
        });
        subsceneDetailView.setArSceneRepository(arSceneRepository);

        Camera camera = arFragment.getArSceneView().getScene().getCamera();
        rtPointCloudProxyNode.setParent(camera);

        // add a test scene, if not yet available
//        if (!arSceneRepository.getARSceneNames().contains("test")) {
//            ARScene arScene = new ARScene();
//            arScene.setName("test");
//            arScene.addSubscene(new ARSubScene("ARSubScene"));
//            arSceneRepository.saveARScene(arScene);
//        }

        loadModelRenderable(R.raw.andy, renderable -> andyRenderable = renderable, "Unable to load andy renderable");
        loadModelRenderable(R.raw.refpoint, renderable -> refpointRenderable = renderable, "Unable to load refpoint renderable");
        loadModelRenderable(R.raw.proxy, renderable -> {
            proxyMat = renderable.getMaterial();
            proxyMat.setFloat4("baseColor", 0f, 0.8f, 1f, 0.6f);
        }, "Unable to load andy renderable");
        loadModelRenderable(R.raw.proxy_visual, renderable -> {
            proxyVisualMat = renderable.getMaterial();
            proxyVisualMat.setFloat4("baseColor", 0f, 0.8f, 1f, 0.6f);
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

    @OnClick(R.id.scenes_panel_button)
    void onScenesPanelButtonClick(View v) {
        if (scenesView.getVisibility() == View.VISIBLE) {
            scenesView.setVisibility(View.INVISIBLE);
        } else {
            scenesView.setVisibility(View.VISIBLE);
        }
    }

    @OnClick(R.id.subscene_detail_button)
    void onSubsceneDetailButtonClick(View v) {
        if (subsceneDetailView.getVisibility() == View.VISIBLE) {
            subsceneDetailView.setVisibility(View.INVISIBLE);
        } else {
            subsceneDetailView.setVisibility(View.VISIBLE);
        }
    }

    @OnClick(R.id.proxy_gen_button)
    void onProxyGenButtonClick(View v) {
        if (currentSubScene == null) {
            Toast.makeText(this, "Keine Subszene ausgewählt!", Toast.LENGTH_LONG).show();
            return;
        }

        Frame arFrame = arFragment.getArSceneView().getArFrame();
        if (arFrame != null) {
            ProxyModel proxy = pointCloudProxyGenerator.generateProxyModel(arFrame, referencePointAnchor.getPose());
            if (proxy != null) {
                addProxyToCurrentSubScene(proxy, showProxies ? proxyVisualMat : proxyMat);
                arSceneRepository.saveARScene(currentScene);
            } else {
                Log.e(TAG, "onProxyGenButtonClick: es konnte kein proxy erstellt werden");
            }
        }
    }

    @OnClick(R.id.rt_point_cloud_proxy_button)
    void onRTPointCloudProxyButtonClick() {
        realTimePointCloudProxiesEnabled = !realTimePointCloudProxiesEnabled;
        rtPointCloudProxyNode.setEnabled(realTimePointCloudProxiesEnabled);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch (requestCode) {
            case ACTIVITY_SELECT_IMAGE:
                if (resultCode == RESULT_OK) {
                    Uri selectedImage = Objects.requireNonNull(imageReturnedIntent.getData());
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};

                    Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                    Objects.requireNonNull(cursor).moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    String filePath = cursor.getString(columnIndex);
                    cursor.close();

                    Toast.makeText(this, filePath, Toast.LENGTH_LONG).show();

                    subsceneDetailView.addImageReferencePoint(filePath);
                }
        }
    }

    private void activateSubScene(@Nullable ARSubScene subScene) {
        currentSubScene = subScene;
        subsceneDetailView.setCurrentSubScene(subScene);
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

        // Check for new ImageReferencePoints
        Collection<AugmentedImage> updatedAugmentedImages = frame.getUpdatedTrackables(AugmentedImage.class);
        for (AugmentedImage updatedAugmentedImage : updatedAugmentedImages) {
            switch (updatedAugmentedImage.getTrackingState()) {
                case TRACKING:
                    if (augmentedImage == null || !(augmentedImage.equals(updatedAugmentedImage))) {
                        if (updatedAugmentedImage.getTrackingMethod() == AugmentedImage.TrackingMethod.FULL_TRACKING) {
                            augmentedImage = updatedAugmentedImage;
                            imageReferencePointDetected(updatedAugmentedImage);
                        }
                    }
                    break;
                case STOPPED:
                    if ((augmentedImage != null) && (augmentedImage.equals(updatedAugmentedImage))) {
                        augmentedImage = null;
                    }
                    break;
            }
        }

        // real time point cloud proxies
        if (realTimePointCloudProxiesEnabled) {
            ProxyModel proxyModel = pointCloudProxyGenerator.generateProxyModel(frame, frame.getCamera().getPose());
            buildProxyRenderable(proxyModel, showProxies ? proxyVisualMat : proxyMat, rtPointCloudProxyNode::setRenderable);
        }
    }

    private void activateScene(ARScene scene) {
        currentScene = scene;
        setupImageReferencePointRecognition(currentScene);
        activateSubScene(currentScene.hasSubScenes() ? currentScene.getSubScenes().get(0) : null);
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

    private static void buildProxyRenderable(ProxyModel proxy, Material material, Consumer<ModelRenderable> callback) {
        if (proxy == null) {
            return;
        }
        List<RenderableDefinition.Submesh> subMeshes = new ArrayList<>();
        subMeshes.add(new RenderableDefinition.Submesh.Builder().setName("proxy").setMaterial(material).setTriangleIndices(proxy.getTriangleIndices()).build());

        MeshUtil.calculateVertexNormals(proxy.getVertices(), proxy.getTriangleIndices());

        RenderableDefinition renderableDefinition = RenderableDefinition.builder()
                .setVertices(proxy.getVertices())
                .setSubmeshes(subMeshes)
                .build();
        ModelRenderable.builder()
                .setSource(renderableDefinition)
                .build()
                .thenAccept(modelRenderable -> {
                    modelRenderable.setMaterial(material);
                    modelRenderable.setShadowCaster(false);
                    callback.accept(modelRenderable);
                })
                .exceptionally(throwable -> {
                    throwable.printStackTrace();
                    return null;
                });
    }

    private void addProxyToScenegraph(ProxyModel proxy, Material material) {
        buildProxyRenderable(proxy, material, modelRenderable -> {
            // place model in scene
            Node node = new Node();
            node.setParent(environmentNode);
            node.setRenderable(modelRenderable);
            node.setLocalPosition(proxy.getPosition());
            node.setLocalRotation(proxy.getRotation());
            proxyNodes.add(node);
        });

//        List<RenderableDefinition.Submesh> subMeshes = new ArrayList<>();
//        subMeshes.add(new RenderableDefinition.Submesh.Builder().setName("proxy").setMaterial(material).setTriangleIndices(proxy.getTriangleIndices()).build());
//
//        RenderableDefinition renderableDefinition = RenderableDefinition.builder()
//                .setVertices(proxy.getVertices())
//                .setSubmeshes(subMeshes)
//                .build();
//        ModelRenderable.builder()
//                .setSource(renderableDefinition)
//                .build()
//                .thenAccept(modelRenderable -> {
//                    modelRenderable.setMaterial(material);
//                    // place model in scene
//                    Node node = new Node();
//                    node.setParent(environmentNode);
//                    node.setRenderable(modelRenderable);
//                    node.setLocalPosition(proxy.getPosition());
//                    node.setLocalRotation(proxy.getRotation());
//                    proxyNodes.add(node);
//                })
//                .exceptionally(throwable -> {
//                    throwable.printStackTrace();
//                    return null;
//                });
    }

    private void setupImageReferencePointRecognition(ARScene scene) {
        AsyncTask.execute(() -> {
            Session session = arFragment.getArSceneView().getSession();
            Config config = Objects.requireNonNull(session).getConfig();
            AugmentedImageDatabase augmentedImageDatabase = new AugmentedImageDatabase(session);
            Stream<ImageReferencePoint> referencePoints = scene.getSubScenes().stream()
                    .flatMap(subScene -> subScene.getEnvironment().getReferencePointsWithType(ImageReferencePoint.class).stream());
            referencePoints.forEach(referencePoint -> {
                String path = arSceneRepository.getImageReferencePointFile(scene, referencePoint).getPath();
                Log.d(TAG, "setupImageReferencePointRecognition: " + path);
                Bitmap augmentedImageBitmap = BitmapFactory.decodeFile(path);
                augmentedImageDatabase.addImage(referencePoint.getFileName(), augmentedImageBitmap);
            });
            config.setAugmentedImageDatabase(augmentedImageDatabase);
            session.configure(config);
            runOnUiThread(() -> {
                Toast.makeText(this, "Bildreferenzpunkte können nun erkannt werden", Toast.LENGTH_LONG).show();
            });
        });
    }

    private void imageReferencePointDetected(AugmentedImage image) {
        if (currentScene == null) {
            return;
        }

        ReferencePoint lastReferencePoint = currentReferencePoint;
        currentReferencePoint = currentScene.getImageReferencePoint(image.getName());
        Log.d(TAG, "imageReferencePointDetected: currentRefPoint:" + ((ImageReferencePoint) currentReferencePoint).getFileName());
        Toast.makeText(this, "Referenzpunkt: " + ((ImageReferencePoint) currentReferencePoint).getFileName(), Toast.LENGTH_SHORT).show();
        Pose currentReferencePointPoseLocal = image.getCenterPose();

        ARSubScene newReferencePointSubscene = currentReferencePoint.getEnvironment().getArSubScene();
        if (!currentSubScene.equals(newReferencePointSubscene)) {
            activateSubScene(newReferencePointSubscene);
        }

        if (!currentSubScene.getEnvironment().hasKnownReferencePointPosition()) {
            currentReferencePoint.setPositionKnown(true);
            arSceneRepository.saveARScene(currentScene);
        } else if (lastReferencePoint != null && !currentReferencePoint.isPositionKnown() && lastReferencePoint.isPositionKnown()) {
            Pose lastReferencePointPoseScene = PoseUtil.getPose(lastReferencePoint.getPosition(), lastReferencePoint.getRotation());
            Pose currentReferencePointPoseScene = lastReferencePointPoseScene.compose(lastReferencePointPoseLocal.inverse().compose(currentReferencePointPoseLocal));
            currentReferencePoint.setPosition(PoseUtil.getVector(currentReferencePointPoseScene));
            currentReferencePoint.setRotation(PoseUtil.getQuaternion(currentReferencePointPoseScene));
            currentReferencePoint.setPositionKnown(true);
            arSceneRepository.saveARScene(currentScene);
        }

        if (currentReferencePoint.isPositionKnown()) {
            Log.d(TAG, "imageReferencePointDetected: known position ref point: " + image.getName());
            if (referencePointAnchor != null) {
                referencePointAnchor.detach();
            }

            referencePointAnchor = arFragment.getArSceneView().getSession().createAnchor(currentReferencePointPoseLocal);
            anchorNode.setAnchor(referencePointAnchor);
            environmentNode.setLocalPosition(currentReferencePoint.getPosition().negated());
            environmentNode.setLocalRotation(currentReferencePoint.getRotation().inverted());

            AnchorNode andyAnchorNode = new AnchorNode(referencePointAnchor);
            andyAnchorNode.setParent(arFragment.getArSceneView().getScene());

            // Create the transformable node and add it to the anchor.
            Node andy = new Node();
            andy.setParent(andyAnchorNode);
            andy.setLocalPosition(currentReferencePoint.getPosition().negated());
            andy.setLocalRotation(currentReferencePoint.getRotation().inverted());
            andy.setRenderable(refpointRenderable);

            lastReferencePointPoseLocal = currentReferencePointPoseLocal;
        }

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

    public boolean checkPermissionForReadExtertalStorage() {
        int result = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    public void requestPermissionForReadExtertalStorage() {
        try {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    READ_STORAGE_PERMISSION_REQUEST_CODE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
