package com.icatch.mobilecam.Listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;

import com.icatch.mobilecam.Log.AppLog;
import com.icatch.mobilecam.data.Message.AppMessage;

/**
 * Created by b.jiang on 2016/8/19.
 */
public class WifiListener {
    private String TAG = "WifiListener";
    private WifiReceiver wifiReceiver;
    private Context context;
    private Handler handler;

    public WifiListener(Context context, Handler handler){
        this.context = context;
        this.handler = handler;
    }

    private class WifiReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
//            AppLog.d(TAG,"WifiReceiver: " + intent.getAction());
            if(intent.getAction().equals(WifiManager.RSSI_CHANGED_ACTION)){
                //signal strength changed
            }

            else if(intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)){//wifi连接上与否
//                AppLog.d(TAG,"网络状态改变");
                NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
//                AppLog.d(TAG,"NetworkInfo.getState: " + info.getState() + ", reason: " + info.getReason());
                if(info.getState().equals(NetworkInfo.State.DISCONNECTED)){
                    AppLog.d(TAG,"网络连接断开");
//                    if(!AppInfo.isReconnecting){
//                        handler.obtainMessage(AppMessage.MESSAGE_DISCONNECTED,null).sendToTarget();
//                        AppInfo.isReconnecting = true;
//                    }
                    handler.obtainMessage(AppMessage.MESSAGE_DISCONNECTED,null).sendToTarget();

                }
                else if(info.getState().equals(NetworkInfo.State.CONNECTED)){
                    WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    //获取当前wifi名称
                    AppLog.d(TAG,"连接到网络 " + wifiInfo.getSSID());
                    handler.obtainMessage(AppMessage.MESSAGE_CONNECTED,wifiInfo).sendToTarget();
                }
            }
            else if(intent.getAction().equals(WifiManager.WIFI_STATE_CHANGED_ACTION)){//wifi打开与否
                int wifistate = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_DISABLED);
                if(wifistate == WifiManager.WIFI_STATE_DISABLED){
                    AppLog.d(TAG,"系统关闭wifi");
                }
                else if(wifistate == WifiManager.WIFI_STATE_ENABLED){
                    AppLog.d(TAG,"系统开启wifi");
                }
//            } else if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
//
//                NetworkInfo info = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
//                AppLog.d(TAG,"NetworkInfo.getState: " + info.getState() + ", reason: " + info.getReason());
//                String reason = intent.getStringExtra(ConnectivityManager.EXTRA_REASON);
//                String EXTRA_EXTRA_INFO = intent.getStringExtra(ConnectivityManager.EXTRA_EXTRA_INFO);
//                AppLog.d(TAG,"reason: " + reason +", EXTRA_EXTRA_INFO: " + EXTRA_EXTRA_INFO);

            } else if (intent.getAction().equals(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)) {
                int linkWifiResult = intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, 123);
                SupplicantState supplicantState=((SupplicantState)intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE));
                NetworkInfo.DetailedState state = WifiInfo.getDetailedStateOf(supplicantState);
//                NetworkInfo wifiInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
//                AppLog.e(TAG, "EXTRA_SUPPLICANT_ERROR: "+ linkWifiResult + ", SupplicantState: " + supplicantState + ", NetworkInfo.DetailedState: " + state + ", WifiInfo: " + wifiInfo);
//                AppLog.e(TAG, "wifi密码错误广播: " + linkWifiResult);
                if (linkWifiResult == WifiManager.ERROR_AUTHENTICATING && state == NetworkInfo.DetailedState.DISCONNECTED) {
//                    WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
//                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
//                    //获取当前wifi名称
//                    AppLog.e(TAG, "WiFi密码错误: " + wifiInfo.getSSID());
                    AppLog.e(TAG, "WiFi密码错误");
                    handler.obtainMessage(AppMessage.MESSAGE_WIFI_PASSWORD_ERROR, null).sendToTarget();
                }
            }
        }
    }

    public void registerReceiver(){
        AppLog.d(TAG,"registerReceiver");
        IntentFilter filter= new IntentFilter();
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        wifiReceiver =new WifiReceiver();
        context.registerReceiver(wifiReceiver, filter);
    }

    public void unregisterReceiver(){
        AppLog.d(TAG,"unregisterReceiver");
        if(wifiReceiver != null){
            context.unregisterReceiver(wifiReceiver);
        }
    }
}
