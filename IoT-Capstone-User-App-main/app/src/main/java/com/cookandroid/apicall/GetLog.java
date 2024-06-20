package com.cookandroid.apicall;

import android.util.Log;
import android.widget.TextView;

import com.cookandroid.userapp.MainActivity;
import com.cookandroid.userapp.datalist;
import com.cookandroid.httpconnection.GetRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class GetLog extends GetRequest {
    final static String TAG = "AndroidAPITest";

    String urlStr;
    private MainActivity mainActivity;
    public static JSONArray jsonarrayforsearch;


    public GetLog(MainActivity activity, String urlStr) {
        super(activity);
        this.urlStr = urlStr;
        this.mainActivity = activity;
    }

    @Override
    protected void onPreExecute() {
        try {
            Log.i(TAG, "urlStr=" + urlStr);
            url = new URL(urlStr);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onPostExecute(String jsonString) {
        if (jsonString == null) {
            mainActivity.updateList(new ArrayList<>());
            return;
        }
        ArrayList<datalist> arrayList = getArrayListFromJSONString(jsonString);
        mainActivity.updateList(arrayList);
    }

    protected ArrayList<datalist> getArrayListFromJSONString(String jsonString) {
        ArrayList<datalist> output = new ArrayList<>();
        try {
            jsonString = jsonString.substring(1,jsonString.length()-1);
            jsonString = jsonString.replace("\\\"","\"");

            JSONObject root = new JSONObject(jsonString);

            JSONArray jsonArray = root.getJSONArray("data");
            jsonarrayforsearch = jsonArray;

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                datalist Datalist = new datalist();
                Datalist.setRecposition(jsonObject.getString("deviceId"));
                Datalist.recuid = jsonObject.getString("uid");
                Datalist.rectime = jsonObject.getInt("time");

                output.add(Datalist);
            }

        } catch (JSONException e) {
            Log.e(TAG, "Exception in processing JSONString.", e);
            e.printStackTrace();
        }
        return output;
    }
}
