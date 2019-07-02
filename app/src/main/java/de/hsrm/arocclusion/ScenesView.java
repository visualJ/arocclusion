package de.hsrm.arocclusion;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ScenesView extends ConstraintLayout {

    @BindView(R.id.scenes_list)
    RecyclerView scenes_list;

    private ScenesListAdapter scenesListAdapter = new ScenesListAdapter();

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
        scenes_list.setAdapter(scenesListAdapter);
    }

    public void setScenes(List<String> scenes) {
        scenesListAdapter.setScenes(scenes);
    }
}
