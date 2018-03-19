package com.sada.blight;

/**
 * Created by Shantanu on 3/19/2018.
 */

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Shaan on 26-01-18.
 */

public final class QueryUtils {
    private static final String TAG = "QueryUtils";

    private QueryUtils() {
    }

    public static String fetchData(String requestUrl) throws IOException {
//        Log.i(TAG, "fetchData: CALLED");
        // Create URL object
        URL url = createUrl(requestUrl);

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
            Log.i(TAG, "fetchData: JSON RESPONSE FETCHED");
        } catch (IOException e) {
            Log.e(TAG, "Problem making the HTTP request.");
        }

        // Extract relevant fields from the JSON response and create a list of {@link News}s
//        String data = jsonResponse;//extractFeatureFromJson(jsonResponse);

        // Return the list of {@link News}s
        return jsonResponse;
    }

    private static URL createUrl(String stringUrl) {
//        Log.i(TAG, "createUrl: CALLED");
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(TAG, "Problem building the URL ", e);
        }
        return url;
    }

    private static String makeHttpRequest(URL url) throws IOException {
//        Log.i(TAG, "makeHttpRequest: CALLED");
        String jsonResponse = "";

        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(15000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(TAG, "Problem retrieving the news JSON results.");
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                // Closing the input stream could throw an IOException, which is why
                // the makeHttpRequest(URL url) method signature specifies than an IOException
                // could be thrown.
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    public static WeatherData extractFeatureFromJson(String dataJSON) {
//        Log.i(TAG, "extractFeatureFromJson: CALLED");
        // If the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty(dataJSON)) {
            return null;
        }

        // Create an empty ArrayList that we can start adding news to
        WeatherData weatherData = new WeatherData();
        // Try to parse the JSON response string. If there's a problem with the way the JSON
        // is formatted, a JSONException exception object will be thrown.
        // Catch the exception so the app doesn't crash, and print the error message to the logs.
        try {
            JSONObject root = new JSONObject(dataJSON);
            weatherData.setTemp(root.getString("temperature"));
            weatherData.setPressure(root.getString("pressure"));
            weatherData.setHumidity(root.getString("humidity"));
            weatherData.setVisibility(root.getString("visibility"));
            weatherData.setWind(root.getString("wind"));
            weatherData.setForTD1(root.getString("forecast_t_d1"));
            weatherData.setForTD2(root.getString("forecast_t_d2"));
            weatherData.setForTD3(root.getString("forecast_t_d3"));
            weatherData.setForTD4(root.getString("forecast_t_d4"));
            weatherData.setForTD5(root.getString("forecast_t_d5"));
            weatherData.setForTD6(root.getString("forecast_t_d6"));
            weatherData.setForPD1(root.getString("forecast_p_d1"));
            weatherData.setForPD2(root.getString("forecast_p_d2"));
            weatherData.setForPD3(root.getString("forecast_p_d3"));
            weatherData.setForPD4(root.getString("forecast_p_d4"));
            weatherData.setForPD5(root.getString("forecast_p_d5"));
            weatherData.setForPD6(root.getString("forecast_p_d6"));


        } catch (JSONException e) {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Log.e("QueryUtils", "Problem parsing the news JSON results", e);
        }

        // Return the list of news
        return weatherData;
    }
}