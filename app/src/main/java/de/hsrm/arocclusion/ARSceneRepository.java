package de.hsrm.arocclusion;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;

public class ARSceneRepository {

    private static Gson gson = new GsonBuilder().create();
    private final Context context;

    public ARSceneRepository(Context context) {
        this.context = context;
    }

    public List<String> getARSceneNames() {
        // TODO: 24.06.2019 implement
        return null;
    }

    public ARScene getARScene(String name) {
        // TODO: 24.06.2019 implement
        ARScene arScene = new ARScene();
        arScene.setName(name);
        arScene.getSubScenes().add(new ARSubScene());
        return arScene;
    }

    public void debugPrintJson(ARScene arScene) {
        Log.d(getClass().getSimpleName(), "AR Scene: " + gson.toJson(arScene));
    }

    public void saveARScene(ARScene scene) {
        // TODO: 24.06.2019 implement
    }
}
