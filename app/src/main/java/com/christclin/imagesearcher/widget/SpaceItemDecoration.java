package com.christclin.imagesearcher.widget;

import android.graphics.Rect;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;


public class SpaceItemDecoration extends RecyclerView.ItemDecoration{
    private int mLeft;
    private int mTop;
    private int mRight;
    private int mBottom;

    private boolean mHeaderSpaceEnabled = true;

    public SpaceItemDecoration(int space) {
        this.mLeft = this.mTop = this.mRight = this.mBottom = space;
    }

    public SpaceItemDecoration(int left, int top, int right, int bottom) {
        this.mLeft = left;
        this.mTop = top;
        this.mRight = right;
        this.mBottom = bottom;
    }

    public void setHeaderSpaceEnabled(boolean headerSpaceEnabled) {
        this.mHeaderSpaceEnabled = headerSpaceEnabled;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.left = mLeft;
        outRect.right = mRight;
        outRect.bottom = mBottom;

        if (mHeaderSpaceEnabled) {
            outRect.top = mTop;
        } else {
            int position = parent.getChildLayoutPosition(view);
            if (parent.getLayoutManager() instanceof GridLayoutManager) {
                GridLayoutManager manager = (GridLayoutManager) parent.getLayoutManager();
                if (position >= manager.getSpanCount()) {
                    outRect.top = mTop;
                }
            } else if (parent.getLayoutManager() instanceof StaggeredGridLayoutManager) {
                StaggeredGridLayoutManager manager = (StaggeredGridLayoutManager) parent.getLayoutManager();
                if (position >= manager.getSpanCount()) {
                    outRect.top = mTop;
                }
            } else {
                if (position > 0) {
                    outRect.top = mTop;
                }
            }
        }
    }
}
