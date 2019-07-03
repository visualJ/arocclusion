package de.hsrm.arocclusion;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Constraints;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ScenesListCellView extends ConstraintLayout {

    @BindView(R.id.cell_text)
    TextView cellText;
    @BindView(R.id.delete_button)
    Button deleteButton;

    public ScenesListCellView(Context context) {
        this(context, null);
    }

    public ScenesListCellView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScenesListCellView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.view_scenes_list_cell, this, true);
        setLayoutParams(new Constraints.LayoutParams(Constraints.LayoutParams.MATCH_PARENT, Constraints.LayoutParams.WRAP_CONTENT));
        ButterKnife.bind(this);
    }

    public Button getDeleteButton() {
        return deleteButton;
    }

    public TextView getCellText() {
        return cellText;
    }
}
