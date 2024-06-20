package com.cookandroid.userapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class recordlistview extends BaseAdapter {
    LayoutInflater layoutInflater = null;
    private ArrayList<datalist> listViewData = null;
    private int count = 0;
    private int firstTime;

    public recordlistview(ArrayList<datalist> listData, int firstTime) {
        listViewData = listData;
        count = listViewData.size();
        this.firstTime = firstTime;
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public Object getItem(int position) {
        return listViewData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            final Context context = parent.getContext();
            if (layoutInflater == null) {
                layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            }
            convertView = layoutInflater.inflate(R.layout.recordlist, parent, false);
        }

        ImageView mainImage = convertView.findViewById(R.id.positionicon);
        TextView rectime = convertView.findViewById(R.id.recordtime);
        TextView holdname = convertView.findViewById(R.id.holdname);

        datalist data = listViewData.get(position);
        mapdatalist mdata ;

        mainImage.setImageResource(data.positoncolor);
        int elapsedTime = data.rectime - firstTime;
        String formattedTime = formatElapsedTime(elapsedTime);
        rectime.setText(formattedTime);
        holdname.setText(data.holdnum);

        return convertView;
    }

    private String formatElapsedTime(int elapsedTime) {
        long hours = TimeUnit.SECONDS.toHours(elapsedTime);
        long minutes = TimeUnit.SECONDS.toMinutes(elapsedTime) - TimeUnit.HOURS.toMinutes(hours);
        long seconds = elapsedTime - TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(elapsedTime));

        return String.format("%d:%02d:%02d", hours, minutes, seconds);
    }
}