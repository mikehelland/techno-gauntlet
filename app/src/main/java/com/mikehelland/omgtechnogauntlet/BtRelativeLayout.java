package com.mikehelland.omgtechnogauntlet;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * Created by m on 1/25/18.
 */

public class BtRelativeLayout extends RelativeLayout {

    private boolean mShowDetails = false;
    boolean getShowDetails() {return mShowDetails;}
    void setShowDetails(boolean showDetails) {
        mShowDetails = showDetails;
    }

    public BtRelativeLayout(Context context) {
        super(context);
    }

    public BtRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BtRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

}
