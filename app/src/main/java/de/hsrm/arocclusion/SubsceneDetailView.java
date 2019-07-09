package de.hsrm.arocclusion;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hsrm.arocclusion.util.ActivityUtil;
import de.hsrm.arocclusion.util.FileUtil;

import static de.hsrm.arocclusion.MainActivity.ACTIVITY_SELECT_IMAGE;

public class SubsceneDetailView extends ConstraintLayout {

    @BindView(R.id.image_reference_points_list)
    RecyclerView imageReferencePointsList;

    private ImageReferencePointListAdapter imageReferencePointListAdapter = new ImageReferencePointListAdapter();
    private ARSceneRepository arSceneRepository;
    private ARSubScene currentSubScene;

    public SubsceneDetailView(Context context) {
        this(context, null);
    }

    public SubsceneDetailView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SubsceneDetailView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.view_subscene_detail, this, true);
        ButterKnife.bind(this);
        setBackgroundColor(Color.parseColor("#1A1A1A"));
        imageReferencePointsList.setAdapter(imageReferencePointListAdapter);
        imageReferencePointListAdapter.setImageReferencePointInteractionListener(new ImageReferencePointListAdapter.ImageReferencePointInteractionListener() {
            @Override
            public void onImageReferencePointSelected(ImageReferencePoint imageReferencePoint) {

            }

            @Override
            public void onImageReferencePointDeleted(ImageReferencePoint imageReferencePoint) {

            }
        });
    }

    @OnClick(R.id.add_image_reference_point_button)
    void onAddImageReferencePointButtonClick(View v) {
        Intent i = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        ActivityUtil.getActivity(this).startActivityForResult(i, ACTIVITY_SELECT_IMAGE);
    }

    public void addImageReferencePoint(String fileName) {
        ARScene scene = currentSubScene.getParent();

        // copy the image file
        File src = new File(fileName);
        File dst = new File(arSceneRepository.getReferenceImageDirectory(scene.getName()), src.getName());
        try {
            FileUtil.copy(src, dst);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        ImageReferencePoint referencePoint = new ImageReferencePoint();
        referencePoint.setFileName(src.getName());
        currentSubScene.getEnvironment().getReferencePoints().add(referencePoint);
        arSceneRepository.saveARScene(scene);
        refreshReferencePointList();
    }

    private void refreshReferencePointList() {
        imageReferencePointListAdapter.setReferencePoints(currentSubScene.getEnvironment().getReferencePointsWithType(ImageReferencePoint.class));
    }

    public ARSceneRepository getArSceneRepository() {
        return arSceneRepository;
    }

    public void setArSceneRepository(ARSceneRepository arSceneRepository) {
        this.arSceneRepository = arSceneRepository;
    }

    public ARSubScene getCurrentSubScene() {
        return currentSubScene;
    }

    public void setCurrentSubScene(ARSubScene currentSubScene) {
        this.currentSubScene = currentSubScene;
        refreshReferencePointList();
    }
}
