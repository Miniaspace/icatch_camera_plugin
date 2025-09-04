package com.icatch.mobilecam.Presenter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;

import com.icatch.mobilecam.Function.SDKEvent;
import com.icatch.mobilecam.Log.AppLog;
import com.icatch.mobilecam.MyCamera.CameraManager;
import com.icatch.mobilecam.MyCamera.MyCamera;
import com.icatch.mobilecam.Presenter.Interface.BasePresenter;
import com.icatch.mobilecam.R;
import com.icatch.mobilecam.SdkApi.FileOperation;
import com.icatch.mobilecam.SdkApi.PanoramaVideoPlayback;
import com.icatch.mobilecam.SdkApi.StreamStablization;
import com.icatch.mobilecam.SdkApi.mobileapi.VideoStreamingControl;
import com.icatch.mobilecam.data.AppInfo.AppInfo;
import com.icatch.mobilecam.data.GlobalApp.GlobalInfo;
import com.icatch.mobilecam.data.Message.AppMessage;
import com.icatch.mobilecam.data.Mode.TouchMode;
import com.icatch.mobilecam.data.SystemInfo.SystemInfo;
import com.icatch.mobilecam.data.entity.DownloadInfo;
import com.icatch.mobilecam.data.entity.MultiPbItemInfo;
import com.icatch.mobilecam.ui.ExtendComponent.MyProgressDialog;
import com.icatch.mobilecam.ui.ExtendComponent.MyToast;
import com.icatch.mobilecam.ui.Interface.VideoPbView;
import com.icatch.mobilecam.ui.RemoteFileHelper2;
import com.icatch.mobilecam.ui.appdialog.AppDialog;
import com.icatch.mobilecam.ui.appdialog.SingleDownloadDialog;
import com.icatch.mobilecam.utils.MediaRefresh;
import com.icatch.mobilecam.utils.PanoramaTools;
import com.icatch.mobilecam.utils.StorageUtil;
import com.icatch.mobilecam.utils.fileutils.FileOper;
import com.icatch.mobilecam.utils.fileutils.FileTools;
import com.icatch.mobilecam.utils.fileutils.FileUtil;
import com.icatchtek.pancam.customer.ICatchIPancamControl;
import com.icatchtek.pancam.customer.ICatchIPancamVideoPlayback;
import com.icatchtek.pancam.customer.type.ICatchGLPanoramaType;
import com.icatchtek.reliant.customer.type.ICatchFile;
import com.tinyai.libmediacomponent.engine.streaming.GLPoint;
import com.tinyai.libmediacomponent.engine.streaming.IPanoramaControl;
import com.tinyai.libmediacomponent.engine.streaming.IStreamingControl;
import com.tinyai.libmediacomponent.engine.streaming.MediaStreamPlayer;
import com.tinyai.libmediacomponent.engine.streaming.StreamRequest;
import com.tinyai.libmediacomponent.engine.streaming.type.DeviceFile;
import com.tinyai.libmediacomponent.engine.streaming.type.VideoStreamParam;

import java.io.File;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by yh.zhang on 2016/9/14.
 */
public class VideoPbPresenter extends BasePresenter {
    private String TAG = VideoPbPresenter.class.getSimpleName();
    private VideoPbView videoPbView;
    private Activity activity;
    private FileOperation fileOperation;
    private ICatchFile curVideoFile;
    private VideoPbHandler handler = new VideoPbHandler();


    private PanoramaVideoPlayback panoramaVideoPlayback;
    private int curVideoPosition;
    private ExecutorService executor;
    protected Timer downloadProgressTimer;
    private List<MultiPbItemInfo> fileList;
    private SingleDownloadDialog singleDownloadDialog;

    private boolean enableRender = AppInfo.enableRender;
    private int curPanoramaType = ICatchGLPanoramaType.ICH_GL_PANORAMA_TYPE_SPHERE;
    private int fileType;
    private boolean hasDeleted = false;
    private MediaStreamPlayer mediaStreamPlayer;
    private IStreamingControl iStreamingControl;

    private final static float MIN_ZOOM = 0.5f;
    private final static float MAX_ZOOM = 2.2f;
    private TouchMode touchMode = TouchMode.NONE;
    private float mPreviousY;
    private float mPreviousX;
    private float beforeLenght;
    private float afterLenght;
    private float currentZoomRate = MAX_ZOOM;

    public VideoPbPresenter(Activity activity) {
        super(activity);
        this.activity = activity;
        Intent intent = activity.getIntent();
        Bundle data = intent.getExtras();
        curVideoPosition = data.getInt("curfilePosition");
        fileType = data.getInt("fileType");
        fileList = RemoteFileHelper2.getInstance().getLocalFileList(fileType);
        if (fileList != null && fileList.isEmpty() == false) {
            this.curVideoFile = fileList.get(curVideoPosition).iCatchFile;
        }
        AppLog.i(TAG, "cur video fileType=" + fileType + " position=" + curVideoPosition + " video name=" + curVideoFile.getFileName());

    }

    @Override
    public void isAppBackground() {
        stopVideoStream();
        super.isAppBackground();
    }



    public void setView(VideoPbView videoPbView) {
        this.videoPbView = videoPbView;
        initCfg();
        initView();
        initClint();
    }

    private void initView() {
        String fileName = curVideoFile.getFileName();
        int start = fileName.lastIndexOf("/");
        String videoName = fileName.substring(start + 1);

        videoPbView.setVideoNameTxv(videoName);
        if (enableRender && PanoramaTools.isPanorama(curVideoFile.getFileWidth(), curVideoFile.getFileHeight())) {
            videoPbView.setPanoramaTypeBtnVisibility(View.VISIBLE);
        } else {
            videoPbView.setPanoramaTypeBtnVisibility(View.GONE);
        }
    }

    public void initClint() {
        String uid = "";
        MyCamera camera = CameraManager.getInstance().getCurCamera();
        panoramaVideoPlayback = camera.getPanoramaVideoPlayback();
        fileOperation = camera.getFileOperation();
        ICatchIPancamVideoPlayback iCatchIPancamPreview =  camera.getPanoramaSession().getSession().getVideoPlayback();
        ICatchIPancamControl pancamControl = camera.getPanoramaSession().getSession().getControl();
        iStreamingControl = new VideoStreamingControl(iCatchIPancamPreview,pancamControl);
        mediaStreamPlayer = new MediaStreamPlayer(activity, iStreamingControl,uid);
        videoPbView.initPreviewPlayerView(mediaStreamPlayer,enableRender);
        initStreamParam();
    }

    private void initStreamParam(){
        Intent intent = activity.getIntent();
        Bundle data = intent.getExtras();
            //deviceFile = panoramaSession.getCameraFile();
        curVideoPosition = data.getInt("curfilePosition");
        fileType= data.getInt("fileType");
        fileList = RemoteFileHelper2.getInstance().getLocalFileList(fileType);
        if (fileList != null && fileList.isEmpty() == false) {
            this.curVideoFile = fileList.get(curVideoPosition).iCatchFile;
        }
        DeviceFile deviceFile = new DeviceFile(curVideoFile.getFileHandle(),
                    curVideoFile.getFileType(),
                    curVideoFile.getFilePath(),
                    curVideoFile.getFileName(),
                    curVideoFile.getFileSize(),
                    curVideoFile.getFileDate(),
                    curVideoFile.getFrameRate(),
                    (int)curVideoFile.getFileWidth(),
                    (int)curVideoFile.getFileHeight(),
                    curVideoFile.getFileProtection(),
                    curVideoFile.getFileDuration());
        boolean disableAudio = false;
        VideoStreamParam videoStreamParam = new VideoStreamParam(deviceFile,true);
        mediaStreamPlayer.setStreamRequest(new StreamRequest(videoStreamParam,disableAudio));
    }

    public void play() {
        videoPbView.startPreview();
    }

    public void stopVideoStream() {
        videoPbView.stopPreview();
    }

    public void delete() {
        showDeleteEnsureDialog();
    }

    public void download() {
        showDownloadEnsureDialog();
    }

    public void exitPlayback() {
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        stopVideoStream();
        Intent intent = new Intent();
        intent.putExtra("hasDeleted", hasDeleted);
        intent.putExtra("fileType", fileType);
        activity.setResult(1000,intent);
        activity.finish();
    }

    private class VideoPbHandler extends Handler {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case AppMessage.MESSAGE_CANCEL_VIDEO_DOWNLOAD:
                    AppLog.d(TAG, "receive CANCEL_VIDEO_DOWNLOAD_SUCCESS");
                    if (singleDownloadDialog != null) {
                        singleDownloadDialog.dismissDownloadDialog();
                    }
                    if (downloadProgressTimer != null) {
                        downloadProgressTimer.cancel();
                    }
                    if (fileOperation.cancelDownload() == false) {
                        MyToast.show(activity, R.string.dialog_cancel_downloading_failed);
                        break;
                    }
                    try {
                        Thread.currentThread().sleep(200);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    String filePath = StorageUtil.getRootPath(activity) + AppInfo.DOWNLOAD_PATH_VIDEO +
                            curVideoFile.getFileName();
                    File file = new File(filePath);
                    if (file.exists()) {
                        file.delete();
                    }
                    MyToast.show(activity, R.string.dialog_cancel_downloading_succeeded);
                    break;
                case AppMessage.MESSAGE_VIDEO_STREAM_NO_EIS_INFORMATION:
                    enableEIS(false);
                    videoPbView.setEisSwitchChecked(false);
                    break;
            }
        }
    }

    private class DownloadThread implements Runnable {
        private String TAG = "DownloadThread";
        String fileType;
        private String targetPath;
        private String fileName;

        DownloadThread(String targetPath,String fileName){
            this.targetPath = targetPath;
            this.fileName = fileName;
        }

        @Override
        public void run() {
            AppLog.d(TAG, "begin DownloadThread");
            AppInfo.isDownloading = true;

            boolean temp = fileOperation.downloadFile(curVideoFile, targetPath);
            //ICOM-4116 End modify by b.jiang 20170315
            if (temp == false) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (singleDownloadDialog != null) {
                            singleDownloadDialog.dismissDownloadDialog();
                        }
                        if (downloadProgressTimer != null) {
                            downloadProgressTimer.cancel();
                        }
                        MyToast.show(activity, R.string.message_download_failed);
                    }
                });
                AppInfo.isDownloading = false;
                return;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                FileUtil.copyPrivateToDCIM(activity, targetPath, AppInfo.DOWNLOAD_PATH_VIDEO, fileName, true);
            }else {
                if (targetPath.endsWith(".mov") || targetPath.endsWith(".MOV")) {
                    fileType = "video/quicktime";
                } else {
                    fileType = "video/mp4";
                }
                MediaRefresh.addMediaToDB(activity, targetPath, fileType);
            }

            AppLog.d(TAG, "end downloadFile temp =" + temp);
            AppInfo.isDownloading = false;
            final String message = activity.getResources().getString(R.string.message_download_to).replace("$1$", targetPath);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (singleDownloadDialog != null) {
                        singleDownloadDialog.dismissDownloadDialog();
                    }
                    if (downloadProgressTimer != null) {
                        downloadProgressTimer.cancel();
                    }
                    MyToast.show(activity, message);
                }
            });
            AppLog.d(TAG, "end DownloadThread");
        }
    }

    private class DeleteThread implements Runnable {
        @Override
        public void run() {
            Boolean retValue = false;
            retValue = fileOperation.deleteFile(curVideoFile);
            if (retValue == false) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        MyProgressDialog.closeProgressDialog();
                        MyToast.show(activity, R.string.dialog_delete_failed_single);
                    }
                });
            } else {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        MyProgressDialog.closeProgressDialog();
                        RemoteFileHelper2.getInstance().remove(fileList.get(curVideoPosition), fileType);
                        hasDeleted  = true;
                        Intent intent = new Intent();
                        intent.putExtra("hasDeleted", hasDeleted);
                        intent.putExtra("fileType", fileType);
                        activity.setResult(1000,intent);
                        activity.finish();
                    }
                });
            }
            AppLog.d(TAG, "end DeleteThread");
        }
    }

    public void showDownloadEnsureDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setCancelable(false);
        builder.setTitle(R.string.dialog_downloading_single);
        long videoFileSize = 0;
        videoFileSize = fileList.get(curVideoPosition).getFileSizeInteger() / 1024 / 1024;
        AppLog.d(TAG, "video FileSize=" + videoFileSize);
        long minute = videoFileSize / 60;
        long seconds = videoFileSize % 60;

        CharSequence what = activity.getResources().getString(R.string.gallery_download_with_vid_msg).replace("$1$", "1").replace("$3$", String.valueOf
                (seconds)).replace("$2$", String.valueOf(minute));
        builder.setMessage(what);
        builder.setNegativeButton(R.string.gallery_download, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                AppLog.d(TAG, "showProgressDialog");
//                MyProgressDialog.showProgressDialog(activity,R.string.dialog_downloading_single);
                //ICOM-4097
                stopVideoStream();

                if (SystemInfo.getSDFreeSize(activity) < curVideoFile.getFileSize()) {
                    dialog.dismiss();
                    MyToast.show(activity, R.string.text_sd_card_memory_shortage);
                } else {
                    singleDownloadDialog = new SingleDownloadDialog(activity, curVideoFile);
                    singleDownloadDialog.setBackBtnOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            handler.obtainMessage(AppMessage.MESSAGE_CANCEL_VIDEO_DOWNLOAD, 0, 0).sendToTarget();
                        }
                    });
                    singleDownloadDialog.showDownloadDialog();

                    executor = Executors.newSingleThreadExecutor();
                    String path;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        path = activity.getExternalCacheDir().getPath() + AppInfo.DOWNLOAD_PATH_VIDEO;
                    }else {
                        path = StorageUtil.getRootPath(activity) + AppInfo.DOWNLOAD_PATH_VIDEO;
                    }
                    String fileName = curVideoFile.getFileName();
                    AppLog.d(TAG, "------------fileName =" + fileName);
                    FileOper.createDirectory(path);
                    String downloadFilePath = FileTools.chooseUniqueFilename(path + fileName);
                    executor.submit(new DownloadThread(downloadFilePath,fileName), null);
                    downloadProgressTimer = new Timer();
                    downloadProgressTimer.schedule(new DownloadProcessTask(downloadFilePath), 0, 1000);
                }
            }
        });
        builder.setPositiveButton(R.string.gallery_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    public void showDeleteEnsureDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setCancelable(false);
        builder.setTitle(activity.getResources().getString(R.string.gallery_delete_des).replace("$1$", "1"));
        builder.setNegativeButton(R.string.gallery_delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // 这里添加点击确定后的逻辑
                //ICOM-4097
                stopVideoStream();
                MyProgressDialog.showProgressDialog(activity, R.string.dialog_deleting);
                executor = Executors.newSingleThreadExecutor();
                executor.submit(new DeleteThread(), null);
            }
        });
        builder.setPositiveButton(R.string.gallery_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    class DownloadProcessTask extends TimerTask {
        int downloadProgress = 0;
        long fileSize;
        long curFileLength;
        String curDownloadFile ;

        public DownloadProcessTask(String downloadFile){
            curDownloadFile = downloadFile;
        }

        @Override
        public void run() {
            //ICOM-4116 Begin Medify by b.jiang 20170315
//            String path;
//            path = Environment.getExternalStorageDirectory().toString() + AppInfo.DOWNLOAD_PATH_VIDEO;
            File file = new File(curDownloadFile);
            //ICOM-4116 End Medify by b.jiang 20170315
            fileSize = curVideoFile.getFileSize();
            curFileLength = file.length();
            if (file != null) {
                if (curFileLength == fileSize) {
                    downloadProgress = 100;
                } else {
                    downloadProgress = (int) (file.length() * 100 / fileSize);
                }
            } else {
                downloadProgress = 0;
            }
            final DownloadInfo downloadInfo = new DownloadInfo(curVideoFile, fileSize, curFileLength, downloadProgress, false);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (singleDownloadDialog != null) {
                        singleDownloadDialog.updateDownloadStatus(downloadInfo);
                    }
                    AppLog.d(TAG, "update Process downloadProgress=" + downloadProgress);
                }
            });
            AppLog.d(TAG, "end DownloadProcessTask");
        }
    }

    private void rotate(float speedX, float speedY, float speedZ, long timestamp) {
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        if(enableRender){
            panoramaVideoPlayback.rotate(rotation, speedX, speedY, speedZ, timestamp);
        }
    }

    public void setPanoramaType() {
        if(curPanoramaType == ICatchGLPanoramaType.ICH_GL_PANORAMA_TYPE_SPHERE ){
            curPanoramaType = ICatchGLPanoramaType.ICH_GL_PANORAMA_TYPE_ASTEROID;
            videoPbView.setPanoramaTypeImageResource(R.drawable.asteroid);
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        }else if(curPanoramaType == ICatchGLPanoramaType.ICH_GL_PANORAMA_TYPE_ASTEROID){
            curPanoramaType = ICatchGLPanoramaType.ICH_GL_PANORAMA_TYPE_VIRTUAL_R;
            videoPbView.setPanoramaTypeImageResource(R.drawable.vr);
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        }else{
            curPanoramaType = ICatchGLPanoramaType.ICH_GL_PANORAMA_TYPE_SPHERE;
            videoPbView.setPanoramaTypeImageResource(R.drawable.panorama);
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        }

        if(iStreamingControl != null){
            IPanoramaControl panoramaControl =  iStreamingControl.getPanoramaControl();
            if(panoramaControl != null){
                panoramaControl.changePanoramaType(curPanoramaType);
            }
        }
    }

    public void showMoreSettingLayout(boolean isShowBar) {
        if (isShowBar) {
            videoPbView.setMoreSettingLayoutVisibility(View.VISIBLE);
            videoPbView.setBarVisibility(View.GONE);
        } else {
            videoPbView.setMoreSettingLayoutVisibility(View.GONE);
            videoPbView.setBarVisibility(View.VISIBLE);
        }
    }

    public void enableEIS(boolean enable) {
        StreamStablization streamStablization = panoramaVideoPlayback.getStreamStablization();
        if (streamStablization == null) {
            return;
        }
        if (enable) {
            streamStablization.enableStablization();
        } else {
            streamStablization.disableStablization();
        }
    }

    public void setSdCardEventListener() {
        GlobalInfo.getInstance().setOnEventListener(new GlobalInfo.OnEventListener() {
            @Override
            public void eventListener(int sdkEventId) {
                switch (sdkEventId){
                    case SDKEvent.EVENT_SDCARD_REMOVED:
                        stopVideoStream();
//                        videoStreaming.stopForSdRemove();
                        RemoteFileHelper2.getInstance().clearAllFileList();
                        AppDialog.showDialogWarn(activity, R.string.dialog_card_removed_and_back, false,new AppDialog.OnDialogSureClickListener() {
                            @Override
                            public void onSure() {
                                exitPlayback();
                            }
                        });
                        break;
//                    case SDKEvent.EVENT_SDCARD_INSERT:
//                        MyToast.show(activity,R.string.dialog_card_inserted);
//                        break;
                }
            }
        });
    }

    public void onSufaceViewTouchDown(MotionEvent event) {
        touchMode = TouchMode.DRAG;
        mPreviousY = event.getY();
        mPreviousX = event.getX();
        beforeLenght = 0;
        afterLenght = 0;
    }

    public void onSufaceViewPointerDown(MotionEvent event) {
        if (event.getPointerCount() == 2) {
            touchMode = TouchMode.ZOOM;
            beforeLenght = getDistance(event);//
        }
    }

    public void onSufaceViewTouchMove(MotionEvent event) {
        if (touchMode == TouchMode.DRAG) {
            rotateB(event, mPreviousX, mPreviousY);
            mPreviousY = event.getY();
            mPreviousX = event.getX();
        } else if (touchMode == TouchMode.ZOOM) {
            afterLenght = getDistance(event);//
            float gapLenght = afterLenght - beforeLenght;
            if (Math.abs(gapLenght) > 5f) {
                float scale_temp = afterLenght / beforeLenght;
                this.setScale(scale_temp);
                beforeLenght = afterLenght;
            }
        }
    }

    public void onSufaceViewTouchUp() {
        touchMode = TouchMode.NONE;
    }

    public void onSufaceViewTouchPointerUp() {
        touchMode = TouchMode.NONE;
    }

    private float getDistance(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) StrictMath.sqrt(x * x + y * y);
    }

    public void rotateB(MotionEvent e, float prevX, float prevY) {
        GLPoint prev = new GLPoint(prevX, prevY);
        GLPoint curr = new GLPoint(e.getX(), e.getY());
        if(enableRender &&  iStreamingControl != null){
            IPanoramaControl panoramaControl =  iStreamingControl.getPanoramaControl();
            if(panoramaControl != null){
                panoramaControl.rotate(prev,curr);
            }
        }
    }

    private void setScale(float scale) {
        if ((currentZoomRate >= MAX_ZOOM && scale > 1) || (currentZoomRate <= MIN_ZOOM && scale < 1)) {
            return;
        }
        float temp = currentZoomRate * scale;
        if (scale > 1) {
            if (temp <= MAX_ZOOM) {
                currentZoomRate = currentZoomRate * scale;
                zoom(currentZoomRate);
            } else {
                currentZoomRate = MAX_ZOOM;
                zoom(currentZoomRate);
            }
        } else if (scale < 1) {
            if (temp >= MIN_ZOOM) {
                currentZoomRate = currentZoomRate * scale;
                zoom(currentZoomRate);
            } else {
                currentZoomRate = MIN_ZOOM;
                zoom(currentZoomRate);
            }
        }

    }

    private void zoom(float currentZoomRate) {
        locate(1 / currentZoomRate);
    }

    public void locate(float progerss) {
        if(enableRender && iStreamingControl != null){
            IPanoramaControl panoramaControl =  iStreamingControl.getPanoramaControl();
            if(panoramaControl != null){
                panoramaControl.glTransformLocate(progerss);
            }
        }
    }
}

