package com.main.android.wi_fi_radar;


public class WIFI {

    private boolean WPS;
    private String SSID;
    private String BSSID;
    private int level;
    private int security;
    private int channel;

    public WIFI(String ssid, String bssid, int channelFromFrequency, int level, int security, boolean wps) {
        this.BSSID = bssid;
        this.WPS = wps;
        this.channel = channelFromFrequency;
        this.security = security;
        this.level = level;
        this.SSID = ssid;
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

    public int getLevel() {
        return level;
    }

    public int getSecurity() {
        return security;
    }

    public int getChannel() {
        return channel;
    }

}
