package de.hsrm.arocclusion;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.util.Consumer;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ScenesView extends ConstraintLayout {

    @BindView(R.id.scenes_list)
    RecyclerView scenesList;

    @BindView(R.id.new_scene_button)
    Button newSceneButton;

    @BindView(R.id.subscenes_list)
    RecyclerView subScenesList;

    @BindView(R.id.new_subscene_button)
    Button newSubsceneButton;

    private ScenesListAdapter scenesListAdapter = new ScenesListAdapter();
    private SubscenesListAdapter subscenesListAdapter = new SubscenesListAdapter();
    private ARSceneRepository arSceneRepository;
    private ScenesViewCallback scenesViewCallback;
    private ARScene currentScene;

    public ScenesView(Context context) {
        this(context, null);
    }

    public ScenesView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScenesView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.view_scenes, this, true);
        ButterKnife.bind(this);
        scenesList.setAdapter(scenesListAdapter);
        scenesListAdapter.setSceneInteractionListener(new ScenesListAdapter.SceneInteractionListener() {
            @Override
            public void onSceneSelected(String sceneName) {
                if (scenesViewCallback != null) {
                    try {
                        currentScene = arSceneRepository.getARScene(sceneName);
                        refreshSubscenes();
                        scenesViewCallback.onSceneSelect(currentScene);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onSceneDeleted(String sceneName) {
                arSceneRepository.deleteARScene(sceneName);
                refreshScenes();
            }
        });
        subScenesList.setAdapter(subscenesListAdapter);
        subscenesListAdapter.setSubsceneInteractionListener(new SubscenesListAdapter.SubsceneInteractionListener() {
            @Override
            public void onSubsceneSelected(ARSubScene scene) {
                if (scenesViewCallback != null) {
                    scenesViewCallback.onSubsceneSelect(scene);
                }
            }

            @Override
            public void onSubsceneDeleted(ARSubScene scene) {
                if (currentScene != null) {
                    currentScene.getSubScenes().remove(scene);
                    refreshSubscenes();
                }
            }
        });
    }

    private void refreshSubscenes() {
        if (currentScene != null) {
            subscenesListAdapter.setSubScenes(currentScene.getSubScenes());
        }
    }

    @OnClick(R.id.new_scene_button)
    void onNewSceneButtonClick(View view) {
        stringDialog("Name der neuen Szene", sceneName -> {
            Toast.makeText(getContext(), MessageFormat.format("Szene erstellt: {0}", sceneName), Toast.LENGTH_LONG).show();
            ARScene arScene = arSceneRepository.newARScene(sceneName);
            arSceneRepository.saveARScene(arScene);
            refreshScenes();
        });
    }

    @OnClick(R.id.new_subscene_button)
    void onNewSubsceneButtonClick(View view) {
        if (currentScene != null) {
            stringDialog("Name der neuen Subszene", subsceneName -> {
                Toast.makeText(getContext(), MessageFormat.format("Subszene erstellt: {0}", subsceneName), Toast.LENGTH_LONG).show();
                currentScene.getSubScenes().add(new ARSubScene(subsceneName));
                arSceneRepository.saveARScene(currentScene);
                refreshSubscenes();
            });
        }
    }

    private void stringDialog(String title, Consumer<String> callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(title);
        final EditText input = new EditText(getContext());
        input.requestFocus();
        builder.setView(input);
        builder.setPositiveButton("OK", (dialog, which) -> callback.accept(input.getText().toString()));
        builder.setNegativeButton("Abbrechen", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    public void setScenes(List<String> scenes) {
        scenesListAdapter.setScenes(scenes);
    }

    public ARSceneRepository getArSceneRepository() {
        return arSceneRepository;
    }

    public void setArSceneRepository(ARSceneRepository arSceneRepository) {
        this.arSceneRepository = arSceneRepository;
        refreshScenes();
    }

    private void refreshScenes() {
        setScenes(arSceneRepository.getARSceneNames());
    }

    public ScenesViewCallback getScenesViewCallback() {
        return scenesViewCallback;
    }

    public void setScenesViewCallback(ScenesViewCallback scenesViewCallback) {
        this.scenesViewCallback = scenesViewCallback;
    }

    public ARScene getCurrentScene() {
        return currentScene;
    }

    interface ScenesViewCallback {
        void onSceneSelect(ARScene scene);

        void onSubsceneSelect(ARSubScene subScene);
    }

}
