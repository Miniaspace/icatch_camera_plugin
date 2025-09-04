package com.icatch.mobilecam.data.SystemInfo;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;

import com.icatch.mobilecam.Log.AppLog;
import com.icatch.mobilecam.data.AppInfo.AppInfo;
import com.icatch.mobilecam.data.AppInfo.AppSharedPreferences;

import java.util.List;

public class MWifiManager {
    private static String TAG = "MWifiManager";
    private static String WIFI_SSID_UNKNOWN = "unknown";
    public static String getSsid(Context context) {
        if (!isWifiEnabled(context)) {
            AppLog.e(TAG, "----------ssid is null=");
            return null;
        }
        //android 8.0及以下
        String ssid = WIFI_SSID_UNKNOWN;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            WifiManager mWifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = mWifi.getConnectionInfo();
            if (wifiInfo != null) {
                ssid = wifiInfo.getSSID();
                if(ssid.contains("\"")){
                    ssid =ssid.replace("\"","");
                }
            }else {
                AppLog.i(TAG, "getSsid wifiInfo is null");
            }
        }else if(Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo wifiInfo2 = mConnectivityManager.getActiveNetworkInfo();
            AppLog.i(TAG, "getSsid wifiInfo2:" + wifiInfo2);
            if(wifiInfo2 !=null){
                AppLog.i(TAG, "getSsid wifiInfo2.getExtraInfo()" + wifiInfo2.getExtraInfo());
            }
            if (wifiInfo2 == null || wifiInfo2.getExtraInfo() == null) {
                ssid = getSsidByNetworkId(context);
            } else {
                String wifiName = wifiInfo2.getExtraInfo();
                ssid =  wifiName.replaceAll("\"", "");
            }
        }else {
            WifiManager mWifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = mWifi.getConnectionInfo();
            if (wifiInfo != null) {
                ssid = wifiInfo.getSSID();
                if(ssid.contains("\"")){
                    ssid =ssid.replace("\"","");
                }
            }else {
                AppLog.i(TAG, "getSsid wifiInfo is null");
            }
        }
        //android 8.0以后

        AppLog.i(TAG, "getSsid ssid:" + ssid);
        return ssid;
    }
    /**
     * 华为android 9.0 获取 ssid
     */
    private static String getSsidByNetworkId(Context context){
        AppLog.d(TAG, "getSsidByNetworkId ");
        String ssid = null;
        WifiManager wifiManager = ((WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE));
        if(null != wifiManager){
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int networkId = wifiInfo.getNetworkId();
            List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();
            for (WifiConfiguration wifiConfiguration:configuredNetworks){
                if (wifiConfiguration.networkId==networkId){
                    ssid=wifiConfiguration.SSID;
                    break;
                }
            }
        }

        if(ssid != null && ssid.contains("\"")){
            ssid =ssid.replace("\"","");
        }
        AppLog.d(TAG, "getSsidByNetworkId ssid:" + ssid);
        return ssid;
    }

//    public static String getIp(Context context){
//        String ip = "192.168.1.1";
//        if(HotSpot.isApEnabled(context)){
//            String value = HotSpot.getFirstConnectedHotIP();
//            if(value != null){
//                ip = value;
//            }
//        }else if(AppInfo.youtubeLive){
//            String value = AppSharedPreferences.readIp(context);
//            if(value != null){
//                ip = value;
//            }
//        }
//        AppLog.d(TAG,"getIp ip=" + ip);
//        return ip;
//    }
    /**
     ** 判断WIFI网络是否可用
     ** @param context
     ** @return
     *      
     */

    public static boolean isWifiConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiNetworkInfo != null && wifiNetworkInfo.isConnected()) {
            return true;
        }

        return false;

    }
    public static  boolean isWifiEnabled(Context context){
        WifiManager mWifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        return  mWifiManager.isWifiEnabled();
    }
}
