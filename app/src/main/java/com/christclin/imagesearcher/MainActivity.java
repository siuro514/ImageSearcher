package com.christclin.imagesearcher;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.christclin.imagesearcher.engine.Engine;
import com.christclin.imagesearcher.engine.FlickrEngine;
import com.christclin.imagesearcher.engine.PixabayEngine;
import com.christclin.imagesearcher.widget.BaseActivity;
import com.christclin.imagesearcher.widget.ImagesPlayerView;
import com.christclin.imagesearcher.widget.TextAdapter;
import com.christclin.imagesearcher.widget.MyImageView;
import com.christclin.imagesearcher.widget.SpaceItemDecoration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private AppCompatSpinner mSpinnerEngine = null;
    private EditText mEditKeyword = null;
    private Button mBtnSearch = null;

    private RecyclerView mRecyclerView = null;
    private ImageGridAdapter mImageAdapter = null;

    private ImagesPlayerView mImagesPlayer = null;

    private ItemTouchHelper.Callback mItemTouchHelperCallback = new ItemTouchHelper.Callback() {
        @Override
        public boolean isLongPressDragEnabled() {
            return true;
        }

        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN |
                    ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
            int swipeFlags = 0;
            return makeMovementFlags(dragFlags, swipeFlags);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            if (viewHolder.getItemViewType() != target.getItemViewType()) {
                return false;
            }
            mImageAdapter.moveImage(viewHolder.getAdapterPosition(), target.getAdapterPosition());
            return true;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

        }
    };
    private ItemTouchHelper mItemTouchHelper = null;

    private TextView mEmptyView = null;
    private ProgressBar mLoadingView = null;

    private List<Engine> mSupportedEngines = new ArrayList<>();
    private Engine mCurrentEngine = null;

    private Engine.OnSearchListener mOnSearchListener = new Engine.OnSearchListener() {
        @Override
        public void onSuccess(List<Engine.Image> images) {
            Log.d(TAG, "onSuccess, images size " + images.size());
            mImageAdapter.addImages(images);
            setLoading(false);
        }

        @Override
        public void onError() {
            setLoading(false);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_view_type:
                StaggeredGridLayoutManager lm = (StaggeredGridLayoutManager) mRecyclerView.getLayoutManager();
                if (lm.getSpanCount() > 1) {
                    lm.setSpanCount(1);
                    item.setIcon(R.drawable.ic_view_grid);
                } else {
                    lm.setSpanCount(2);
                    item.setIcon(R.drawable.ic_view_list);
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setTitle(R.string.title_engine);
        setSupportActionBar(toolbar);

        mEmptyView = (TextView) findViewById(R.id.empty);
        mLoadingView = (ProgressBar) findViewById(R.id.loading);

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        mRecyclerView.addItemDecoration(new SpaceItemDecoration(getResources().getDimensionPixelSize(R.dimen.item_space_margin)));
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (isScrollToBottom() && mCurrentEngine.hasNext()) {
                        mCurrentEngine.searchNext(mOnSearchListener);
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

            }
        });

        mImageAdapter = new ImageGridAdapter();
        mRecyclerView.setAdapter(mImageAdapter);

        // set touch helper for dragging
        mItemTouchHelper = new ItemTouchHelper(mItemTouchHelperCallback);
        mItemTouchHelper.attachToRecyclerView(mRecyclerView);

        // set for images player
        mImagesPlayer = (ImagesPlayerView) findViewById(R.id.images_player);
        mImagesPlayer.setOnImageShowListener(new ImagesPlayerView.OnImageListener() {
            @Override
            public void onImageShow(ImageView imageView, int position) {
                Engine.Image image = mImageAdapter.getImage(position);
                Glide.with(MainActivity.this).load(image.getUrl()).placeholder(R.drawable.placeholder).into(imageView);
            }

            @Override
            public void onImageSave(final int position) {
                Log.d(TAG, "onImageSave, position=" + position);
                if (checkPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, new OnPermissionListener() {
                    @Override
                    public void onPermissionGranted() {
                        saveImage(position);
                    }

                    @Override
                    public void onPermissionDenied() {

                    }
                })) {
                    saveImage(position);
                }
            }
        });

        // set default supported engines;
        mSupportedEngines.add(new PixabayEngine(this));
        mSupportedEngines.add(new FlickrEngine(this));
        mCurrentEngine = mSupportedEngines.get(0);

        mEditKeyword = (EditText) findViewById(R.id.edittext_keyword);
        mBtnSearch = (Button) findViewById(R.id.btn_search);
        mBtnSearch.setOnClickListener(this);

        TextAdapter engineAdapter = new TextAdapter(this);
        engineAdapter.setTextColor(ContextCompat.getColor(this, R.color.search_btn_border));
        for (int idx = 0; idx < mSupportedEngines.size(); idx++) {
            engineAdapter.addText(mSupportedEngines.get(idx).getTitle());
        }
        mSpinnerEngine = (AppCompatSpinner) findViewById(R.id.spinner_engine);
        mSpinnerEngine.setAdapter(engineAdapter);
        mSpinnerEngine.setSelection(0, false);
        mSpinnerEngine.setOnItemSelectedListener(this);
    }

    private void saveImage(int position) {
        Log.d(TAG, "saveImage, position=" + position);
        Engine.Image image = mImageAdapter.getImage(position);
        SimpleTarget target = new SimpleTarget<Bitmap>(SimpleTarget.SIZE_ORIGINAL, SimpleTarget.SIZE_ORIGINAL) {
            @Override
            public void onLoadFailed(Exception e, Drawable errorDrawable) {
                Snackbar.make(mImagesPlayer, R.string.msg_download_error, Snackbar.LENGTH_SHORT).show();
            }

            @Override
            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                Log.d(TAG, "onResourceReady, resource=" + resource);
                if (saveBitmapToFile(resource)) {
                    Snackbar.make(mImagesPlayer, R.string.msg_download_success, Snackbar.LENGTH_SHORT).show();
                } else {
                    Snackbar.make(mImagesPlayer, R.string.msg_download_error, Snackbar.LENGTH_SHORT).show();
                }
            }
        };
        Glide.with(MainActivity.this).load(image.getUrl()).asBitmap().into(target);
    }

    private boolean saveBitmapToFile(Bitmap bitmap) {
        FileOutputStream os = null;
        try {
            File file = Environment.getExternalStoragePublicDirectory("Pictures");
            if (file == null) {
                return false;
            }
            file = new File(file, "ImageSearcher");
            file.mkdirs();
            file = new File(file, System.currentTimeMillis() + ".jpg");
            os = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
            os.close();

            // trigger media scanner to scan new file
            Uri uri = Uri.fromFile(file);
            Intent scanFileIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri);
            sendBroadcast(scanFileIntent);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    //e.printStackTrace();
                }
            }
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_search:
                hideKeyboard();
                setLoading(true);
                mImageAdapter.clearImages();
                mCurrentEngine.reset();
                mCurrentEngine.setKeyword(mEditKeyword.getText().toString());
                mCurrentEngine.searchNext(mOnSearchListener);
                break;
        }
    }

    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.spinner_engine:
                mCurrentEngine = mSupportedEngines.get(position);
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    class ImageGridAdapter extends RecyclerView.Adapter<ImageGridAdapter.ViewHolder> {

        private List<Engine.Image> mImages = new ArrayList<>();

        public void moveImage(int fromIdx, int toIdx) {
            Engine.Image image = mImages.get(fromIdx);
            mImages.remove(fromIdx);
            mImages.add(toIdx, image);
            notifyItemMoved(fromIdx, toIdx);
        }

        public Engine.Image getImage(int position) {
            return mImages.get(position);
        }

        public void clearImages() {
            mImages.clear();
            notifyDataSetChanged();
        }

        public void addImages(List<Engine.Image> images) {
            mImages.addAll(images);
            notifyDataSetChanged();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Log.d(TAG, "onCreateViewHolder");
            return new ViewHolder(LayoutInflater.from(MainActivity.this).inflate(R.layout.griditem_image, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Log.d(TAG, "onBindViewHolder, position=" + position);
            Engine.Image image = mImages.get(position);
            if (image.getWidth() > 0 && image.getHeight() > 0) {
                holder.imageView.setAspectRatio((float)image.getWidth() / (float)image.getHeight());
            } else {
                holder.imageView.setAspectRatio(1f);
            }
            Glide.with(MainActivity.this).load(image.getThumb())
                    .placeholder(R.drawable.placeholder)
                    .into(holder.imageView);
        }

        @Override
        public int getItemCount() {
            return mImages != null ? mImages.size() : 0;
        }

        class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            View rootView;
            MyImageView imageView;

            ViewHolder(View view) {
                super(view);
                rootView = view;
                imageView = (MyImageView) view.findViewById(R.id.image);
                imageView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                int position = getAdapterPosition();
                mImagesPlayer.setImageCount(getItemCount());
                mImagesPlayer.setCurrentPosition(position);
                mImagesPlayer.show();
            }
        }
    }

    private void setLoading(boolean loading) {
        if (loading) {
            mLoadingView.setVisibility(View.VISIBLE);
            mEmptyView.setVisibility(View.GONE);
        } else {
            mLoadingView.setVisibility(View.GONE);
            mEmptyView.setVisibility(mImageAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
        }
    }

    private boolean isScrollToBottom() {
        return !mRecyclerView.canScrollVertically(1);
    }

    @Override
    public void onBackPressed() {
        if (mImagesPlayer.getVisibility() == View.VISIBLE) {
            mImagesPlayer.dismiss();
        } else {
            super.onBackPressed();
        }
    }
}
