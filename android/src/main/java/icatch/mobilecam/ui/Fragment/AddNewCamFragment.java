package com.icatch.mobilecam.ui.Fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.PatternMatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.icatch.mobilecam.Listener.OnFragmentInteractionListener;
import com.icatch.mobilecam.Log.AppLog;
import com.icatch.mobilecam.MyCamera.CameraAddType;
import com.icatch.mobilecam.MyCamera.CameraType;
import com.icatch.mobilecam.data.AppInfo.AppInfo;
import com.icatch.mobilecam.data.AppInfo.AppSharedPreferences;
import com.icatch.mobilecam.data.Message.AppMessage;
import com.icatch.mobilecam.R;
import com.icatch.mobilecam.data.entity.CameraSlot;
import com.icatch.mobilecam.ui.ExtendComponent.MyToast;
import com.icatch.mobilecam.utils.OnCallback;
import com.icatch.mobilecam.utils.SharedPreferencesUtil;
import com.icatch.mobilecam.utils.WifiNetworkSpecifierUtil;

public class AddNewCamFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private String TAG = "AddNewCamFragment";
    private Button wifiConnectCamBtn;
    private Button usbConnectCamBtn;
    private Button BTPairBtn;
    private Handler appStartHandler;
    private Context appContext;
    private ImageButton backBtn;
    private int position;
    private Button wifiAudoConnectBtn;
    private String pwd = AppInfo.default_pwd;

    public AddNewCamFragment() {
        // Required empty public constructor
    }

    public AddNewCamFragment(Context context,Handler handler,int position) {
        this.appContext = context;
        this.appStartHandler = handler;
        this.position = position;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        AppLog.d(TAG, "onCreateView");
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_new_cam, container, false);
        BTPairBtn = (Button) view.findViewById(R.id.bt_pair);
        wifiConnectCamBtn = (Button) view.findViewById(R.id.wifi_connect_camera);
        usbConnectCamBtn = (Button) view.findViewById(R.id.usb_connect_camera);
        wifiAudoConnectBtn = view.findViewById(R.id.wifi_auto_connect);

        wifiAudoConnectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    configDevicePwd(R.string.action_input_device_pwd);
                }else {
                    MyToast.show(getContext(),getString(R.string.wifi_auto_connect_cannot_used));
                }
            }
        });
        wifiConnectCamBtn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                appStartHandler.obtainMessage(AppMessage.MESSAGE_CAMERA_CONNECTING_START, CameraType.WIFI_CAMERA,position).sendToTarget();
            }
        });
        usbConnectCamBtn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                appStartHandler.obtainMessage(AppMessage.MESSAGE_CAMERA_CONNECTING_START, CameraType.USB_CAMERA,position).sendToTarget();
            }
        });

        BTPairBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                BTPairBeginFragment btPairBegin = new BTPairBeginFragment(appContext, appStartHandler);
                SharedPreferencesUtil.put(getContext(),SharedPreferencesUtil.CONFIG_FILE,"camera_position",position);
//                int position = (int) SharedPreferencesUtil.get(getContext(),SharedPreferencesUtil.CONFIG_FILE,"camera_position",0);
//                AppLog.d(TAG,"BTPairBeginFragment position:" + position);
                FragmentManager fm = getFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.launch_setting_frame, btPairBegin);
                ft.addToBackStack("BTPairBeginFragment");
                ft.commit();
            }
        });
        backBtn = (ImageButton) view.findViewById(R.id.back_btn);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mListener != null){
                    mListener.removeFragment();
                }
            }
        });
        return view;
    }


    @Override
    public void onAttach(Context context) {
        AppLog.d(TAG, "onAttach");
        super.onAttach(context);
        try {
            mListener = (OnFragmentInteractionListener) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        AppLog.d(TAG, "onDetach");
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onResume() {
        AppLog.d(TAG, "onResume");
        super.onResume();
        if(mListener != null){
            mListener.submitFragmentInfo(AddNewCamFragment.class.getSimpleName(),R.string.title_activity_add_new_cam);
        }
    }

    @Override
    public void onStart() {
        AppLog.d(TAG, "onStart");
        super.onStart();
    }

    @Override
    public void onStop() {
        AppLog.d(TAG, "onStop");
        super.onStop();
    }

    @Override
    public void onDestroy() {
        AppLog.d(TAG, "onDestroy");
        super.onDestroy();
    }

    private void configDevicePwd(int title) {
        showConfigDevicePwdDialog(getActivity(), title);

    }

    private void showConfigDevicePwdDialog(final Context context, int title) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context);
        View contentView = View.inflate(context, R.layout.input_ip, null);
        final EditText resetTxv = (EditText) contentView.findViewById(R.id.ip_address);
        resetTxv.setText(pwd);
        builder.setTitle(title);
        builder.setView(contentView);
        builder.setCancelable(false);
        builder.setPositiveButton(context.getResources().getString(R.string.ok)
                // 为按钮设置监听器
                , new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.Q)
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        pwd = resetTxv.getText().toString();
                        autoConnectWifi(pwd);

                    }
                });
        // 为对话框设置一个“取消”按钮
        builder.setNegativeButton(context.getResources().getString(R.string.gallery_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //取消登录，不做任何事情。
            }
        });
        //创建、并显示对话框
        builder.create().show();
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void autoConnectWifi(final String password) {
        //                    PatternMatcher patternMatcher = new PatternMatcher("[a-zA-z0-9@]+.*", PatternMatcher.PATTERN_ADVANCED_GLOB);
        PatternMatcher patternMatcher =  new PatternMatcher("[\\u4E00-\\u9FA5a-zA-z0-9@_-]+.*", PatternMatcher.PATTERN_ADVANCED_GLOB);
//                    PatternMatcher patternMatcher = new PatternMatcher("SBC", PatternMatcher.PATTERN_PREFIX);
        WifiNetworkSpecifierUtil.getInstance().connectWifi(getContext(),patternMatcher, password, new OnCallback() {
            @Override
            public void onSuccess(String ssid) {
                CameraSlot cameraSlot = new CameraSlot(position, true, ssid, CameraType.WIFI_CAMERA, null, false, CameraAddType.WIFI_CONNECTION_AUTO, password);
                appStartHandler.obtainMessage(AppMessage.MESSAGE_CAMERA_CONNECTING_START, CameraType.WIFI_CAMERA, position, cameraSlot).sendToTarget();

            }

            @Override
            public void onError(int error) {
                WifiNetworkSpecifierUtil.getInstance().disconnectWifi();
                if (error < 0) {
                    MyToast.show(getActivity(), R.string.action_device_pwd_error);
                    configDevicePwd(R.string.action_device_pwd_error);
                }
            }
        });
    }
}
