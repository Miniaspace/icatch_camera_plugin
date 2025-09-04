package com.icatch.mobilecam.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.icatch.mobilecam.Log.AppLog;

import java.util.LinkedList;
import java.util.List;

public class PermissionTools {
    private static String TAG = PermissionTools.class.getSimpleName();
    public static final int WRITE_OR_READ_EXTERNAL_STORAGE_REQUEST_CODE = 102;
//    public static final int READ_EXTERNAL_STORAGE_REQUEST_CODE = 103;
    public static final int ALL_REQUEST_CODE = 102;

    public static final int CAMERA_REQUEST_CODE = 103;

    public static void RequestPermissions(final Activity activity) {
        AppLog.d(TAG, "Start RequestPermissions");
        if ((ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)||
                (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) ) {
            //申请WRITE_EXTERNAL_STORAGE权限
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    WRITE_OR_READ_EXTERNAL_STORAGE_REQUEST_CODE);
        } else {

        }
        AppLog.d(TAG, "End RequestPermissions");
    }

    public static boolean CheckSelfPermission(final Activity activity){
        return (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED)&&
                (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED);
    }

    public static void requestAllPermissions(final Activity activity) {
        AppLog.d(TAG, "Start request all necessary permissions");
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
            return;
        }
        List<String> requestList = new LinkedList<>();

        AppLog.i(TAG,"Build.Version.SDK_INT = "+Build.VERSION.SDK_INT);
        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            // partial access on Android 14 (API level 34) or higher
            if(ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)!= PackageManager.PERMISSION_GRANTED){
                Log.i("PERM","request READ_MEDIA_VISUAL_USER_SELECTED");
                requestList.add(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED);
            }
        } else if (Build.VERSION.SDK_INT >= 33 ){
            // Android 13 (AP Level 33 ) or higher
            if(ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_MEDIA_IMAGES)!= PackageManager.PERMISSION_GRANTED){
                Log.i("PERM","request READ_MEDIA_IMAGES");
                requestList.add(Manifest.permission.READ_MEDIA_IMAGES);
            }
            if(ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_MEDIA_VIDEO)!= PackageManager.PERMISSION_GRANTED){
                Log.i("PERM","request READ_MEDIA_VIDEO");
                requestList.add(Manifest.permission.READ_MEDIA_VIDEO);
            }
            if(ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_MEDIA_AUDIO)!= PackageManager.PERMISSION_GRANTED) {
                Log.i("PERM","request READ_MEDIA_AUDIO");
                requestList.add(Manifest.permission.READ_MEDIA_AUDIO);
            }
        } else {
            if( ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE)  != PackageManager.PERMISSION_GRANTED ){
                // Android 12 (API Level 32)
                Log.i("PERM","request READ_EXTERNAL_STORAGE");
                requestList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }
        if(Build.VERSION.SDK_INT <= 29){
            // Android 10 (API Level 29)
            if( ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)  != PackageManager.PERMISSION_GRANTED ){
                Log.i("PERM","request WRITE_EXTERNAL_STORAGE");
                requestList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        }
        // Append location permission for get wifi ssid
        if(ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            Log.i("PERM","request ACCESS_FINE_LOCATION");
            requestList.add(Manifest.permission.ACCESS_FINE_LOCATION);
            Log.i("PERM","request ACCESS_COARSE_LOCATION");
            requestList.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            if(ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
//                    != PackageManager.PERMISSION_GRANTED) {
//                requestList.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
//            }
//        }

        if(ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED){
            requestList.add(Manifest.permission.RECORD_AUDIO);
        }
        if(requestList.size() > 0){
            String[] systemRequestArray = requestList.toArray(new String[requestList.size()]);
            ActivityCompat.requestPermissions(activity, systemRequestArray,ALL_REQUEST_CODE);
        } else {
            AppLog.d(TAG, "permission has granted!");
        }


        // this is only way to grant MANAGE_EXTERNAL_STORAGE permission on API Level 31 up
        // wifi camera app should not need this permission, so I disabled it.
        // Allen.chuang 2024.1.5
        /*
        if (Build.VERSION.SDK_INT >= 31){
            if (Environment.isExternalStorageManager()) {
                // already has MANAGE_EXTERNAL_STORAGE permission
            } else {
                // this will return to system setting page for ask grant permission
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
                intent.setData(uri);
                activity.startActivityForResult(intent, 1);
            }

        }
         */


        AppLog.d(TAG, "End requestPermissions");
    }


    // for wifi camera
    public static boolean checkAllSelfPermission(final Activity activity){

//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
//            return ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                    == PackageManager.PERMISSION_GRANTED
//                    && ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE)
//                            == PackageManager.PERMISSION_GRANTED
//                    && ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
//                            == PackageManager.PERMISSION_GRANTED
//                    && ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO)
//                            == PackageManager.PERMISSION_GRANTED
//                    && ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
//                            == PackageManager.PERMISSION_GRANTED
//                    ;
//        }else {
//            return ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                    == PackageManager.PERMISSION_GRANTED &&
//                    ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE)
//                            == PackageManager.PERMISSION_GRANTED &&
//                    ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
//                            == PackageManager.PERMISSION_GRANTED&&
//                    ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO)
//                            == PackageManager.PERMISSION_GRANTED;
//        }
        Log.i("PERM","checkAllSelfPermission");
        AppLog.i(TAG,"Build.Version.SDK_INT = "+Build.VERSION.SDK_INT);

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q){
            // <= target 29 (android 10)
            return ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            //  >= target 34
            return ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)== PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_MEDIA_IMAGES)== PackageManager.PERMISSION_GRANTED&&
                    ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_MEDIA_VIDEO)== PackageManager.PERMISSION_GRANTED&&
                    ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED&&
                    ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            >= target 33
            return  ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_MEDIA_VIDEO)  == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_MEDIA_AUDIO)  == PackageManager.PERMISSION_GRANTED&&
                    ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;

        } else {
//            target < 32
            return  ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED&&
                    ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO)== PackageManager.PERMISSION_GRANTED;

        }

    }

    // for USB camera
    public static boolean checkCameraSelfPermission(final Activity activity){
        return ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    // for USB camera
    public static void requestCameraPermissions(final Activity activity) {
        AppLog.d(TAG, "Start request camera necessary permissions");
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
            return;
        }
        List<String> requestList = new LinkedList<>();
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED){
            requestList.add(Manifest.permission.CAMERA);
        }
        if(requestList.size() > 0){
            String[] systemRequestArray = requestList.toArray(new String[requestList.size()]);
            ActivityCompat.requestPermissions(activity, systemRequestArray,CAMERA_REQUEST_CODE);
        } else {
            AppLog.d(TAG, "permission has granted!");
        }
        AppLog.d(TAG, "End requestPermissions");
    }


}
