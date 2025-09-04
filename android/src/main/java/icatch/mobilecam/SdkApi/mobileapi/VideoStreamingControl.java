package com.icatch.mobilecam.SdkApi.mobileapi;

import android.view.Surface;

import com.icatch.mobilecam.Log.AppLog;
import com.icatchtek.pancam.customer.ICatchIPancamControl;
import com.icatchtek.pancam.customer.ICatchIPancamVideoPlayback;
import com.icatchtek.pancam.customer.ICatchPancamConfig;
import com.icatchtek.pancam.customer.exception.IchGLSurfaceNotSetException;
import com.icatchtek.pancam.customer.gl.ICatchIPancamGL;
import com.icatchtek.pancam.customer.stream.ICatchIStreamProvider;
import com.icatchtek.pancam.customer.surface.ICatchSurfaceContext;
import com.icatchtek.reliant.customer.exception.IchInvalidSessionException;
import com.icatchtek.reliant.customer.exception.IchPauseFailedException;
import com.icatchtek.reliant.customer.exception.IchResumeFailedException;
import com.icatchtek.reliant.customer.exception.IchSeekFailedException;
import com.icatchtek.reliant.customer.exception.IchStreamNotRunningException;
import com.icatchtek.reliant.customer.exception.IchStreamNotSupportException;
import com.icatchtek.reliant.customer.exception.IchTransportException;
import com.icatchtek.reliant.customer.type.ICatchCodec;
import com.icatchtek.reliant.customer.type.ICatchFile;
import com.tinyai.libmediacomponent.engine.streaming.EventListener;
import com.tinyai.libmediacomponent.engine.streaming.IPanoramaControl;
import com.tinyai.libmediacomponent.engine.streaming.IStreamProvider;
import com.tinyai.libmediacomponent.engine.streaming.IStreamingControl;
import com.tinyai.libmediacomponent.engine.streaming.StreamRequest;
import com.tinyai.libmediacomponent.engine.streaming.type.DeviceFile;
import com.tinyai.libmediacomponent.engine.streaming.type.VideoQuality;
import com.tinyai.libmediacomponent.engine.streaming.type.VideoStreamParam;

/**
 * Description TODO
 * Author b.jiang
 * Date 2022/3/9 17:36
 */
public class VideoStreamingControl implements IStreamingControl {

    private static final String TAG = VideoStreamingControl.class.getSimpleName();
    private ICatchIPancamVideoPlayback videoPlayback;
    private ICatchIPancamControl pancamControl;
    private ICatchSurfaceContext iCatchSurfaceContext;
    private boolean isStreaming = false;
    private EventListener eventListener;
    private MediaPlayListenerManager mediaPlayListenerManager;
    private IPanoramaControl panoramaControl;

    public VideoStreamingControl(ICatchIPancamVideoPlayback videoPlayback, ICatchIPancamControl pancamControl){
        this.videoPlayback = videoPlayback;
        this.pancamControl =  pancamControl;
    }


    @Override
    public int start(StreamRequest request) {

        boolean disableAudio = request.isDisableAudio();

        VideoStreamParam videoStreamParam = request.getVideotreamParam();
        if(videoStreamParam == null){
            return -1;
        }
        if(eventListener != null && mediaPlayListenerManager != null){
            mediaPlayListenerManager.addListener();
        }
        boolean isRemote = videoStreamParam.isRemote();
        DeviceFile file = videoStreamParam.getDeviceFile();
        //(int fileHandle,
        // int fileType,
        // String filePath,
        // String fileName,
        // long fileSize,
        // String fileDate,
        // double frameRate,
        // int fileWidth,
        // int fileHeight,
        // int fileProtection,
        // int fileDuration)
        ICatchFile iCatchFile = new ICatchFile(
                file.getFileHandle(),
                file.getFileType(),
                file.getFilePath(),
                file.getFileName(),
                file.getFileSize(),
                file.getFileDate(),
                file.getFrameRate(),
                file.getFileWidth(),
                file.getFileHeight(),
                file.getFileProtection(),
                file.getFileDuration());

        boolean ret = false;
        AppLog.d(TAG,"start play isRemote:"+isRemote + " FileHandle:" + iCatchFile.getFileHandle() + " filePath:" + iCatchFile.getFilePath());
        try {
            ret = videoPlayback.play(iCatchFile,disableAudio,isRemote);
            AppLog.d(TAG,"end play ret:"+ret);
            if(!ret){

                return -1;
            }
            ret = videoPlayback.resume();
            AppLog.d(TAG,"end resume ret:"+ret);
            if (!ret) {
                return -1;
            }
            isStreaming =true;

        }  catch (IchStreamNotSupportException e) {
            AppLog.d(TAG, "Exception : IchStreamNotSupportException");
            e.printStackTrace();
        } catch (Exception e) {
            AppLog.d(TAG, "Exception : " + e.getClass().getSimpleName());
            e.printStackTrace();
        }
        AppLog.d(TAG,"end start ret:"+ret);
        if(ret){
            return 0;
        }else {
            return -1;
        }

    }

    @Override
    public int stop() {
        AppLog.d(TAG, "start stop ");
        boolean retValue = false;
        try {
            retValue = videoPlayback.pause();
        } catch (Exception e) {
            e.printStackTrace();
            AppLog.e(TAG,"pause Exception:" + e.getClass().getSimpleName());
        }
        AppLog.d(TAG,"end pause ret:" + retValue);

        try {
            retValue = videoPlayback.stop();
        } catch (Exception e) {
            e.printStackTrace();
            AppLog.e(TAG,"stop Exception:" + e.getClass().getSimpleName());
        }
        AppLog.d(TAG,"end stop ret:" + retValue);
        if(eventListener != null && mediaPlayListenerManager != null){
            mediaPlayListenerManager.removeListener();
        }
        AppLog.d(TAG, "end stop retValue=" + retValue);
        if(retValue){
            return 0;
        }else {
            return -1;
        }
    }

    @Override
    public boolean isOpenStream() {
        return isStreaming;
    }


    @Override
    public boolean enableRender(Surface surface) {
        boolean ret = false;
        ICatchPancamConfig.getInstance().setOutputCodec(ICatchCodec.ICH_CODEC_JPEG, ICatchCodec.ICH_CODEC_YUV_NV12);
        AppLog.d(TAG,"enableRender");
        iCatchSurfaceContext = new ICatchSurfaceContext(surface);
        try {
            ret = videoPlayback.enableRender(iCatchSurfaceContext);
        } catch (Exception e) {
            AppLog.e(TAG, "Exception : " + e.getClass().getSimpleName());
            e.printStackTrace();
        }
        AppLog.d(TAG,"enableRender end:"+ ret);
        return ret;
    }


    @Override
    public boolean setViewPort(int w, int h) {
        if(iCatchSurfaceContext == null){
            return false;
        }
        try {
            return iCatchSurfaceContext.setViewPort(0,0,w,h);
        } catch (IchGLSurfaceNotSetException e) {
            e.printStackTrace();
        }
        return false;
    }


    @Override
    public IStreamProvider disableRender() {
        AppLog.d(TAG,"start disableRender");
        ICatchIStreamProvider iCatchIStreamProvider= null;
        StreamProvider streamProvider = null;
        try {
            iCatchIStreamProvider = videoPlayback.disableRender();
        } catch (Exception e) {
            AppLog.e(TAG, "Exception : " + e.getClass().getSimpleName());
            e.printStackTrace();
        }
        //默认输出codec为YUN_I420，需手动配置为RGBA
        ICatchPancamConfig.getInstance().setOutputCodec(ICatchCodec.ICH_CODEC_JPEG, ICatchCodec.ICH_CODEC_RGBA_8888);
        if(iCatchIStreamProvider != null){
            streamProvider = new StreamProvider(iCatchIStreamProvider);
        }
        return streamProvider;
    }

    @Override
    public IPanoramaControl enableGLRender() {
        AppLog.d(TAG,"enableGLRender");
        try {
            ICatchIPancamGL iPancamGL = videoPlayback.enableGLRender();
            AppLog.d(TAG,"enableGLRender ret");
            this.panoramaControl = new PanoramaControl(iPancamGL);
        } catch (Exception e) {
            AppLog.e(TAG, "enableGLRender Exception : " + e.getClass().getSimpleName());
            e.printStackTrace();
        }

        return this.panoramaControl;
    }

    @Override
    public IPanoramaControl getPanoramaControl() {
        return this.panoramaControl;
    }

    @Override
    public int play() {
        return 0;
    }

    @Override
    public int pause() {
        boolean ret = false;
        AppLog.d(TAG,"start pause");
        try {
            ret = videoPlayback.pause();
        } catch (Exception e) {
            e.printStackTrace();
            AppLog.d(TAG,"pause Exception:" + e.getClass().getSimpleName());
        }
        AppLog.d(TAG,"pause ret = " + ret);
        if(ret){
            return 0;
        }else {
            return -1;
        }
    }

    @Override
    public int resume() {
        AppLog.d(TAG, "start resume");
        boolean ret = false;
        try {
            ret = videoPlayback.resume();
        } catch (Exception e) {
            e.printStackTrace();
            AppLog.e(TAG,"pause Exception:" + e.getClass().getSimpleName());
        }
        AppLog.d(TAG, "end resume ret=" + ret);
        if(ret){
            return 0;
        }else {
            return -1;
        }
    }

    @Override
    public double getDuration() {
        AppLog.d(TAG, "start getDuration");
        double ret = 0;
        try {
            ret = videoPlayback.getLength();
        } catch (Exception e) {
            e.printStackTrace();
            AppLog.e(TAG,"getDuration Exception:" + e.getClass().getSimpleName());
        }
        AppLog.d(TAG, "end getDuration ret:" +ret);
        return ret;
    }

    @Override
    public int seek(double pts) {
        AppLog.d(TAG, "start seek pts:" + pts);
        boolean ret = false;
        try {
            ret = videoPlayback.seek(pts);
        } catch (Exception e) {
            e.printStackTrace();
            AppLog.e(TAG,"seek Exception:" + e.getClass().getSimpleName());
       }
        AppLog.d(TAG, "end seek ret:" + ret);
        if(ret){
            return 0;
        }else {
            return -1;
        }
    }

    @Override
    public int mute(boolean mute) {
        return 0;
    }

    @Override
    public boolean isMute() {
        return false;
    }

    @Override
    public boolean setVideoQuality(VideoQuality videoQuality) {
        return false;
    }

    @Override
    public void setEventListener(EventListener eventListener) {
        this.eventListener = eventListener;
        if(mediaPlayListenerManager == null){
            mediaPlayListenerManager = new MediaPlayListenerManager(this.eventListener,pancamControl);
        }else {
            mediaPlayListenerManager.setEventListener(this.eventListener);
        }

    }

}
