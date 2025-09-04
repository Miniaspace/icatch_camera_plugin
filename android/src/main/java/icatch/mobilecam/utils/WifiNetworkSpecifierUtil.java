package com.icatch.mobilecam.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSpecifier;
import android.net.wifi.WifiNetworkSuggestion;
import android.os.Build;
import android.os.PatternMatcher;

import com.icatch.mobilecam.Log.AppLog;
import com.icatch.mobilecam.data.AppInfo.AppInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

public class WifiNetworkSpecifierUtil {
    private static final String TAG = WifiNetworkSpecifierUtil.class.getSimpleName();

    private static WifiNetworkSpecifierUtil api;
    private ConnectivityManager connectivityManager;

    private NetworkSpecifierCallback networkCallback;

    public static WifiNetworkSpecifierUtil getInstance() {
        if (api == null) {
            api = new WifiNetworkSpecifierUtil();
        }
        return api;
    }


    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void connectWifi(Context context, String ssid, String password, final OnCallback onCallback) {
        AppLog.e(TAG, "connectWifi ssid:" + ssid);
        PatternMatcher patternMatcher = new PatternMatcher(ssid, PatternMatcher.PATTERN_ADVANCED_GLOB);
        connectWifi(context,patternMatcher, password, onCallback);
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void connectWifi(Context context, PatternMatcher patternMatcher, String password, final OnCallback onCallback) {
        AppLog.e(TAG, "connectWifi pwd:" + password);
        disconnectWifi();
        final WifiNetworkSpecifier specifier = new WifiNetworkSpecifier.Builder()
//                .setSsidPattern(new PatternMatcher("SBC_", PatternMatcher.PATTERN_PREFIX))
//                .setSsidPattern(new PatternMatcher("[a-zA-z0-9@]+.*", PatternMatcher.PATTERN_ADVANCED_GLOB))
//                .setSsid(ssid)
//                .setSsidPattern(new PatternMatcher("SBC_C63149", PatternMatcher.PATTERN_LITERAL))
                .setSsidPattern(patternMatcher)
                .setWpa2Passphrase(password)
                .build();

        final NetworkRequest request = new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED)//网络不受限
                .addCapability(NetworkCapabilities.NET_CAPABILITY_TRUSTED)//信任网络，增加这个连个参数让设备连接wifi之后还联网。
//                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .setNetworkSpecifier(specifier)
                .build();

        connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        networkCallback = new NetworkSpecifierCallback() {

            @Override
            public void onPasswordError() {
                super.onPasswordError();
                AppLog.e(TAG, "Network onPasswordError");
                if (onCallback != null) {
                    onCallback.onError(-1);
                }
            }

            @Override
            public void onAvailable(@NonNull Network network) {
                super.onAvailable(network);
                connectivityManager.bindProcessToNetwork(network);
                AppLog.e(TAG, "Network onAvailable: " + network);
                //ping("www.baidu.com");
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (onCallback != null) {
                    onCallback.onSuccess(null);
                }
            }

            @Override
            public void onLosing(@NonNull Network network, int maxMsToLive) {
                super.onLosing(network, maxMsToLive);
                AppLog.e(TAG, "Network onLosing: " + maxMsToLive);
            }

            @Override
            public void onLost(@NonNull Network network) {
                super.onLost(network);
                AppLog.e(TAG, "Network onLost: " + network);
            }

            @Override
            public void onUnavailable() {
                super.onUnavailable();
                AppLog.e(TAG, "Network onUnavailable");
                if (onCallback != null) {
                    onCallback.onError(0);
                }
            }

            @Override
            public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
                super.onCapabilitiesChanged(network, networkCapabilities);
                AppLog.e(TAG, "Network onCapabilitiesChanged: " + networkCapabilities);
            }

            @Override
            public void onLinkPropertiesChanged(@NonNull Network network, @NonNull LinkProperties linkProperties) {
                super.onLinkPropertiesChanged(network, linkProperties);
                AppLog.e(TAG, "Network onLinkPropertiesChanged: " + linkProperties);
            }

            @Override
            public void onBlockedStatusChanged(@NonNull Network network, boolean blocked) {
                super.onBlockedStatusChanged(network, blocked);
                AppLog.e(TAG, "Network onBlockedStatusChanged: " + blocked);
            }
        };

        connectivityManager.requestNetwork(request, networkCallback);
//        connectivityManager.addDefaultNetworkActiveListener(new ConnectivityManager.OnNetworkActiveListener() {
//            @Override
//            public void onNetworkActive() {
//                AppLog.e(TAG, "Network onNetworkActive" );
//
//            }
//        });
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void scanWifi(Context context, final OnCallback onCallback) {
        final WifiNetworkSpecifier specifier = new WifiNetworkSpecifier.Builder()
//                .setSsidPattern(new PatternMatcher("SBC_", PatternMatcher.PATTERN_PREFIX))
                .setSsidPattern(new PatternMatcher("[a-zA-z0-9@]+.*", PatternMatcher.PATTERN_ADVANCED_GLOB))
//                .setSsid("SBC_C63149")
//                .setSsidPattern(new PatternMatcher("SBC_C63149", PatternMatcher.PATTERN_LITERAL))
                .setWpa2Passphrase("1234567890")
                .build();

        final NetworkRequest request = new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
//                .addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED)//网络不受限
//                .addCapability(NetworkCapabilities.NET_CAPABILITY_TRUSTED)//信任网络，增加这个连个参数让设备连接wifi之后还联网。
//                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .setNetworkSpecifier(specifier)
                .build();

        connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        networkCallback = new NetworkSpecifierCallback() {

            @Override
            public void onAvailable(@NonNull Network network) {
                super.onAvailable(network);
                connectivityManager.bindProcessToNetwork(network);
                AppLog.e(TAG, "Network onAvailable: " + network.toString());
                if (onCallback != null) {
                    onCallback.onSuccess(null);
                }
                ping("www.baidu.com");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        ping("www.baidu.com");

                    }
                }).start();

            }

            @Override
            public void onUnavailable() {
                super.onUnavailable();
                AppLog.e(TAG, "Network onUnavailable");
                if (onCallback != null) {
                    onCallback.onError(0);
                }
            }
        };

        connectivityManager.requestNetwork(request, networkCallback);
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void connectWifi2(Context context) {
        WifiNetworkSuggestion.Builder builder = new WifiNetworkSuggestion.Builder();
        builder.setSsid("BOI_5G").setWpa2Passphrase("boi_2020");
        final WifiNetworkSuggestion suggestion = builder.build();


        WifiManager manager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);

        ArrayList<WifiNetworkSuggestion> wifiNetworkSuggestionArrayList = new ArrayList<>();
        wifiNetworkSuggestionArrayList.add(suggestion);
        int status = manager.addNetworkSuggestions(wifiNetworkSuggestionArrayList);

        if (status == WifiManager.STATUS_NETWORK_SUGGESTIONS_SUCCESS) {
            AppLog.e(TAG, "WifiManager.STATUS_NETWORK_SUGGESTIONS_SUCCESS");
        } else {
            AppLog.e(TAG, "!WifiManager.STATUS_NETWORK_SUGGESTIONS_SUCCESS status = " + status);
        }

//        // Optional (Wait for post connection broadcast to one of your suggestions)
//        final IntentFilter intentFilter =
//                new IntentFilter(WifiManager.ACTION_WIFI_NETWORK_SUGGESTION_POST_CONNECTION);
//
//        final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                if (!intent.getAction().equals(
//                        WifiManager.ACTION_WIFI_NETWORK_SUGGESTION_POST_CONNECTION)) {
//                    return;
//                }
//
//                AppLog.e(TAG, "WifiManager.ACTION_WIFI_NETWORK_SUGGESTION_POST_CONNECTION");
//                // do post connect processing here..
//            }
//        };
//        registerReceiver(broadcastReceiver, intentFilter);



    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void connectWifi3(Context context) {
        final WifiNetworkSuggestion suggestion1 =
                new WifiNetworkSuggestion.Builder()
                        .setSsid("SBC_C63149")
                        .setWpa2Passphrase("1234567890")
                        .setIsAppInteractionRequired(true) // Optional (Needs location permission)
                        .build();

        final WifiNetworkSuggestion suggestion2 =
                new WifiNetworkSuggestion.Builder()
                        .setSsid("BOI")
                        .setWpa2Passphrase("boi_2020")
                        .setIsAppInteractionRequired(true) // Optional (Needs location permission)
                        .build();

        final WifiNetworkSuggestion suggestion3 =
                new WifiNetworkSuggestion.Builder()
                        .setSsid("BpSC-VPN")
                        .setWpa2Passphrase("BpSC-VPN")
                        .setIsAppInteractionRequired(true) // Optional (Needs location permission)
                        .build();

        final List<WifiNetworkSuggestion> suggestionsList = new ArrayList();
        suggestionsList.add(suggestion1);
        suggestionsList.add(suggestion2);
        suggestionsList.add(suggestion3);


        final WifiManager wifiManager =
                (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        final int status = wifiManager.addNetworkSuggestions(suggestionsList);
        if (status != WifiManager.STATUS_NETWORK_SUGGESTIONS_SUCCESS) {
            AppLog.e(TAG, "!WifiManager.STATUS_NETWORK_SUGGESTIONS_SUCCESS status = " + status);

// do error handling here…
        } else {
            AppLog.e(TAG, "status == WifiManager.STATUS_NETWORK_SUGGESTIONS_SUCCESS");

        }



// Optional (Wait for post connection broadcast to one of your suggestions)
//        final IntentFilter intentFilter =
//                new IntentFilter(WifiManager.ACTION_WIFI_NETWORK_SUGGESTION_POST_CONNECTION);
//
//        final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                if (!intent.getAction().equals(
//                        WifiManager.ACTION_WIFI_NETWORK_SUGGESTION_POST_CONNECTION)) {
//                    AppLog.e(TAG, "intent.getAction() = " + intent.getAction());
//                    return;
//                }
//
//                AppLog.e(TAG, "WifiManager.ACTION_WIFI_NETWORK_SUGGESTION_POST_CONNECTION");
//                // do post connect processing here..
//            }
//        };
//        registerReceiver(broadcastReceiver, intentFilter);
    }

    public void disconnectWifi() {
        if (connectivityManager != null) {
            connectivityManager.bindProcessToNetwork(null);
            if (networkCallback != null) {
                connectivityManager.unregisterNetworkCallback(networkCallback);
                networkCallback = null;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            connectivityManager = null;
        }
    }

    public void notifyPasswordError() {
        if (networkCallback != null) {
            networkCallback.onPasswordError();
        }
    }


    public void bindToNetwork(Context context,final OnCallback onCallback) {
        AppLog.d(TAG,"bindToNetwork");
        final ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkRequest.Builder builder;
        builder = new NetworkRequest.Builder();

        builder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI);
        connectivityManager.requestNetwork(builder.build(), new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                AppLog.d(TAG,"bindToNetwork onAvailable:"+network.toString());
                connectivityManager.bindProcessToNetwork(network);
                connectivityManager.unregisterNetworkCallback(this);
                if(onCallback != null){
                    onCallback.onSuccess(null);
                }
            }

            @Override
            public void onUnavailable() {
                AppLog.d(TAG,"bindToNetwork onUnavailable:");
                super.onUnavailable();
                if(onCallback != null){
                    onCallback.onError(0);
                }
            }

            @Override
            public void onLosing(@NonNull Network network, int maxMsToLive) {
                super.onLosing(network, maxMsToLive);
                AppLog.d(TAG,"bindToNetwork onLosing:");
            }

            @Override
            public void onLost(@NonNull Network network) {
                super.onLost(network);
                AppLog.d(TAG,"bindToNetwork onLost:");
            }
        });
    }

    //判断是否有外网连接（普通方法不能判断外网的网络是否连接，比如连接上局域网）

    public static void ping() {
        ping("www.baidu.com");

    }
    public static void ping(final String addr) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                String result = null;
                String ip = addr;// ping 的地址，可以换成任何一种可靠的外网
                try {
                    Process p = Runtime.getRuntime().exec("ping -c 5 -w 10 " + ip);// ping -c 次 -w timeout
                    // 读取ping的内容，可以不加
                    InputStream input = p.getInputStream();
                    BufferedReader in = new BufferedReader(new InputStreamReader(input));
                    StringBuffer stringBuffer = new StringBuffer();
                    String content = "";
                    AppLog.d("PING", "---- ping[" + ip + "] result content ----");
                    while ((content = in.readLine()) != null) {
                        if (!content.isEmpty()) {
                            AppLog.d("PING", "---- " + content + " ----");
                        }
                    }
                    // ping的状态
                    int status = p.waitFor();
                    if (status == 0) {
                        result = "success";
                    } else {
                        result = "failed";
                    }
                } catch (IOException e) {
                    result = "IOException";
                } catch (InterruptedException e) {
                    result = "InterruptedException";
                } finally {
                    AppLog.d("PING", "---- ping[" + ip + "] result = " + result + " ----");
                }
            }
        }).start();
    }

    private class NetworkSpecifierCallback extends ConnectivityManager.NetworkCallback {
        public void onPasswordError() {}
    }


}

