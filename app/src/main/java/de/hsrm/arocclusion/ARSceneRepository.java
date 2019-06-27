package de.hsrm.arocclusion;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class ARSceneRepository {

    private static Gson gson = new GsonBuilder().create();
    private final Context context;
    private final File arSceneDir;

    public ARSceneRepository(Context context) {
        this.context = context;
        File externalFilesDir = context.getExternalFilesDir(null);
        arSceneDir = new File(externalFilesDir, "ar_scenes");
        arSceneDir.mkdirs();
    }

    public List<String> getARSceneNames() {
        return Arrays.asList(arSceneDir.list());
    }

    public ARScene getARScene(String name) throws IOException {
        // TODO: 24.06.2019 implement
        File sceneFile = getSceneFile(name);
        try (FileReader reader = new FileReader(sceneFile)) {
            return gson.fromJson(reader, ARScene.class);
        }
    }

    public void debugPrintJson(ARScene arScene) {
        Log.d(getClass().getSimpleName(), "AR Scene: " + gson.toJson(arScene));
    }

    public void saveARScene(ARScene scene) {
        // TODO: 24.06.2019 implement
        String json = gson.toJson(scene);
        File sceneFile = getSceneFile(scene);
        try (FileWriter writer = new FileWriter(sceneFile)) {
            writer.write(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteARScene(ARScene scene) {
        // TODO: 27.06.2019 implement
        File sceneFile = getSceneFile(scene);
        sceneFile.delete();
    }

    private File getSceneFile(ARScene scene) {
        return getSceneFile(scene.getName());
    }

    private File getSceneFile(String name) {
        return new File(arSceneDir, name);
    }
}
