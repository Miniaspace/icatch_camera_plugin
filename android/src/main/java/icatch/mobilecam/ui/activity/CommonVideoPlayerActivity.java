package com.icatch.mobilecam.ui.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.icatch.mobilecam.Function.streaming.VideoStreaming;
import com.icatch.mobilecam.MyCamera.CameraManager;
import com.icatch.mobilecam.MyCamera.LocalSession;
import com.icatch.mobilecam.MyCamera.MyCamera;
import com.icatch.mobilecam.Log.AppLog;
import com.icatch.mobilecam.Presenter.VideoPbPresenter;
import com.icatch.mobilecam.R;
import com.icatch.mobilecam.SdkApi.mobileapi.VideoStreamingControl;
import com.icatch.mobilecam.data.Mode.VideoPbMode;
import com.icatch.mobilecam.data.entity.MultiPbItemInfo;
import com.icatch.mobilecam.data.type.FileType;
import com.icatch.mobilecam.ui.ExtendComponent.MyProgressDialog;
import com.icatch.mobilecam.ui.RemoteFileHelper;
// import com.icatchtek.baseutil.log.AppLog;
import android.util.Log;
import com.icatchtek.pancam.customer.ICatchIPancamControl;
import com.icatchtek.pancam.customer.ICatchIPancamVideoPlayback;
import com.icatchtek.reliant.customer.type.ICatchFile;
import com.tinyai.libmediacomponent.components.media.PreviewControlView;
import com.tinyai.libmediacomponent.components.media.VideoControlView;
import com.tinyai.libmediacomponent.components.media.VideoPlayerView;
import com.tinyai.libmediacomponent.engine.streaming.IStreamingControl;
import com.tinyai.libmediacomponent.engine.streaming.MediaStreamPlayer;
import com.tinyai.libmediacomponent.engine.streaming.StreamRequest;
import com.tinyai.libmediacomponent.engine.streaming.type.DeviceFile;
import com.tinyai.libmediacomponent.engine.streaming.type.VideoStreamParam;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CommonVideoPlayerActivity extends MobileCamBaseActivity {

    private  boolean remote = false;
    private MediaStreamPlayer mediaStreamPlayer;
    private VideoPlayerView videoPlayerView;
    private static final String TAG = "CommonVideoPlayerActivity";
    private FileType fileType;
    private boolean hasDeleted = false;
    private List<MultiPbItemInfo> fileList;
    private int curVideoPosition;
    private ICatchFile curVideoFile;
    private ExecutorService executor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common_video_player);
        videoPlayerView = findViewById(R.id.video_player_view);
        videoPlayerView.setDeleteBtnVisibility(View.GONE);
        videoPlayerView.setDownloadBtnVisibility(View.GONE);
        videoPlayerView.setContainerBackground(R.color.black);
        initClient();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // do not display menu bar
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startStream();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopStream();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroySession();
    }

    public void destroySession() {
        if(!remote){
            LocalSession.getInstance().destroyPanoramaSession();
        }
    }


    private void initClient(){
        String uid = "";
        Intent intent = getIntent();
        Bundle data = intent.getExtras();
        remote = data.getBoolean("remote",false);
        if(remote){
            MyCamera camera = CameraManager.getInstance().getCurCamera();
            ICatchIPancamVideoPlayback iCatchIPancamPreview =  camera.getPanoramaSession().getSession().getVideoPlayback();
            ICatchIPancamControl pancamControl = camera.getPanoramaSession().getSession().getControl();
            IStreamingControl iStreamingControl = new VideoStreamingControl(iCatchIPancamPreview,pancamControl);;
            mediaStreamPlayer = new MediaStreamPlayer(this, iStreamingControl,uid);
        }else {
            LocalSession.getInstance().preparePanoramaSession();
            ICatchIPancamVideoPlayback iCatchIPancamPreview =  LocalSession.getInstance().getPanoramaSession().getSession().getVideoPlayback();
            ICatchIPancamControl pancamControl = LocalSession.getInstance().getPanoramaSession().getSession().getControl();
            IStreamingControl iStreamingControl = new VideoStreamingControl(iCatchIPancamPreview,pancamControl);;
            mediaStreamPlayer = new MediaStreamPlayer(this, iStreamingControl,uid);
        }
        videoPlayerView.setStream(mediaStreamPlayer);
        initStreamParam();
    }


    private void initStreamParam(){
        DeviceFile deviceFile = null;
        Intent intent = getIntent();
        Bundle data = intent.getExtras();
        if(remote){
            //deviceFile = panoramaSession.getCameraFile();
            curVideoPosition = data.getInt("curfilePosition");
            int fileTypeInt = data.getInt("fileType");
            fileType = FileType.values()[fileTypeInt];
            fileList = RemoteFileHelper.getInstance().getLocalFileList(fileType);
            if (fileList != null && fileList.isEmpty() == false) {
                this.curVideoFile = fileList.get(curVideoPosition).iCatchFile;
            }
            deviceFile = new DeviceFile(curVideoFile.getFileHandle(),
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
            String fileName = curVideoFile.getFileName();
            int start = fileName.lastIndexOf("/");
            String videoName = fileName.substring(start + 1);
            videoPlayerView.setTitle(videoName);
        }else {
            String curLocalVideoPath = data.getString("curfilePath");
            deviceFile =  new DeviceFile(0, 2, curLocalVideoPath, curLocalVideoPath, 0);;
            int start = curLocalVideoPath.lastIndexOf("/");
            String videoName = curLocalVideoPath.substring(start + 1);
            videoPlayerView.setTitle(videoName);
        }

        if(deviceFile == null){
            AppLog.d(TAG,"startStream deviceFile is null");
            return;
        }
        boolean disableAudio = false;
        VideoStreamParam videoStreamParam = new VideoStreamParam(deviceFile,remote);
        mediaStreamPlayer.setStreamRequest(new StreamRequest(videoStreamParam,disableAudio));

        if(!remote){
            videoPlayerView.setDownloadBtnVisibility(View.GONE);
        }
        videoPlayerView.setControlBarClickListener(new VideoControlView.OnControlBarClickListener() {
            @Override
            public void onDownloadClick() {
                download();
            }

            @Override
            public void onDeleteClick() {
                delete();
            }

            @Override
            public void onBackClick() {
                back();
            }

            @Override
            public void onMoreClick() {

            }
        });

        videoPlayerView.setFullScreenModeChangedListener(new VideoControlView.OnFullScreenModeChangedListener() {
            @Override
            public void onFullScreenModeChanged(boolean isFullScreen) {
                //处理全屏切换
                AppLog.d(TAG,"onFullScreenModeChanged isFullScreen:" + isFullScreen);
                if(isFullScreen){
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                }else {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }
            }
        });
    }

    private void startStream(){
        AppLog.d(TAG,"startStream");
        videoPlayerView.startPreview();
    }

    private void stopStream(){
        AppLog.d(TAG,"stopStream");
        videoPlayerView.stopPreview();
    }

    private void back(){
        stopStream();

        if(remote){
            Intent intent = new Intent();
            intent.putExtra("hasDeleted", hasDeleted);
            intent.putExtra("fileType", fileType.ordinal());
            setResult(1000,intent);
            finish();
        }else {
            finish();
        }
    }

    private void download(){

    }



    private void delete() {

//        showDeleteEnsureDialog();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        AppLog.d(TAG,"onConfigurationChanged newConfig.orientation:" + newConfig.orientation);
        if(newConfig.orientation==Configuration.ORIENTATION_PORTRAIT){
//            Toast.makeText(this, "现在是竖屏", Toast.LENGTH_SHORT).show();
            videoPlayerView.setFullScreenStatus(false);

        }else {
//            Toast.makeText(this, "现在是横屏", Toast.LENGTH_SHORT).show();
            videoPlayerView.setFullScreenStatus(true);
        }
    }

    //    public void showDeleteEnsureDialog() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setCancelable(false);
//        builder.setTitle(getResources().getString(R.string.gallery_delete_des).replace("$1$", "1"));
//        builder.setNegativeButton(R.string.gallery_delete, new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int whichButton) {
//                // 这里添加点击确定后的逻辑
//                //ICOM-4097
//
//                    stopStream();
//
//                MyProgressDialog.showProgressDialog(CommonVideoPlayerActivity.this, R.string.dialog_deleting);
//                executor = Executors.newSingleThreadExecutor();
//                executor.submit(new VideoPbPresenter.DeleteThread(), null);
//            }
//        });
//        builder.setPositiveButton(R.string.gallery_cancel, new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int whichButton) {
//                // 这里添加点击确定后的逻辑
//                //ICOM-4097
//                dialog.dismiss();
//            }
//        });
//        builder.create().show();
//    }
}