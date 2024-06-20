package com.cookandroid.apicall;

import com.cookandroid.userapp.mapdatalist;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SearchRoot {
    public static String searchRootInJson(JSONArray jsonArray, ArrayList<mapdatalist> marrayList) {
        String rootValue = null;
        try {

            for (int j = 0; j < marrayList.size(); j++) {
                String targetUid = SearchUid.bringTargetUid(marrayList, j);

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject item = jsonArray.getJSONObject(i);
                    String uid = item.getString("uid");

                    if (uid.equals(targetUid)) {
                        rootValue = SearchUid.bringTargetrootname(marrayList, j);
                    }

                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return rootValue;
    }
}
