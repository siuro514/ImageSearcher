package com.christclin.imagesearcher.widget;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.christclin.imagesearcher.R;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase;

public class PhotoViewerActivity extends AppCompatActivity {
    private static final String CLASSNAME = PhotoViewerActivity.class.getSimpleName();
    public static final String ARG_URL = "url";

    private String mUrl = "";
    private ProgressDialog mProgressDialog;
    private ImageViewTouch mImageView;

    public static void launch(Context context, String url) {
        if (context == null || TextUtils.isEmpty(url)) return;

        Intent intent = new Intent(context,  PhotoViewerActivity.class);
        intent.putExtra(ARG_URL, url);
        context.startActivity(intent);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent != null) {
            mUrl = intent.getStringExtra(ARG_URL);
            Log.d(CLASSNAME, "onCreate, mUrl=" + mUrl);
        }

        setContentView(R.layout.activity_image_viewer);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_keyboard_arrow_left_white);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("");

        mImageView = (ImageViewTouch) findViewById(R.id.image);
        mImageView.setDisplayType(ImageViewTouchBase.DisplayType.FIT_TO_SCREEN);
        //Glide.with(this).load(mUrl).into(mImageView);
        Glide.with(this).load(mUrl)
                .fitCenter()
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(mImageView);
    }
}
