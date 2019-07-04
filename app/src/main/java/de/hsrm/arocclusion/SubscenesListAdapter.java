package de.hsrm.arocclusion;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SubscenesListAdapter extends RecyclerView.Adapter<SubscenesListAdapter.ScenesListViewHolder> {

    private List<ARSubScene> subScenes;
    private SubsceneInteractionListener subsceneInteractionListener;

    @NonNull
    @Override
    public ScenesListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ScenesListViewHolder(new ScenesListCellView(parent.getContext()));
    }

    @Override
    public void onBindViewHolder(@NonNull ScenesListViewHolder holder, int position) {
        holder.itemView.getCellText().setText(subScenes.get(position).getName());
        holder.itemView.getCellText().setOnClickListener(v -> {
            if (subsceneInteractionListener != null) {
                subsceneInteractionListener.onSubsceneSelected(subScenes.get(position));
            }
        });
        holder.itemView.getDeleteButton().setOnClickListener(v -> {
            if (subsceneInteractionListener != null) {
                subsceneInteractionListener.onSubsceneDeleted(subScenes.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return subScenes == null ? 0 : subScenes.size();
    }

    public List<ARSubScene> getSubScenes() {
        return subScenes;
    }

    public void setSubScenes(List<ARSubScene> subScenes) {
        this.subScenes = subScenes;
        notifyDataSetChanged();
    }

    public SubsceneInteractionListener getSubsceneInteractionListener() {
        return subsceneInteractionListener;
    }

    public void setSubsceneInteractionListener(SubsceneInteractionListener onSceneSelectListener) {
        this.subsceneInteractionListener = onSceneSelectListener;
    }

    class ScenesListViewHolder extends RecyclerView.ViewHolder {

        ScenesListCellView itemView;

        public ScenesListViewHolder(ScenesListCellView view) {
            super(view);
            itemView = view;
        }
    }

    interface SubsceneInteractionListener {
        void onSubsceneSelected(ARSubScene sceneName);

        void onSubsceneDeleted(ARSubScene sceneName);
    }

}
