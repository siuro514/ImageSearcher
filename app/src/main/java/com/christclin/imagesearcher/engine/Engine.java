package com.christclin.imagesearcher.engine;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.List;

/**
 * Created by chris on 2017/7/1.
 */

public abstract class Engine {
    private static final String TAG = Engine.class.getSimpleName();

    private RequestQueue mRequestQueue = null;

    private String mKeyword = null;

    public interface OnSearchListener {
        void onSuccess(List<Image> images);
        void onError();
    }

    public class Image {
        static final int WIDTH_NONE = -1;
        static final int HEIGHT_NONE = -1;

        private String thumb;
        private String url;
        private int width;
        private int height;

        Image(String thumb, String url, int width, int height) {
            this.thumb = thumb;
            this.url = url;
            this.width = width;
            this.height = height;
        }

        public String getThumb() {
            return thumb;
        }

        public String getUrl() {
            return url;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }
    }

    public Engine(Context context) {
        mRequestQueue = Volley.newRequestQueue(context);
    }

    abstract public void reset();
    abstract public String getTitle();
    abstract public boolean hasNext();
    abstract String getNextUrl();
    abstract List<Image> parseImages(String response) throws Exception;

    public String getKeyword() {
        return mKeyword;
    }

    public void setKeyword(String keyword) {
        mKeyword = keyword;
    }

    public void searchNext(final OnSearchListener listener) {
        Log.d(TAG, "searchNext, url=" + getNextUrl() + ", listener=" + listener);
        StringRequest request = new StringRequest(Request.Method.GET, getNextUrl(), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    if (listener != null) {
                        listener.onSuccess(parseImages(response));
                    }
                } catch (Exception e) {
                    //e.printStackTrace();
                    if (listener != null) {
                        listener.onError();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.w(TAG, "onErrorResponse, res code=" + error.networkResponse.statusCode);
                if (listener != null) {
                    listener.onError();
                }
            }
        });
        mRequestQueue.add(request);
    }
}
