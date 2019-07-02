package de.hsrm.arocclusion;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ScenesListAdapter extends RecyclerView.Adapter<ScenesListAdapter.ScenesListViewHolder> {

    private List<String> scenes;

    @NonNull
    @Override
    public ScenesListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.e(getClass().getSimpleName(), "onCreateViewHolder: HIER!");
        return new ScenesListViewHolder(new TextView(parent.getContext()));
    }

    @Override
    public void onBindViewHolder(@NonNull ScenesListViewHolder holder, int position) {
        ((TextView) (holder.itemView)).setText(scenes.get(position));
        Log.e(getClass().getSimpleName(), "onBindViewHolder: HIER!");
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
        Log.e(getClass().getSimpleName(), "setScenes: HIER! " + scenes.size());
    }

    class ScenesListViewHolder extends RecyclerView.ViewHolder {

        public ScenesListViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

}
