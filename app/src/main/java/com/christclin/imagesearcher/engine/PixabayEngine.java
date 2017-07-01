package com.christclin.imagesearcher.engine;

import android.content.Context;
import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chris on 2017/7/1.
 */

public class PixabayEngine extends Engine {

    private static final String BASE_URL = "https://pixabay.com/api";
    private static final String KEY = "5779570-3497cbcd5d706a5741d7f66ae";

    private static final int PER_PAGE = 20;

    private int mTotalHits = 0;
    private int mCurrentHits = 0;
    private int mCurrentPage = 0;


    public PixabayEngine(Context context) {
        super(context);
    }

    @Override
    public void reset() {
        mTotalHits = 0;
        mCurrentHits = 0;
        mCurrentPage = 0;
    }

    @Override
    public String getTitle() {
        return "Pixabay";
    }

    @Override
    public boolean hasNext() {
        return (mCurrentPage - 1) * PER_PAGE + mCurrentHits < mTotalHits;
    }

    @Override
    String getNextUrl() {
        Uri.Builder builder = Uri.parse(BASE_URL).buildUpon();
        builder.appendQueryParameter("key", KEY);
        builder.appendQueryParameter("image_type", "photo");
        builder.appendQueryParameter("per_page", String.valueOf(PER_PAGE));
        builder.appendQueryParameter("page", String.valueOf(mCurrentPage + 1));
        builder.appendQueryParameter("q", getKeyword());

        return builder.toString();
    }

    @Override
    List<Image> parseImages(String response) throws Exception {
        JSONObject data = new JSONObject(response);
        mTotalHits = data.getInt("totalHits");
        JSONArray hits = data.getJSONArray("hits");
        mCurrentHits = hits.length();
        mCurrentPage++;

        List<Image> images = new ArrayList<>();
        for (int idx = 0; idx < mCurrentHits; idx++) {
            images.add(new Image(hits.getJSONObject(idx).getString("previewURL"),
                    hits.getJSONObject(idx).getString("webformatURL"),
                    hits.getJSONObject(idx).getInt("webformatWidth"),
                    hits.getJSONObject(idx).getInt("webformatHeight")));
        }
        return images;
    }
}
