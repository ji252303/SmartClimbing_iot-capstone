package com.cookandroid.apicall;

import com.cookandroid.userapp.datalist;
import com.cookandroid.userapp.mapdatalist;

import java.util.ArrayList;
public class SearchUid {
    public static String bringTargetUid(ArrayList<mapdatalist> tagList, int a) {
        String targetedUid = null;
        if (!tagList.isEmpty()) {
            targetedUid = tagList.get(a).holduid;
        }
        return targetedUid;
    }

    public static String bringTargetrootname(ArrayList<mapdatalist> tagList, int a) {
        String targetedUid = null;
        if (!tagList.isEmpty()) {
            targetedUid = tagList.get(a).rootname;
        }
        return targetedUid;
    }
}
