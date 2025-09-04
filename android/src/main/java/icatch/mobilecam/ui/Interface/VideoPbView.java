package com.icatch.mobilecam.ui.Interface;

import com.tinyai.libmediacomponent.engine.streaming.MediaStreamPlayer;

/**
 * Created by yh.zhang on 2016/9/14.
 */
public interface VideoPbView {

    void setPanoramaTypeImageResource(int resId);

    void setPanoramaTypeBtnVisibility(int visibility);

    void setMoreSettingLayoutVisibility(int visibility);

    void setEisSwitchChecked(boolean checked);

    void initPreviewPlayerView(MediaStreamPlayer mediaStreamPlayer, boolean enableRender);

    void startPreview();

    void stopPreview();

    void setVideoNameTxv(String title);

    void setBarVisibility(int visibility);


}
