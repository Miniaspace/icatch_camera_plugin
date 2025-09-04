package com.icatch.mobilecam.ui.activity;

import static android.os.Environment.isExternalStorageManager;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.appbar.AppBarLayout;
import com.icatch.mobilecam.Listener.MyOrientoinListener;
import com.icatch.mobilecam.Listener.OnFragmentInteractionListener;
import com.icatch.mobilecam.Log.AppLog;
import com.icatch.mobilecam.Presenter.LaunchPresenter;
import com.icatch.mobilecam.R;
import com.icatch.mobilecam.data.AppInfo.AppInfo;
import com.icatch.mobilecam.data.AppInfo.ConfigureInfo;
import com.icatch.mobilecam.data.GlobalApp.ExitApp;
import com.icatch.mobilecam.data.GlobalApp.GlobalInfo;
import com.icatch.mobilecam.ui.Interface.LaunchView;
import com.icatch.mobilecam.ui.activity.nfc.NdefMessageParser;
import com.icatch.mobilecam.ui.activity.nfc.NfcUtil;
import com.icatch.mobilecam.ui.activity.nfc.ParsedNdefRecord;
import com.icatch.mobilecam.ui.adapter.CameraSlotAdapter;
import com.icatch.mobilecam.ui.appdialog.AppDialog;
import com.icatch.mobilecam.utils.ClickUtils;
import com.icatch.mobilecam.utils.GlideUtils;
import com.icatch.mobilecam.utils.GpsUtil;
import com.icatch.mobilecam.utils.LruCacheTool;
import com.icatch.mobilecam.utils.PermissionTools;
import com.icatch.mobilecam.utils.WifiNetworkSpecifierUtil;
import com.icatch.mobilecam.utils.fileutils.FileOper;
import com.icatch.mobilecam.utils.fileutils.FileUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class LaunchActivity extends MobileCamBaseActivity implements View.OnClickListener, LaunchView, OnFragmentInteractionListener {
    private final static String TAG = "LaunchActivity";
    private TextView noPhotosFound, noVideosFound;
    private ImageView localVideo, localPhoto;
    private ListView camSlotListView;
    private LaunchPresenter presenter;
    private LinearLayout launchLayout;
    private FrameLayout launchSettingFrame;
    private ActionBar actionBar;
    private AppBarLayout appBarLayout;
    private MyOrientoinListener myOrientoinListener;
    private PendingIntent mPendingIntent;
    private NfcAdapter mNfcAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        AppLog.d(TAG, "cpu type is " + android.os.Build.CPU_ABI);
//        if (android.os.Build.CPU_ABI.contains("armeabi") == false) {
//            return;
//        }
        setContentView(R.layout.activity_launch);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        //申请读写外部存储权限
        //PermissionTools.RequestPermissions(LaunchActivity.this);
        appBarLayout = (AppBarLayout) findViewById(R.id.appbar);
        launchLayout = (LinearLayout) findViewById(R.id.launch_view);
        launchSettingFrame = (FrameLayout) findViewById(R.id.launch_setting_frame);

        noPhotosFound = (TextView) findViewById(R.id.no_local_photos);
        noVideosFound = (TextView) findViewById(R.id.no_local_videos);

        localVideo = (ImageView) findViewById(R.id.local_video);
        localVideo.setOnClickListener(this);
        localPhoto = (ImageView) findViewById(R.id.local_photo);
        localPhoto.setOnClickListener(this);
        presenter = new LaunchPresenter(LaunchActivity.this);
        presenter.setView(this);
//        presenter.addGlobalLisnter(ICatchCamEventID.ICH_CAM_EVENT_SDCARD_REMOVED, false);
        camSlotListView = (ListView) findViewById(R.id.cam_slot_listview);
        camSlotListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                presenter.removeCamera(position);
                return false;
            }
        });

        camSlotListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !GpsUtil.checkGPSIsOpen(LaunchActivity.this)){
                    AppDialog.showDialogWarn(LaunchActivity.this, R.string.turn_on_location_information_tips, false, new AppDialog.OnDialogSureClickListener() {
                        @Override
                        public void onSure() {
                            GpsUtil.openGpsSettings(LaunchActivity.this);
                        }
                    });
                    return;
                }
                if (!ClickUtils.isFastDoubleClick(camSlotListView)){
                    FragmentManager fm = getSupportFragmentManager();
                    presenter.launchCamera(position, fm);
                }
            }
        });

        LruCacheTool.getInstance().initLruCache();
        presenter.initUsbMonitor();
        if (Build.VERSION.SDK_INT < 23 || PermissionTools.checkAllSelfPermission(this)) {
            ConfigureInfo.getInstance().initCfgInfo(this.getApplicationContext());
            checkLicenseAgreement(LaunchActivity.this);
        } else {
            Log.i("PERM","start to request All permission");
            PermissionTools.requestAllPermissions(LaunchActivity.this);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Log.i("PERM","MANAGE_EXTERNAL_STORAGE="+ isExternalStorageManager());
        }else{
            Log.i("PERM","MANAGE_EXTERNAL_STORAGE="+ ContextCompat.checkSelfPermission(this, Manifest.permission.MANAGE_EXTERNAL_STORAGE));
        }
        Log.i("PERM","WRITE_EXTERNAL_STORAGE="+ ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE));
        Log.i("PERM","ACCESS_FINE_LOCATION="+ ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION));
        Log.i("PERM","READ_EXTERNAL_STORAGE="+ ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE));
        AppLog.i(TAG, "end onCreate");

        //初始化nfc
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
            if (android.os.Build.VERSION.SDK_INT >= 31) {
                mPendingIntent = PendingIntent.getActivity(this, 0,
                        new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_IMMUTABLE);
            }else {
                mPendingIntent = PendingIntent.getActivity(this, 0,
                        new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
            }
            if (mNfcAdapter == null) {
                Toast.makeText(this, "nfc is not available", Toast.LENGTH_SHORT).show();
                return;
            }
        }

    }

    @Override
    protected void onStart() {
        AppLog.i(TAG, "onStart");
        super.onStart();
//        if (android.os.Build.CPU_ABI.contains("armeabi") == false) {
//            AppDialog.showDialogQuit(LaunchActivity.this, "Do not support X86!");
//            return;
//        }
    }

    @Override
    protected void onResume() {
        AppLog.i(TAG, "Start onResume");
        super.onResume();
//        presenter.submitAppInfo();
        if (Build.VERSION.SDK_INT < 23 || PermissionTools.checkAllSelfPermission(this)) {
            presenter.loadLocalThumbnails02();
        }
        presenter.registerWifiReceiver();
//        presenter.registerUSB();
        presenter.loadListview();

        myOrientoinListener = new MyOrientoinListener(this, this);
        boolean autoRotateOn = (android.provider.Settings.System.getInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0) == 1);
        //检查系统是否开启自动旋转
        if (autoRotateOn) {
            myOrientoinListener.enable();
        }
        AppLog.i(TAG, "End onResume");

        if (mNfcAdapter != null) { //有nfc功能
            if (mNfcAdapter.isEnabled()) {
                //nfc功能打开了
                //隐式启动
                mNfcAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);
            } else {
                AppLog.d(TAG,"nfc功能未打开");
//                Toast.makeText(MainActivity.this, "请打开nfc功能", Toast.LENGTH_SHORT).show();
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            registerNFCReceiver();
        }
    }



    @Override
    protected void onPause() {
        super.onPause();
        AppLog.i(TAG, "onPause");
        if (myOrientoinListener != null) {
            myOrientoinListener.disable();
            myOrientoinListener = null;
        }
        if (mNfcAdapter != null) {
            mNfcAdapter.disableForegroundDispatch(this);
        }
        unregisterNFCReceiver();

    }

    @Override
    protected void onStop() {
        AppLog.i(TAG, "onStop");
        super.onStop();
        presenter.unregisterWifiReceiver();
        presenter.unregisterUSB();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_HOME:
                Log.d("AppStart", "home");
                break;
            case KeyEvent.KEYCODE_BACK:
                Log.d("AppStart", "back");
//                finish();
                removeFragment();
                break;
            case KeyEvent.KEYCODE_MENU:
                Log.d("AppStart", "KEYCODE_MENU");
                break;
            default:
                return super.onKeyDown(keyCode, event);
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        presenter.removeActivity();
//        presenter.delGlobalLisnter(ICatchCamEventID.ICH_CAM_EVENT_SDCARD_REMOVED, false);
        GlobalInfo.getInstance().endSceenListener();
        LruCacheTool.getInstance().clearCache();
        AppLog.refreshAppLog();
        WifiNetworkSpecifierUtil.getInstance().disconnectWifi();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_launch, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        AppLog.d(TAG, "onPrepareOptionsMenu");
        super.onPrepareOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        AppLog.i(TAG, "id =" + id);
//        AppLog.i(TAG, "R.id.home =" + R.id.home);
        AppLog.i(TAG, "R.id.action_search =" + R.id.action_search);
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {
            //return true;
            presenter.startSearchCamera();
        }else if (id == R.id.action_input_ip) {
//            try {
//                Thread.sleep(8000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
            presenter.inputIp();
        }else if(id == R.id.action_device_pwd){
            presenter.configDevicePwd();
        }
        else if (id == R.id.action_about) {
            AppDialog.showAPPVersionDialog(LaunchActivity.this);
        }else if (id == android.R.id.home) {
//            finish();
            removeFragment();
            return true;
        }else if (id == R.id.action_license) {
            Intent mainIntent = new Intent(LaunchActivity.this, LicenseAgreementActivity.class);
            startActivity(mainIntent);;
        } else if (id == R.id.action_help) {
            Intent mainIntent = new Intent(LaunchActivity.this, LaunchHelpActivity.class);
            LaunchActivity.this.startActivity(mainIntent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        AppLog.i(TAG, "click info:::v.getId() =" + v.getId());
        AppLog.i(TAG, "click info:::R.id.local_photo =" + R.id.local_photo);
        AppLog.i(TAG, "click info:::R.id.local_video =" + R.id.local_video);
        Intent intent = new Intent();
        int id = v.getId();
        if (id == R.id.local_photo) {
            AppLog.i(TAG, "click the local photo");
            intent.putExtra("CUR_POSITION", 0);
            intent.setClass(LaunchActivity.this, LocalMultiPbActivity.class);
            startActivity(intent);
//                presenter.requesetUsbPermission();
//                UsbDeviceManager usbDeviceManager  = new UsbDeviceManager();
//                usbDeviceManager.getUsbPermission(LaunchActivity.this);
        } else if (id == R.id.local_video) {
            AppLog.i(TAG, "click the local video");
            intent.putExtra("CUR_POSITION", 1);
            intent.setClass(LaunchActivity.this, LocalMultiPbActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void setLocalPhotoThumbnail(Bitmap bitmap) {
        localPhoto.setImageBitmap(bitmap);
    }

    @Override
    public void setLocalVideoThumbnail(Bitmap bitmap) {
        localVideo.setImageBitmap(bitmap);
    }

    @Override
    public void loadDefaultLocalPhotoThumbnail() {
        localPhoto.setImageResource(R.drawable.local_default_thumbnail);
    }

    @Override
    public void loadDefaultLocalVideoThumbnail() {
        localVideo.setImageResource(R.drawable.local_default_thumbnail);
    }

    @Override
    public void setNoPhotoFilesFoundVisibility(int visibility) {
        noPhotosFound.setVisibility(visibility);
    }

    @Override
    public void setNoVideoFilesFoundVisibility(int visibility) {
        noVideosFound.setVisibility(visibility);
    }

    @Override
    public void setPhotoClickable(boolean clickable) {
        localPhoto.setEnabled(clickable);
    }

    @Override
    public void setVideoClickable(boolean clickable) {
        localVideo.setEnabled(clickable);
    }

    @Override
    public void setListviewAdapter(CameraSlotAdapter cameraSlotAdapter) {
        camSlotListView.setAdapter(cameraSlotAdapter);
    }

    @Override
    public void setBackBtnVisibility(boolean visibility) {
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(visibility);
        }
    }

    @Override
    public void setNavigationTitle(int resId) {
        if (actionBar != null) {
            actionBar.setTitle(resId);
        }
    }

    @Override
    public void setNavigationTitle(String res) {
        if (actionBar != null) {
            actionBar.setTitle(res);
        }
    }

    @Override
    public void setLaunchLayoutVisibility(int visibility) {
        launchLayout.setVisibility(visibility);
        appBarLayout.setVisibility(visibility);
    }

    @Override
    public void setLaunchSettingFrameVisibility(int visibility) {
        launchSettingFrame.setVisibility(visibility);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean retValue = true;
        switch (requestCode) {
            case PermissionTools.ALL_REQUEST_CODE:
                AppLog.i(TAG, "ALL_REQUEST_CODE permissions.length = " + permissions.length);
                AppLog.i(TAG, "grantResults.length = " + grantResults.length);
                for (int i = 0; i < permissions.length; i++) {
                    AppLog.i(TAG, "permissions:" + permissions[i] + " grantResults:" + grantResults[i]);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        if (!isExternalStorageManager()) {
                            retValue = false;
                            break;
                        }
                    } else {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            retValue = false;
                            break;
                        }
                    }
                }
                if (retValue) {
                    presenter.loadLocalThumbnails02();
                    ConfigureInfo.getInstance().initCfgInfo(this.getApplicationContext());
                    checkLicenseAgreement(LaunchActivity.this);
                } else {
                    // show deny message and exit app
//                    AppDialog.showDialogQuit(this, R.string.permission_is_denied_info);
                }

                break;

            case PermissionTools.CAMERA_REQUEST_CODE:
                for (int i = 0; i < permissions.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        retValue = false;
                        break;
                    }
                }

                if (retValue) {
                    presenter.reconnectUSBCamera();
                } else {
                    AppDialog.showDialogWarn(this, R.string.camera_permission_is_denied_info);
                }
                break;
            default:
                 AppDialog.showDialogWarn(this, R.string.permission_is_denied_info);
        }
    }

    @Override
    public void submitFragmentInfo(String fragment, int resId) {
//        setNavigationTitle(resId);
    }

    @Override
    public void removeFragment() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            finish();
        } else {
            if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
                setNavigationTitle(R.string.app_name);
                launchSettingFrame.setVisibility(View.GONE);
                launchLayout.setVisibility(View.VISIBLE);
                appBarLayout.setVisibility(View.VISIBLE);
                setBackBtnVisibility(false);
            }
            getSupportFragmentManager().popBackStack();
        }
    }

    // 将所有的fragment 出栈;

    @Override
    public void fragmentPopStackOfAll() {
        int fragmentBackStackNum = getSupportFragmentManager().getBackStackEntryCount();
        for (int i = 0; i < fragmentBackStackNum; i++) {
            getSupportFragmentManager().popBackStack();
        }
        setBackBtnVisibility(false);
        setNavigationTitle(R.string.app_name);
        launchSettingFrame.setVisibility(View.GONE);
        launchLayout.setVisibility(View.VISIBLE);
        appBarLayout.setVisibility(View.VISIBLE);

    }

    @Override
    public void setLocalPhotoThumbnailUri(Uri uri) {
        GlideUtils.loadImageViewLodingSizeForUri(this, uri, 500, 500, localPhoto, R.drawable.local_default_thumbnail, R.drawable.local_default_thumbnail);

    }

    @Override
    public void setLocalVideoThumbnailUri(Uri uri) {
        GlideUtils.loadImageViewLodingSizeForUri(this, uri, 500, 500, localVideo, R.drawable.local_default_thumbnail, R.drawable.local_default_thumbnail);

    }

    @Override
    public void setLocalPhotoThumbnail(String filePath) {
        GlideUtils.loadImageViewLodingSize(this, filePath, 500, 500, localPhoto, R.drawable.local_default_thumbnail, R.drawable.local_default_thumbnail);
    }

    @Override
    public void setLocalVideoThumbnail(String filePath) {
        GlideUtils.loadImageViewLodingSize(this,filePath,500,500,localVideo,R.drawable.local_default_thumbnail,R.drawable.local_default_thumbnail);
    }


    public void checkLicenseAgreement(Context context){
        SharedPreferences preferences = context.getSharedPreferences("appData", MODE_PRIVATE);
        boolean isAgreeLicenseAgreement = preferences.getBoolean("agreeLicenseAgreement", false);
        AppLog.d(TAG, "showLicenseAgreementDialog isAgreeLicenseAgreement=" + isAgreeLicenseAgreement);
        String AgreeLicenseAgreementVersion = preferences.getString("agreeLicenseAgreementVersion", "");
        AppLog.d(TAG, "showLicenseAgreementDialog Version =" + AgreeLicenseAgreementVersion);

        if ((!isAgreeLicenseAgreement) || (!AppInfo.EULA_VERSION.equalsIgnoreCase(AgreeLicenseAgreementVersion))) {
            showLicenseAgreementDialog(context, AppInfo.EULA_VERSION);
        }
    }

    AlertDialog agreementDialog;
    public void showLicenseAgreementDialog(final Context context, final String eulaversion) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View contentView = View.inflate(context, R.layout.dialog_privacy_policy, null);
        TextView textView = contentView.findViewById(R.id.txv_privacy_policy);
        SpannableString spanString = new SpannableString(context.getString(R.string.content_privacy_policy_2));
        spanString.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View view) {
                //点击的响应事件
                //AppLog.d(TAG,"spanString onclick");
                //MyToast.show(context,"onclick");
                Intent mainIntent = new Intent(LaunchActivity.this, LicenseAgreementActivity.class);
                startActivity(mainIntent);
            }
        }, 0, spanString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setText(R.string.content_privacy_policy_1);
        textView.append(spanString);
        textView.append(context.getString(R.string.content_privacy_policy_3));
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        builder.setTitle(R.string.title_privacy_policy);
        builder.setView(contentView);

        builder.setPositiveButton(R.string.text_agree, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences.Editor editor = context.getSharedPreferences("appData", MODE_PRIVATE).edit();
                editor.putBoolean("agreeLicenseAgreement", true);
                editor.putString("agreeLicenseAgreementVersion", eulaversion);
                editor.commit();
                dialog.dismiss();
            }
        });
        builder.setCancelable(false);
        builder.setNegativeButton(R.string.text_disagree, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                ExitApp.getInstance().exit();
            }
        });
        agreementDialog = builder.create();
        agreementDialog.show();
    }

    public void closeLicenseAgreementDialog(){
        if(agreementDialog != null){
            agreementDialog.dismiss();
            agreementDialog = null;
        }
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (mNfcAdapter != null) { //有nfc功能
            if (mNfcAdapter.isEnabled()) {//nfc功能打开了
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                    resolveIntent(getIntent());
                }else {
                    AppLog.d(TAG,"android版本较低，不支持 nfc连接功能 VERSION:" +Build.VERSION.SDK_INT);
                }
            } else {
                AppLog.d(TAG,"nfc功能未打开");
//                Toast.makeText(MainActivity.this, "请打开nfc功能", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //初次判断是什么类型的NFC卡
    private void resolveIntent(Intent intent) {
        NdefMessage[] msgs = NfcUtil.getNdefMsg(intent); //重点功能，解析nfc标签中的数据
        if (msgs != null) {
            setNFCMsgView(msgs);
        }

    }

    private StringBuffer mTagText = new StringBuffer();//NFC扫描结果

    /**
     * 显示扫描后的信息
     *
     * @param ndefMessages ndef数据
     */
    @SuppressLint("SetTextI18n")
    private void setNFCMsgView(NdefMessage[] ndefMessages) {
        if (ndefMessages == null || ndefMessages.length == 0) {
            return;
        }
        AppLog.d(TAG,"ndefMessages length:" + ndefMessages.length);
        List<ParsedNdefRecord> records = NdefMessageParser.parse(ndefMessages[0]);

        AppLog.d(TAG,"records length:" + records.size());
        try {
            ParsedNdefRecord record = records.get(0);
            String message = record.getViewText();

            try {
                JSONObject jsonObject = new JSONObject(message);
                String type = jsonObject.optString("type");
                String ssid = jsonObject.optString("ssid");
                String password = jsonObject.optString("password");
                AppLog.d(TAG, "setNFCMsgView type=" + type + " ssid=" + ssid + " password:" + password);

                showConnectCameraDialogForNfc(LaunchActivity.this,ssid,password);

            } catch (JSONException e) {
                e.printStackTrace();
                AppLog.d(TAG, "setNFCMsgView JSONException:" +e.getMessage());
            }

//            String[] numberArray = message.split("\n");
//            for (int i = 0; i < numberArray.length; i++) {
//                AppLog.d(TAG, "numberArray: " + numberArray[i]);
//                mTagText.append(numberArray[i]);
//            }
        } catch (Exception e) {
            AppLog.d(TAG, "setNFCMsgView Exception:" +e.getMessage());
        }
    }

    AlertDialog nfcConnectDailog;
    private void showConnectCameraDialogForNfc(Context context, final String ssid, final String password){
        if(nfcConnectDailog != null && nfcConnectDailog.isShowing()){
            nfcConnectDailog.cancel();
            nfcConnectDailog = null;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

//        PackageInfo packageInfo = null;
//        try {
//            packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
//        } catch (PackageManager.NameNotFoundException e) {
//            e.printStackTrace();
//        }
//        String appVersion = "";
//        if (packageInfo != null) {
//            appVersion = packageInfo.versionName;
//        }
        String info  =  getString(R.string.nfc_connect_camera_info)
                .replace("$1$", ssid)
                .replace("$2$", password);
        builder.setMessage(info);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                presenter.connectCameraForNfc(ssid,password);
            }
        });
        builder.setNegativeButton(R.string.gallery_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        nfcConnectDailog = builder.create();
        nfcConnectDailog.show();
    }

    private void enableNFC(){
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (android.os.Build.VERSION.SDK_INT >= 31) {
            mPendingIntent = PendingIntent.getActivity(this, 0,
                    new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_IMMUTABLE);
        }else {
            mPendingIntent = PendingIntent.getActivity(this, 0,
                    new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        }
        if (mNfcAdapter.isEnabled()) {
            mNfcAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);
        } else {
            AppLog.d(TAG,"nfc功能未打开");
        }
    }

    private void disableNFC(){
        if (mNfcAdapter != null) {
            mNfcAdapter.disableForegroundDispatch(this);
            mNfcAdapter = null;
        }
    }

    private NFCStateReceiver nfcStateReceiver;
    private void registerNFCReceiver(){
        if (nfcStateReceiver == null) {
            nfcStateReceiver = new NFCStateReceiver();
        }
        IntentFilter nfcFilter = new IntentFilter();
        nfcFilter.addAction(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED);
        registerReceiver(nfcStateReceiver, nfcFilter);
    }

    private void unregisterNFCReceiver(){
        if(nfcStateReceiver != null){
            unregisterReceiver(nfcStateReceiver);
            nfcStateReceiver = null;
        }
    }

    //NFC状态监听
    private class NFCStateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                NfcManager manager = (NfcManager) context.getSystemService(Context.NFC_SERVICE);
                NfcAdapter adapter = manager.getDefaultAdapter();
                if (adapter != null) {
                    if (!adapter.isEnabled()){
                        AppLog.d(TAG,"NFC关闭");
//                        Toast.makeText(mContext,"请开启NFC", Toast.LENGTH_LONG).show();
                        disableNFC();
                    }else {
                        AppLog.d(TAG,"NFC开启");
//                        startReader();
                        enableNFC();
                    }
                }
            } catch (Exception e) {
                AppLog.d(TAG,"NFC 监听异常："+ e.getMessage());
            }
        }
    }


}
