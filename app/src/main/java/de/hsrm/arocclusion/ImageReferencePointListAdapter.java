package de.hsrm.arocclusion;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ImageReferencePointListAdapter extends RecyclerView.Adapter<ImageReferencePointListAdapter.ScenesListViewHolder> {

    private List<ImageReferencePoint> referencePoints;
    private ImageReferencePointInteractionListener imageReferencePointInteractionListener;

    @NonNull
    @Override
    public ScenesListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ScenesListViewHolder(new ScenesListCellView(parent.getContext()));
    }

    @Override
    public void onBindViewHolder(@NonNull ScenesListViewHolder holder, int position) {
        holder.itemView.getCellText().setText(referencePoints.get(position).getFileName());
        holder.itemView.getCellText().setOnClickListener(v -> {
            if (imageReferencePointInteractionListener != null) {
                imageReferencePointInteractionListener.onImageReferencePointSelected(referencePoints.get(position));
            }
        });
        holder.itemView.getDeleteButton().setOnClickListener(v -> {
            if (imageReferencePointInteractionListener != null) {
                imageReferencePointInteractionListener.onImageReferencePointDeleted(referencePoints.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return referencePoints.size();
    }

    public List<ImageReferencePoint> getReferencePoints() {
        return referencePoints;
    }

    public void setReferencePoints(List<ImageReferencePoint> referencePoints) {
        this.referencePoints = referencePoints;
        notifyDataSetChanged();
    }

    public ImageReferencePointInteractionListener getImageReferencePointInteractionListener() {
        return imageReferencePointInteractionListener;
    }

    public void setImageReferencePointInteractionListener(ImageReferencePointInteractionListener onSceneSelectListener) {
        this.imageReferencePointInteractionListener = onSceneSelectListener;
    }

    class ScenesListViewHolder extends RecyclerView.ViewHolder {

        ScenesListCellView itemView;

        public ScenesListViewHolder(ScenesListCellView view) {
            super(view);
            itemView = view;
        }
    }

    interface ImageReferencePointInteractionListener {
        void onImageReferencePointSelected(ImageReferencePoint imageReferencePoint);

        void onImageReferencePointDeleted(ImageReferencePoint imageReferencePoint);
    }

}
