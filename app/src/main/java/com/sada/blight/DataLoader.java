package com.sada.blight;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import java.io.IOException;

/**
 * Created by Shantanu on 3/19/2018.
 */

class DataLoader extends AsyncTaskLoader<String> {
    private String queryURL;
    private static final String TAG = "DataLoader";

    public DataLoader(Context context, String query_url) {
        super(context);
        queryURL = query_url;
    }

    @Override
    protected void onStartLoading() {
//        Log.i(TAG, "onStartLoading: forceLoad : CALLED");
        forceLoad();
    }

    @Override
    public String loadInBackground() {
        if (queryURL == null) {
            return null;
        }

        // Perform the network request, parse the response, and extract a list of news.
        String data = null;
        try {
            data = QueryUtils.fetchData(queryURL);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "loadInBackground: PROBLEM FETCHING DATA : ", e);
        }
        return data;    }
}
