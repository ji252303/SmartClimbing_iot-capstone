package com.cookandroid.apicall;

import android.app.Activity;
import android.util.Log;

import com.cookandroid.userapp.MainActivity;
import com.cookandroid.userapp.datalist;
import com.cookandroid.httpconnection.GetRequest;
import com.cookandroid.userapp.mapdatalist;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class GetRoot extends GetRequest {
    final static String TAG = "AndroidAPITest";
    String urlStr;
    private MainActivity mainActivity;
    public List<datalist> recordlist;
    public static ArrayList<mapdatalist> marrayList;


    public GetRoot(MainActivity activity, String urlStr) {
        super(activity);
        this.urlStr = urlStr;
        this.mainActivity = activity;
    }

    public GetRoot(Activity activity) {
        super(activity);
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
            return;
        }
        marrayList = getArrayListFromJSONString(jsonString);
        String rootValue = SearchRoot.searchRootInJson(GetLog.jsonarrayforsearch, marrayList);
        mainActivity.searchUidInResponse(rootValue);
    }

    protected ArrayList<mapdatalist> getArrayListFromJSONString(String jsonString) {
        recordlist = mainActivity.combinedList;
        ArrayList<mapdatalist> output = new ArrayList<>();
        try {
            JSONObject root = new JSONObject(jsonString);

            String bodyString = root.getString("body");

            JSONArray jsonArray = new JSONArray(bodyString);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                mapdatalist MapDatalist = new mapdatalist();
                MapDatalist.rootname = jsonObject.getString("root");
                MapDatalist.holduid = jsonObject.getString("uid");
                MapDatalist.holdnum = jsonObject.getString("hold_numbering");
                MapDatalist.x = jsonObject.getDouble("x");
                MapDatalist.y = jsonObject.getDouble("y");

                for (int j = 0; j < recordlist.size(); j++) {
                    if (recordlist.get(j).recuid.equals(MapDatalist.holduid)) {
                        recordlist.get(j).holdnum = MapDatalist.holdnum;
                        recordlist.get(j).x = MapDatalist.x;
                        recordlist.get(j).y = MapDatalist.y;
                    }
                }
                output.add(MapDatalist);
            }

        } catch (JSONException e) {
            Log.e(TAG, "Exception in processing JSONString.", e);
            e.printStackTrace();
        }
        return output;
    }
}
