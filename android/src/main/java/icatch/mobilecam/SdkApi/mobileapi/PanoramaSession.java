package com.icatch.mobilecam.SdkApi.mobileapi;

import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.Log;

import com.icatch.mobilecam.Log.AppLog;
import com.icatchtek.control.customer.ICatchCameraControl;
import com.icatchtek.control.customer.ICatchCameraPlayback;
import com.icatchtek.control.customer.ICatchCameraSession;
import com.icatchtek.control.customer.exception.IchCameraModeException;
import com.icatchtek.control.customer.exception.IchDevicePropException;
import com.icatchtek.control.customer.exception.IchNoSuchPathException;
import com.icatchtek.pancam.customer.ICatchIPancamControl;
import com.icatchtek.pancam.customer.ICatchIPancamPreview;
import com.icatchtek.pancam.customer.ICatchIPancamVideoPlayback;
import com.icatchtek.pancam.customer.ICatchPancamSession;
import com.icatchtek.pancam.customer.type.ICatchGLColor;
import com.icatchtek.pancam.customer.type.ICatchGLDisplayPPI;
import com.icatchtek.reliant.customer.exception.IchInvalidArgumentException;
import com.icatchtek.reliant.customer.exception.IchInvalidSessionException;
import com.icatchtek.reliant.customer.exception.IchSocketException;
import com.icatchtek.reliant.customer.exception.IchTransportException;
import com.icatchtek.reliant.customer.transport.ICatchINETTransport;
import com.icatchtek.reliant.customer.transport.ICatchITransport;
import com.icatchtek.reliant.customer.type.ICatchFile;
import com.icatchtek.reliant.customer.type.ICatchFileType;
import com.tinyai.libmediacomponent.engine.streaming.IStreamingControl;
import com.tinyai.libmediacomponent.engine.streaming.type.DeviceFile;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by zhang yanhu C001012 on 2016/6/22 11:41.
 */
public class PanoramaSession {
    private static final String TAG = PanoramaSession.class.getSimpleName();
    private ICatchPancamSession iCatchPancamSession;
    private ICatchCameraSession iCatchCameraSession;
    private ICatchINETTransport transport;
    private boolean isConnected = false;
    private boolean cameraSessionPrepared = false;

    public boolean connect(boolean enablePTPIP,String localIP,String remoteIP) {
        AppLog.d(TAG,"connect enablePTPIP=" + enablePTPIP);
        boolean ret = false;

        transport = new ICatchINETTransport(remoteIP,localIP);

        AppLog.i(TAG, "transport is"+  transport.getClass().getSimpleName());
        try {
            transport.prepareTransport();
        } catch (IchTransportException e) {
            AppLog.i(TAG, " IchTransportException");
            e.printStackTrace();
        }
        ret = prepareSession(transport);
        ret = prepareCameraSession(transport,enablePTPIP);
        if (ret) {
            isConnected = true;
        }
        setCameraDate();
        return ret;
    }

    public boolean disconnect() {
        if (isConnected) {
            if(transport != null){
                try {
                    transport.destroyTransport();
                } catch (IchTransportException e) {
                    e.printStackTrace();
                }
            }
            destroySession();
            destroyCameraSession();
            isConnected = false;
        }
        return true;
    }

    public boolean prepareSession(ICatchITransport transport) {
        boolean ret = false;
        iCatchPancamSession = ICatchPancamSession.createSession();
        DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
        ICatchGLDisplayPPI displayPPI = new ICatchGLDisplayPPI(displayMetrics.xdpi, displayMetrics.ydpi);
        try {
            ret = iCatchPancamSession.prepareSession(transport, ICatchGLColor.BLACK, displayPPI);
        } catch (IchTransportException e) {
            e.printStackTrace();
        }
        AppLog.d(TAG, "ICatchPancamSession preparePanoramaSession ret=" + ret);
        return ret;
    }

    public boolean prepareCameraSession(ICatchITransport itrans,boolean enablePTPIP) {
        // TODO Auto-generated constructor stub
        AppLog.d(TAG, "start prepareCameraSession itrans="+ itrans + " enablePTPIP=" +enablePTPIP);
        if (enablePTPIP) {
            try {
                ICatchCameraSession.getCameraConfig(itrans).enablePTPIP();
            } catch (IchInvalidArgumentException e) {
                AppLog.e(TAG, "enablePTPIP IchInvalidArgumentException");
                e.printStackTrace();
            }
        } else {
            try {
                ICatchCameraSession.getCameraConfig(itrans).disablePTPIP();
            } catch (IchInvalidArgumentException e) {
                AppLog.e(TAG, "disablePTPIP IchInvalidArgumentException");
                e.printStackTrace();
            }
        }

        cameraSessionPrepared = true;
        AppLog.d(TAG, "start createSession");
        iCatchCameraSession = ICatchCameraSession.createSession();
        boolean retValue = false;
        try {
            retValue = iCatchCameraSession.prepareSession(itrans);
        } catch (IchTransportException e) {
            e.printStackTrace();
        }
        if (retValue == false) {
            AppLog.e(TAG, "failed to prepareCameraSession");
            cameraSessionPrepared = false;
            Log.v("1111", "CommandSession,prepareCameraSession fail!");
        }
        AppLog.d(TAG, "end prepareCameraSession ret=" + cameraSessionPrepared);
        return cameraSessionPrepared;
    }

    public boolean destroyCameraSession() {
        AppLog.i(TAG, "Start destroyPanoramaSession");
        Boolean retValue = false;
        try {
            retValue = iCatchCameraSession.destroySession();
            AppLog.i(TAG, "End  destroyPanoramaSession,retValue=" + retValue);
        } catch (IchInvalidSessionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return retValue;
    }

    public ICatchPancamSession getSession() {
        return iCatchPancamSession;
    }

    public IStreamingControl getPreviewStream(){
        ICatchIPancamPreview iCatchIPancamPreview =  iCatchPancamSession.getPreview();
        ICatchCameraControl cameraControl = null;
        try {
             cameraControl = iCatchCameraSession.getControlClient();
        } catch (IchInvalidSessionException e) {
            e.printStackTrace();
        }
        PreviewStreamingControl previewStreaming = new PreviewStreamingControl(iCatchIPancamPreview,cameraControl);

        return previewStreaming;

    }

    public DeviceFile getCameraFile(){
        DeviceFile deviceFile = null;
        ICatchCameraPlayback cameraPlayback = null;
        try {
            cameraPlayback = iCatchCameraSession.getPlaybackClient();
        } catch (IchInvalidSessionException e) {
            e.printStackTrace();
        }

        if(cameraPlayback != null){
            List<ICatchFile> iCatchFiles = null;
            AppLog.d(TAG,"getCameraFile");
            try {
//                int filterFileType = ICatchCamListFileFilter.ICH_OFC_TYPE_VIDEO;
//                cameraPlayback.setFileListAttribute(filterFileType, ICatchCamListFileFilter.ICH_TAKEN_BY_ALL_SENSORS);
                int fileCount = cameraPlayback.getFileCount();
                AppLog.d(TAG,"getCameraFile fileCount:" + fileCount);
                iCatchFiles = cameraPlayback.listFiles(ICatchFileType.ICH_FILE_TYPE_ALL,1,fileCount,5);
            } catch (IchSocketException e) {
                e.printStackTrace();
            } catch (IchCameraModeException e) {
                e.printStackTrace();
            } catch (IchNoSuchPathException e) {
                e.printStackTrace();
            } catch (IchInvalidSessionException e) {
                e.printStackTrace();
            } catch (IchInvalidArgumentException e) {
                e.printStackTrace();
            }

            AppLog.d(TAG,"End getCameraFile iCatchFiles:" + iCatchFiles);
            if(iCatchFiles !=null && iCatchFiles.size()>0){
                ICatchFile file = iCatchFiles.get(0);
                //int fileHandle,
                // int fileType,
                // String filePath,
                // String fileName,
                // long fileSize,
                // String fileDate,
                // double frameRate,
                // int fileWidth,
                // int fileHeight,
                // int fileProtection,
                // int fileDuration
                deviceFile = new DeviceFile(file.getFileHandle(),
                        file.getFileType(),
                        file.getFilePath(),
                        file.getFileName(),
                        file.getFileSize(),
                        file.getFileDate(),
                        file.getFrameRate(),
                        (int)file.getFileWidth(),
                        (int)file.getFileHeight(),
                        file.getFileProtection(),
                        file.getFileDuration());
            }
        }
        return deviceFile;
    }

    public IStreamingControl getVideoStream(){
        ICatchIPancamVideoPlayback iCatchIPancamPreview =  iCatchPancamSession.getVideoPlayback();
        ICatchIPancamControl pancamControl = iCatchPancamSession.getControl();
//        ICatchCameraControl cameraControl = null;
//        try {
//            cameraControl = iCatchCameraSession.getControlClient();
//        } catch (IchInvalidSessionException e) {
//            e.printStackTrace();
//        }
        VideoStreamingControl previewStreaming = new VideoStreamingControl(iCatchIPancamPreview,pancamControl);
        return previewStreaming;

    }

    public boolean destroySession() {
        boolean ret = false;
        if (iCatchPancamSession != null) {
            try {
                ret = iCatchPancamSession.destroySession();
            } catch (IchInvalidSessionException e) {
                e.printStackTrace();
            }
            AppLog.d(TAG, "ICatchPancamSession destroyPanoramaSession ret=" + ret);
        }
        return ret;
    }

    public boolean setCameraDate() {
        long time = System.currentTimeMillis();
        Date date = new Date(time);
        SimpleDateFormat myFmt = new SimpleDateFormat("yyyyMMdd HHmmss");
        String tempDate = myFmt.format(date);
        tempDate = tempDate.replaceAll(" ", "T");
        tempDate = tempDate + ".0";
        AppLog.i(TAG, "start setCameraDate date = " + tempDate);
        boolean retValue = false;
        try {
            retValue = iCatchCameraSession.getPropertyClient().setStringPropertyValue(0x5011, tempDate);
        } catch (IchSocketException e) {
            AppLog.e(TAG, "IchSocketException");
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IchCameraModeException e) {
            AppLog.e(TAG, "IchCameraModeException");
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IchInvalidSessionException e) {
            AppLog.e(TAG, "IchInvalidSessionException");
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IchDevicePropException e) {
            AppLog.e(TAG, "IchDevicePropException");
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        AppLog.i(TAG, "end setCameraDate retValue =" + retValue);
        return retValue;
    }
}
