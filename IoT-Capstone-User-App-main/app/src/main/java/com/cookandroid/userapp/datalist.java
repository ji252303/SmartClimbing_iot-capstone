package com.cookandroid.userapp;

public class datalist {
    public String recposition = "";
    public int rectime = 0;
    public String recuid = "";
    public int positoncolor = R.drawable.righthand;
    public String holdnum = "";
    public double x;
    public double y;

    public void setRecposition(String recposition) {
        this.recposition = recposition;
        switch (recposition) {
            case "ArduinoNano33IoT_1":
                positoncolor = R.drawable.righthand;
                break;
            case "ArduinoNano33IoT_2":
                positoncolor = R.drawable.lefthand;
                break;
            case "ArduinoNano33IoT_3":
                positoncolor = R.drawable.rightfoot;
                break;
            case "ArduinoNano33IoT_4":
                positoncolor = R.drawable.leftfoot;
                break;
        }
    }
}
