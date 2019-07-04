package de.hsrm.arocclusion;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ScenesListAdapter extends RecyclerView.Adapter<ScenesListAdapter.ScenesListViewHolder> {

    private List<String> scenes;
    private SceneInteractionListener sceneInteractionListener;

    @NonNull
    @Override
    public ScenesListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ScenesListViewHolder(new ScenesListCellView(parent.getContext()));
    }

    @Override
    public void onBindViewHolder(@NonNull ScenesListViewHolder holder, int position) {
        holder.itemView.getCellText().setText(scenes.get(position));
        holder.itemView.getCellText().setOnClickListener(v -> {
            if (sceneInteractionListener != null) {
                sceneInteractionListener.onSceneSelected(scenes.get(position));
            }
        });
        holder.itemView.getDeleteButton().setOnClickListener(v -> {
            if (sceneInteractionListener != null) {
                sceneInteractionListener.onSceneDeleted(scenes.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return scenes.size();
    }

    public List<String> getScenes() {
        return scenes;
    }

    public void setScenes(List<String> scenes) {
        this.scenes = scenes;
        notifyDataSetChanged();
    }

    public SceneInteractionListener getSceneInteractionListener() {
        return sceneInteractionListener;
    }

    public void setSceneInteractionListener(SceneInteractionListener onSceneSelectListener) {
        this.sceneInteractionListener = onSceneSelectListener;
    }

    class ScenesListViewHolder extends RecyclerView.ViewHolder {

        ScenesListCellView itemView;

        public ScenesListViewHolder(ScenesListCellView view) {
            super(view);
            itemView = view;
        }
    }

    interface SceneInteractionListener {
        void onSceneSelected(String sceneName);

        void onSceneDeleted(String sceneName);
    }

}
