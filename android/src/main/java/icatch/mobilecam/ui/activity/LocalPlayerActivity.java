package com.icatch.mobilecam.ui.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.util.EventLogger;
import com.google.android.exoplayer2.util.MimeTypes;
import com.icatch.mobilecam.Log.AppLog;
import com.icatch.mobilecam.R;
import com.icatch.mobilecam.data.GlobalApp.GlobalInfo;
// import com.icatchtek.basecomponent.prompt.MyProgressDialog;
import com.icatch.mobilecam.utils.ClickUtils;
// import com.icatchtek.baseutil.device.MyOrientoinListener; // 注释掉不存在的依赖
// import com.icatchtek.baseutil.device.ScreenUtils;
// import com.icatchtek.baseutil.info.SystemInfo;
import android.app.ProgressDialog;
import android.util.DisplayMetrics;
import com.tinyai.libmediacomponent.components.filelist.FileItemInfo;

import java.util.List;

public class LocalPlayerActivity extends MobileCamBaseActivity {

    private final static String TAG = LocalPlayerActivity.class.getSimpleName();
    private PlayerView playerView;
    private SimpleExoPlayer player;

    private Handler handler = new Handler();
    private boolean playWhenReady = true;
    private int currentWindow = 0;
    private long playbackPosition = 0;
    private String videoPath;
    private int fileIndex;
    private boolean orientationIsVertical = true;
    private ImageButton fullscreen_switch;
    private ImageView topbar_back;
    private RelativeLayout video_view_layout;
    private TextView titleTxv;
    // private MyOrientoinListener orientationEventListener; // 注释掉不存在的依赖

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_player);

        playerView = findViewById(R.id.player_view);
        fullscreen_switch = findViewById(com.google.android.exoplayer2.ui.R.id.exo_fullscreen);
        topbar_back = findViewById(R.id.exo_top_bar_back);
        video_view_layout = findViewById(R.id.video_view_layout);
        titleTxv = findViewById(R.id.exo_top_bar_title);
        Intent intent = getIntent();
        Bundle data = intent.getExtras();
        videoPath = data.getString("curfilePath");
        fileIndex = data.getInt("curfilePosition");
        AppLog.i(TAG, "videoPath=" + videoPath);
        AppLog.i(TAG, "fileIndex=" + fileIndex);
        if(videoPath != null){
            int start = videoPath.lastIndexOf("/");
            String videoName = videoPath.substring(start + 1);
            titleTxv.setText(videoName);
        }
        topbar_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBack();
            }
        });
        fullscreen_switch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ClickUtils.isFastDoubleClick(v)){
                    AppLog.i(TAG, "isFastDoubleClick the v.id=" + v.getId());
                    return;
                }
                if (!orientationIsVertical) {
                    setPvLayout(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                } else {
                    setPvLayout(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                }
            }
        });

        initializePlayer();
        // orientationEventListener = new MyOrientoinListener(this, this); // 注释掉不存在的依赖

        // if (orientationEventListener != null) {
        //     orientationEventListener.enable();
        // } // 注释掉不存在的依赖
        setPvLayout(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

    }

    private void initializePlayer() {
//        playbackStateListener = new PlaybackStateListener();
        if (player == null) {

            DefaultTrackSelector trackSelector = new DefaultTrackSelector(this);
            trackSelector.setParameters(
                    trackSelector.buildUponParameters().setMaxVideoSizeSd());
            player = new SimpleExoPlayer.Builder(this)
                    .setTrackSelector(trackSelector)
                    .build();

            player.addAnalyticsListener(new EventLogger(trackSelector, "ExoPlayerDebug"));

            playerView.setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS);
            playerView.setShowNextButton(false);
            playerView.setShowPreviousButton(false);
            playerView.setShowRewindButton(false);
            playerView.setShowFastForwardButton(false);


//            player.addListener(playbackStateListener);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    player.setPlayWhenReady(playWhenReady);
                    playerView.setPlayer(player);
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        play();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stop();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // if (orientationEventListener != null) {
        //     orientationEventListener.disable();
        // } // 注释掉不存在的依赖
        releasePlayer();
    }

    private void play(){
//        String url = getExternalCacheDir() + "/001.mp4";
        List<FileItemInfo>  fileList = GlobalInfo.getInstance().getLocalVideoList();
        FileItemInfo file = fileList.get(fileIndex);
        String mimeType = MimeTypes.APPLICATION_MP4;
        MediaItem mediaItem;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            mediaItem = new MediaItem.Builder()
                    .setUri(file.getUri())
                    .setMimeType(mimeType)
                    .build();
        }else {
            mediaItem = new MediaItem.Builder()
                    .setUri(file.getFilePath())
                    .setMimeType(mimeType)
                    .build();
        }
        handler.post(new Runnable() {
            @Override
            public void run() {
                player.setPlayWhenReady(playWhenReady);
                player.setMediaItem(mediaItem);

                player.seekTo(currentWindow, playbackPosition);
                player.prepare();
//                MyProgressDialog.closeProgressDialog();

            }
        });

    }

    private void stop(){
        if (player.isPlaying() || player.isLoading()) {
            player.stop();
        }
    }

    private void releasePlayer() {
        if (player != null) {
            playbackPosition = player.getCurrentPosition();
            playWhenReady = player.getPlayWhenReady();
            player.release();
            player = null;
        }
    }

    private void onBack(){
        AppLog.d(TAG, "back");
        if (!orientationIsVertical) {
            setPvLayout(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            finish();
        }
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_HOME:
                Log.d("AppStart", "home");
                break;
            case KeyEvent.KEYCODE_BACK:
                AppLog.d(TAG, "back");
                onBack();
                break;
            default:
                return super.onKeyDown(keyCode, event);
        }
        return true;
    }

    public void setPvLayout(int requestedOrientation) {
        //竖屏
        if (requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            fullscreen_switch.setImageResource(com.google.android.exoplayer2.ui.R.drawable.exo_icon_fullscreen_enter);
            orientationIsVertical = true;
            video_view_layout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            // ScreenUtils.setPortrait(this); // 注释掉不存在的依赖
        } else if (requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE || requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE) {
            fullscreen_switch.setImageResource(com.google.android.exoplayer2.ui.R.drawable.exo_icon_fullscreen_exit);
            //横屏
            // ScreenUtils.setLandscape(this, requestedOrientation); // 注释掉不存在的依赖
            video_view_layout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            orientationIsVertical = false;
        }
    }
}