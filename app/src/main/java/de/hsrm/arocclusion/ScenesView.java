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

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ScenesView extends ConstraintLayout {

    @BindView(R.id.scenes_list)
    RecyclerView scenesList;

    @BindView(R.id.new_scene_button)
    Button newSceneButton;

    private ScenesListAdapter scenesListAdapter = new ScenesListAdapter();
    private ARSceneRepository arSceneRepository;
    private ScenesViewCallback scenesViewCallback;

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
                    scenesViewCallback.onSceneSelect(sceneName);
                }
            }

            @Override
            public void onSceneDeleted(String sceneName) {
                arSceneRepository.deleteARScene(sceneName);
                refresh();
            }
        });
    }

    @OnClick(R.id.new_scene_button)
    void onNewSceneButtonClick(View view) {
        getSceneName(sceneName -> {
            Toast.makeText(getContext(), sceneName, Toast.LENGTH_LONG).show();
            ARScene arScene = arSceneRepository.newARScene(sceneName);
            arSceneRepository.saveARScene(arScene);
            refresh();
        });
    }

    private void getSceneName(Consumer<String> callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Name der neuen Szene");
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
        refresh();
    }

    private void refresh() {
        setScenes(arSceneRepository.getARSceneNames());
    }

    public ScenesViewCallback getScenesViewCallback() {
        return scenesViewCallback;
    }

    public void setScenesViewCallback(ScenesViewCallback scenesViewCallback) {
        this.scenesViewCallback = scenesViewCallback;
    }

    interface ScenesViewCallback {
        void onSceneSelect(String scene);
    }

}
