package de.hsrm.arocclusion;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ScenesListAdapter extends RecyclerView.Adapter<ScenesListAdapter.ScenesListViewHolder> {

    private List<String> scenes;
    private OnSceneSelectListener onSceneSelectListener;

    @NonNull
    @Override
    public ScenesListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ScenesListViewHolder(new ScenesListCellView(parent.getContext()));
    }

    @Override
    public void onBindViewHolder(@NonNull ScenesListViewHolder holder, int position) {
        holder.itemView.getCellText().setText(scenes.get(position));
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

    public OnSceneSelectListener getOnSceneSelectListener() {
        return onSceneSelectListener;
    }

    public void setOnSceneSelectListener(OnSceneSelectListener onSceneSelectListener) {
        this.onSceneSelectListener = onSceneSelectListener;
    }

    class ScenesListViewHolder extends RecyclerView.ViewHolder {

        ScenesListCellView itemView;

        public ScenesListViewHolder(ScenesListCellView view) {
            super(view);
            itemView = view;
        }
    }

    interface OnSceneSelectListener {
        void onSceneSelected(String sceneName);
    }


}
