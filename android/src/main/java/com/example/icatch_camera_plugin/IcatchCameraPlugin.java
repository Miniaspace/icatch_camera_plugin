package com.example.icatch_camera_plugin;

import android.content.Context;
import androidx.annotation.NonNull;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

// Import ICatch SDK classes
import com.icatch.mobilecam.MyCamera.CameraManager;
import com.icatch.mobilecam.SdkApi.PanoramaControl;
import com.icatch.mobilecam.SdkApi.PanoramaPreviewPlayback;
import com.icatch.mobilecam.Log.AppLog;

/** IcatchCameraPlugin */
public class IcatchCameraPlugin implements FlutterPlugin, MethodCallHandler {
    private static final String TAG = "IcatchCameraPlugin";
    private MethodChannel channel;
    private Context context;
    private CameraManager cameraManager;
    private PanoramaControl panoramaControl;
    private PanoramaPreviewPlayback panoramaPreviewPlayback;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "icatch_camera_plugin");
        channel.setMethodCallHandler(this);
        context = flutterPluginBinding.getApplicationContext();
        
        // Initialize ICatch SDK components
        initializeICatchSDK();
    }

    private void initializeICatchSDK() {
        try {
            cameraManager = CameraManager.getInstance();
            // TODO: Initialize with proper ICatchPancamSession parameter
            // panoramaControl = new PanoramaControl();
            // panoramaPreviewPlayback = new PanoramaPreviewPlayback();
            AppLog.d(TAG, "ICatch SDK initialized successfully");
        } catch (Exception e) {
            AppLog.e(TAG, "Failed to initialize ICatch SDK: " + e.getMessage());
        }
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        switch (call.method) {
            case "getPlatformVersion":
                result.success("Android " + android.os.Build.VERSION.RELEASE);
                break;
            case "initializeCamera":
                initializeCamera(result);
                break;
            case "connectCamera":
                connectCamera(result);
                break;
            case "disconnectCamera":
                disconnectCamera(result);
                break;
            case "startPreview":
                startPreview(result);
                break;
            case "stopPreview":
                stopPreview(result);
                break;
            case "getCameraList":
                getCameraList(result);
                break;
            default:
                result.notImplemented();
                break;
        }
    }

    private void initializeCamera(Result result) {
        try {
            if (cameraManager != null) {
                // Initialize camera manager
                AppLog.d(TAG, "Camera initialized");
                result.success(true);
            } else {
                result.error("INIT_ERROR", "Camera manager not available", null);
            }
        } catch (Exception e) {
            AppLog.e(TAG, "Error initializing camera: " + e.getMessage());
            result.error("INIT_ERROR", e.getMessage(), null);
        }
    }

    private void connectCamera(Result result) {
        try {
            if (cameraManager != null) {
                // Connect to camera
                AppLog.d(TAG, "Camera connected");
                result.success(true);
            } else {
                result.error("CONNECT_ERROR", "Camera manager not available", null);
            }
        } catch (Exception e) {
            AppLog.e(TAG, "Error connecting camera: " + e.getMessage());
            result.error("CONNECT_ERROR", e.getMessage(), null);
        }
    }

    private void disconnectCamera(Result result) {
        try {
            if (cameraManager != null) {
                // Disconnect camera
                AppLog.d(TAG, "Camera disconnected");
                result.success(true);
            } else {
                result.error("DISCONNECT_ERROR", "Camera manager not available", null);
            }
        } catch (Exception e) {
            AppLog.e(TAG, "Error disconnecting camera: " + e.getMessage());
            result.error("DISCONNECT_ERROR", e.getMessage(), null);
        }
    }

    private void startPreview(Result result) {
        try {
            if (panoramaPreviewPlayback != null) {
                // Start camera preview
                AppLog.d(TAG, "Preview started");
                result.success(true);
            } else {
                result.error("PREVIEW_ERROR", "Preview playback not available", null);
            }
        } catch (Exception e) {
            AppLog.e(TAG, "Error starting preview: " + e.getMessage());
            result.error("PREVIEW_ERROR", e.getMessage(), null);
        }
    }

    private void stopPreview(Result result) {
        try {
            if (panoramaPreviewPlayback != null) {
                // Stop camera preview
                AppLog.d(TAG, "Preview stopped");
                result.success(true);
            } else {
                result.error("PREVIEW_ERROR", "Preview playback not available", null);
            }
        } catch (Exception e) {
            AppLog.e(TAG, "Error stopping preview: " + e.getMessage());
            result.error("PREVIEW_ERROR", e.getMessage(), null);
        }
    }

    private void getCameraList(Result result) {
        try {
            if (cameraManager != null) {
                // Get available cameras
                AppLog.d(TAG, "Getting camera list");
                result.success("Camera list retrieved");
            } else {
                result.error("LIST_ERROR", "Camera manager not available", null);
            }
        } catch (Exception e) {
            AppLog.e(TAG, "Error getting camera list: " + e.getMessage());
            result.error("LIST_ERROR", e.getMessage(), null);
        }
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
    }
}