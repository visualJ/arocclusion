package de.hsrm.arocclusion;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

public class ARSceneRepository {

    private static Gson gson = new GsonBuilder().registerTypeHierarchyAdapter(ReferencePoint.class, new ReferencePointDeserializer()).create();
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
        File sceneFile = getSceneFile(name);
        try (FileReader reader = new FileReader(sceneFile)) {
            return gson.fromJson(reader, ARScene.class);
        }
    }

    public void debugPrintJson(ARScene arScene) {
        Log.d(getClass().getSimpleName(), "AR Scene: " + gson.toJson(arScene));
    }

    public void saveARScene(ARScene scene) {
        String json = gson.toJson(scene);
        File sceneFile = getSceneFile(scene);
        try (FileWriter writer = new FileWriter(sceneFile)) {
            writer.write(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteARScene(ARScene scene) {
        File sceneFile = getSceneFile(scene);
        sceneFile.delete();
    }

    public void deleteARScene(String name) {
        File sceneFile = getSceneFile(name);
        sceneFile.delete();
    }

    public ARScene newARScene(String name) {
        ARScene arScene = new ARScene();
        arScene.setName(name);
        ARSubScene subScene = new ARSubScene();
        arScene.getSubScenes().add(subScene);
        return arScene;
    }

    private File getSceneFile(ARScene scene) {
        return getSceneFile(scene.getName());
    }

    private File getSceneFile(String name) {
        return new File(arSceneDir, name);
    }

    private static class ReferencePointDeserializer implements JsonDeserializer<ReferencePoint> {

        @Override
        public ReferencePoint deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            String type = json.getAsJsonObject().get("type").getAsString();
            switch (type) {
                case ImageReferencePoint.TYPE:
                    return gson.fromJson(json, ImageReferencePoint.class);
            }
            return null;
        }
    }
}
