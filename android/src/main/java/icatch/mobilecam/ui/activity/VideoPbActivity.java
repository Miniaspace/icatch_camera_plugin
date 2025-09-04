package com.icatch.mobilecam.ui.activity;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.icatch.mobilecam.Log.AppLog;
import com.icatch.mobilecam.Presenter.VideoPbPresenter;
import com.icatch.mobilecam.R;
import com.icatch.mobilecam.ui.Interface.VideoPbView;
import com.tinyai.libmediacomponent.components.media.VideoControlView;
import com.tinyai.libmediacomponent.components.media.VideoPlayerView;
import com.tinyai.libmediacomponent.components.media.type.PreviewRenderType;
import com.tinyai.libmediacomponent.engine.streaming.MediaStreamPlayer;

public class VideoPbActivity extends MobileCamBaseActivity implements VideoPbView {
    private String TAG = VideoPbActivity.class.getSimpleName();

    private VideoPbPresenter presenter;
    private ImageButton panoramaTypeBtn;
    private LinearLayout moreSettingLayout;
    private ImageButton cancelBtn;
    private Switch eisSwitch;
    private TextView deleteTxv;
    private VideoPlayerView videoPlayerView;
    private boolean isFullScreen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_pb);
        videoPlayerView = findViewById(R.id.video_player_view);
        moreSettingLayout = (LinearLayout) findViewById(R.id.more_setting_layout);
        cancelBtn = (ImageButton) findViewById(R.id.cancel_btn);
        eisSwitch = (Switch) findViewById(R.id.eis_switch);
        deleteTxv  = (TextView) findViewById(R.id.delete_txv);
        panoramaTypeBtn = (ImageButton) findViewById(R.id.panorama_type_btn);

        presenter = new VideoPbPresenter(this);
        presenter.setView(this);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // do not display menu bar
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                presenter.showMoreSettingLayout(false);
            }
        });

        eisSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isChecked = eisSwitch.isChecked();
                presenter.enableEIS(isChecked);
            }
        });

        deleteTxv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                presenter.delete();
            }
        });

        videoPlayerView.setControlBarClickListener(new VideoControlView.OnControlBarClickListener() {
            @Override
            public void onDownloadClick() {
                presenter.download();
            }

            @Override
            public void onDeleteClick() {
                presenter.delete();
            }

            @Override
            public void onBackClick() {
                backClick();
            }

            @Override
            public void onMoreClick() {
                presenter.showMoreSettingLayout(true);
            }
        });
        videoPlayerView.setFullScreenModeChangedListener(new VideoControlView.OnFullScreenModeChangedListener() {
            @Override
            public void onFullScreenModeChanged(boolean isFullScreen) {
                AppLog.d(TAG,"onFullScreenModeChanged isFullScreen:" + isFullScreen);
                if(isFullScreen){
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                }else {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }
            }
        });

        videoPlayerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
//                AppLog.d(TAG,"onTouch event:" + (event.getAction() & MotionEvent.ACTION_MASK));
                switch (event.getAction() & MotionEvent.ACTION_MASK) {

                    case MotionEvent.ACTION_DOWN:
                        presenter.onSufaceViewTouchDown(event);
                        break;
                    case MotionEvent.ACTION_POINTER_DOWN:
                        presenter.onSufaceViewPointerDown(event);
                        break;

                    case MotionEvent.ACTION_MOVE:
                        presenter.onSufaceViewTouchMove(event);
                        break;
                    case MotionEvent.ACTION_UP:
                        presenter.onSufaceViewTouchUp();
                        break;

                    case MotionEvent.ACTION_POINTER_UP:
                        presenter.onSufaceViewTouchPointerUp();
                        break;
                }
                return false;
            }
        });

        panoramaTypeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                presenter.setPanoramaType();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.submitAppInfo();
        presenter.setSdCardEventListener();
        presenter.play();
    }

    @Override
    protected void onPause() {
        super.onPause();
        presenter.stopVideoStream();
    }

    @Override
    protected void onStop() {
        super.onStop();
        presenter.isAppBackground();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.removeActivity();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_HOME:
                Log.d("AppStart", "home");
                break;
            case KeyEvent.KEYCODE_BACK:
                Log.d("AppStart", "back");
                backClick();
                break;
            default:
                return super.onKeyDown(keyCode, event);
        }
        return true;
    }


    @Override
    public void setPanoramaTypeImageResource(int resId) {
        panoramaTypeBtn.setImageResource(resId);
    }

    @Override
    public void setPanoramaTypeBtnVisibility(int visibility) {
        panoramaTypeBtn.setVisibility(visibility);
    }

    @Override
    public void setMoreSettingLayoutVisibility(int visibility) {
        moreSettingLayout.setVisibility(visibility);
    }

    @Override
    public void setEisSwitchChecked(boolean checked) {
        eisSwitch.setChecked(checked);
    }

    @Override
    public void initPreviewPlayerView(MediaStreamPlayer mediaStreamPlayer, boolean enableRender) {
        if(videoPlayerView == null){
            return;
        }
//        videoPlayerView.setAlwaysHideBar(true);
        videoPlayerView.setRenderType(enableRender? PreviewRenderType.SDK_RENDER : PreviewRenderType.APP_RENDER);
//        previewPlayerView.setRenderType( PreviewRenderType.APP_RENDER);
        videoPlayerView.setStream(mediaStreamPlayer);
    }

    @Override
    public void startPreview() {
        if(videoPlayerView == null){
            return;
        }
        videoPlayerView.startPreview();
    }

    @Override
    public void stopPreview() {
        if(videoPlayerView == null){
            return;
        }
        videoPlayerView.stopPreview();
    }

    @Override
    public void setVideoNameTxv(String title) {
        if(videoPlayerView == null){
            return;
        }
        videoPlayerView.setTitle(title);
    }

    @Override
    public void setBarVisibility(int visibility) {
        if(videoPlayerView == null){
            return;
        }
        videoPlayerView.setAlwaysHideBar(visibility  ==View.VISIBLE ? false: true);
//        videoPlayerView.setButtomBarVisibility(visibility);
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(newConfig.orientation==Configuration.ORIENTATION_PORTRAIT){
            videoPlayerView.setFullScreenStatus(false);
            isFullScreen =false;
        }else {
            videoPlayerView.setFullScreenStatus(true);
            isFullScreen =true;
        }
        AppLog.d(TAG, "onConfigurationChanged newConfig Orientation=" + newConfig.orientation);
    }

    private void backClick(){
        if(isFullScreen){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            return;
        }
        presenter.exitPlayback();
    }
}

