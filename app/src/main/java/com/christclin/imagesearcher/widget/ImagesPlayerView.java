package com.christclin.imagesearcher.widget;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.christclin.imagesearcher.R;

import java.util.Stack;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase;

public class ImagesPlayerView extends RelativeLayout implements ViewPager.OnPageChangeListener {
    private static final String TAG = ImagesPlayerView.class.getSimpleName();

    private View mRootView;
    private ViewPager mPager;
    private Toolbar mToolbar;

    private int mImageCount = 0;
    private OnImageListener mListener = null;

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        mToolbar.setTitle((position + 1) + " / " + mImageCount);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    public interface OnImageListener {
        void onImageShow(ImageView imageView, int position);
        void onImageSave(int position);
    }

    public ImagesPlayerView(Context context) {
        this(context, null, 0);
    }

    public ImagesPlayerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ImagesPlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mRootView = inflate(getContext(), R.layout.view_images_player, this);

        mPager = (ViewPager) mRootView.findViewById(R.id.pager_images);
        mPager.setOffscreenPageLimit(2);
        mPager.addOnPageChangeListener(this);
        mPager.setAdapter(new ImagesPagerAdapter());

        mToolbar = (Toolbar) mRootView.findViewById(R.id.toolbar);
        mToolbar.setNavigationIcon(R.drawable.ico_nav_back);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        mToolbar.inflateMenu(R.menu.view_images_player);
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_download:
                        Log.d(TAG, "download");
                        if (mListener != null) {
                            mListener.onImageSave(mPager.getCurrentItem());
                        }
                        return true;
                }
                return false;
            }
        });
    }

    public ImagesPlayerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        this(context, attrs, defStyleAttr);
    }

    public void show() {
        setVisibility(View.VISIBLE);
    }

    public void dismiss() {
        setVisibility(View.GONE);
    }

    public void setImageCount(int count) {
        mImageCount = count;
        mPager.getAdapter().notifyDataSetChanged();
    }

    public void setCurrentPosition(int position) {
        mToolbar.setTitle((position + 1) + " / " + mImageCount);
        mPager.setCurrentItem(position, false);
    }

    public void setOnImageShowListener(OnImageListener listener) {
        mListener = listener;
    }

    private class ImagesPagerAdapter extends PagerAdapter {

        private Stack<View> mViewHolderList = new Stack<>();

        @Override
        public int getCount() {
            return mImageCount;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            ViewHolder viewHolder;
            View view = null;
            if (mViewHolderList.size() > 0) {
                view = mViewHolderList.pop();
            }

            if (view == null) {
                view = LayoutInflater.from(getContext()).inflate(R.layout.pageritem_image, container, false);
                container.addView(view);
                viewHolder = new ViewHolder(view);
                view.setTag(viewHolder);
            } else {
                view.setVisibility(View.VISIBLE);
                viewHolder = (ViewHolder) view.getTag();
            }

            if (mListener != null) {
                mListener.onImageShow(viewHolder.imageView, position);
            }

            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object view) {
            if (!(view instanceof View)) return;

            View destroyView = (View) view;
            Object tag = destroyView.getTag();
            if (tag instanceof ViewHolder) {
                ((ViewHolder)tag).imageView.setImageDrawable(null);
            }
            mViewHolderList.push(destroyView);
            destroyView.setVisibility(View.INVISIBLE);
        }

        class ViewHolder {
            View rootView;
            ImageViewTouch imageView;

            ViewHolder(View view) {
                rootView = view;
                imageView = (ImageViewTouch) view.findViewById(R.id.image);
                imageView.setDisplayType(ImageViewTouchBase.DisplayType.FIT_TO_SCREEN);
            }
        }
    }
}
