package com.christclin.imagesearcher.engine;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chris on 2017/7/1.
 */

public class FlickrEngine extends Engine {

    private static final String BASE_URL = "https://api.flickr.com/services/rest";
    private static final String KEY = "9f5524a5bf57ef94d5170e68cdabe486";
    private static final String URL_FORMAT = "https://farm%s.staticflickr.com/%s/%s_%s_%s.jpg";

    private static final int PER_PAGE = 20;

    private int mTotalPages = 0;
    private int mCurrentPage = 0;

    public FlickrEngine(Context context) {
        super(context);
    }

    @Override
    public void reset() {
        mTotalPages = 0;
        mCurrentPage = 0;
    }

    @Override
    public String getTitle() {
        return "Flickr";
    }

    @Override
    public boolean hasNext() {
        return mCurrentPage < mTotalPages;
    }

    @Override
    String getNextUrl() {
        Uri.Builder builder = Uri.parse(BASE_URL).buildUpon();
        builder.appendQueryParameter("method", "flickr.photos.search");
        builder.appendQueryParameter("api_key", KEY);
        builder.appendQueryParameter("format", "json");
        builder.appendQueryParameter("nojsoncallback", "1");
        builder.appendQueryParameter("per_page", String.valueOf(PER_PAGE));
        builder.appendQueryParameter("page", String.valueOf(mCurrentPage + 1));
        builder.appendQueryParameter("text", getKeyword());

        return builder.toString();
    }

    @Override
    List<Image> parseImages(String response) throws Exception {
        //Log.d("CHRIS", "response=" + response);
        JSONObject data = new JSONObject(response);
        data = data.getJSONObject("photos");
        mTotalPages = data.getInt("pages");
        mCurrentPage = data.getInt("page");
        JSONArray photos = data.getJSONArray("photo");

        List<Image> images = new ArrayList<>();
        for (int idx = 0; idx < photos.length(); idx++) {
            JSONObject photo = photos.getJSONObject(idx);
            String id = photo.getString("id");
            String secret = photo.getString("secret");
            String farm = photo.getString("farm");
            String server = photo.getString("server");
            images.add(new Image(
                    String.format(URL_FORMAT, farm, server, id, secret, "n"),
                    String.format(URL_FORMAT, farm, server, id, secret, "z"),
                    Image.WIDTH_NONE, Image.HEIGHT_NONE));
        }
        return images;
    }
}
