package com.christclin.imagesearcher.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;

/**
 * Created by chris on 2017/7/1.
 */

public class MyImageView extends android.support.v7.widget.AppCompatImageView {

    private float mAspectRatio = 1f;

    public MyImageView(Context context) {
        super(context);
    }

    public MyImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MyImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setAspectRatio(float ratio) {
        mAspectRatio = ratio;
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = getMeasuredWidth();
        //Log.d("CHRIS", "w=" + width + ", h=" + (int)(width * mAspectRatio));
        setMeasuredDimension(width, (int)(width / mAspectRatio));
    }
}
