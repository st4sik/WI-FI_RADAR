package com.main.android.wi_fi_radar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main extends AppCompatActivity {

    public static int maxSignalLevel=100;
    RadarView rv;
    WifiManager wifi;
    List<ScanResult> results;
    private final static ArrayList<Integer> channelsFrequency =
            new ArrayList<>(
                    Arrays.asList(0, 2412, 2417, 2422, 2427, 2432, 2437, 2442,
                            2447, 2452, 2457, 2462, 2467, 2472, 2484));

    ArrayList<WIFI> WiFiAPs;

    BroadcastReceiver scanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            results= new ArrayList<>();
            results=wifi.getScanResults();
            if (results == null) return;
            WiFiAPs.clear();
            for (ScanResult result : results) {
                WIFI AP = new WIFI(result.SSID,
                        result.BSSID,
                        getChannelFromFrequency
                                (result.frequency),
                        WifiManager.calculateSignalLevel
                                (result.level, maxSignalLevel),
                        getSecurity(result.capabilities),
                        getWPS(result.capabilities));
                WiFiAPs.add(AP);
            }
            rv.invalidate();
        }
    };

    public static boolean getWPS(String capabilities) {

            return capabilities.contains("[WPS]");

    }

    public static int getChannelFromFrequency(int frequency) {
        return channelsFrequency.indexOf(Integer.valueOf(frequency));
    }

    public static int getSecurity(String capabilities ){
        if (capabilities.contains("[WPA")) return 2;
        else if (capabilities.contains("[WEP")) return 1;
        else return 0;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rv = (RadarView)this.findViewById
                (R.id.radarView);

        wifi=(WifiManager)getSystemService(Context.WIFI_SERVICE);

        if (!wifi.isWifiEnabled())
            if (wifi.getWifiState() != WifiManager.WIFI_STATE_ENABLING)
                wifi.setWifiEnabled(true);
        WiFiAPs= new ArrayList<>();


        rv.setData(WiFiAPs);
        wifi.startScan();

    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter
                (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(scanReceiver, filter);
    }

    @Override
    public void onPause() {
        unregisterReceiver(scanReceiver);
        super.onPause();
    }



}
