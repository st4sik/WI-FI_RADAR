package com.main.android.wi_fi_radar;


public class WIFI {

    private boolean WPS;
    private String SSID;
    private String BSSID;
    private int level;
    private int security;
    private int channel;

    public void WIFE(String SSID, String BSSID, int level, int security, int channel, boolean WPS) {
        this.BSSID = BSSID;
        this.WPS = WPS;
        this.channel = channel;
        this.security = security;
        this.level = level;
        this.SSID = SSID;
    }

    public boolean getWPS() {
        return WPS;
    }

    public String getSSID() {
        return SSID;
    }

    public String getBSSID() {
        return BSSID;
    }

    public int getLevel(){
        return level;
    }

    public int getSecurity(){
        return security;
    }

    public int getChannel(){
        return channel;
    }

}
