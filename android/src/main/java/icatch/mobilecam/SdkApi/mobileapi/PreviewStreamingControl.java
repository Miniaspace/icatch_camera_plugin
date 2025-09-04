package com.icatch.mobilecam.SdkApi.mobileapi;

import android.view.Surface;

import com.icatch.mobilecam.Log.AppLog;
import com.icatchtek.control.customer.ICatchCameraControl;
import com.icatchtek.pancam.customer.ICatchIPancamPreview;
import com.icatchtek.pancam.customer.ICatchPancamConfig;
import com.icatchtek.pancam.customer.exception.IchGLSurfaceNotSetException;
import com.icatchtek.pancam.customer.gl.ICatchIPancamGL;
import com.icatchtek.pancam.customer.stream.ICatchIStreamProvider;
import com.icatchtek.pancam.customer.surface.ICatchSurfaceContext;
import com.icatchtek.reliant.customer.exception.IchInvalidArgumentException;
import com.icatchtek.reliant.customer.exception.IchInvalidSessionException;
import com.icatchtek.reliant.customer.exception.IchPermissionDeniedException;
import com.icatchtek.reliant.customer.exception.IchStreamAlreadyStartedException;
import com.icatchtek.reliant.customer.exception.IchStreamNotRunningException;
import com.icatchtek.reliant.customer.exception.IchStreamNotSupportException;
import com.icatchtek.reliant.customer.exception.IchTransportException;
import com.icatchtek.reliant.customer.type.ICatchCodec;
import com.icatchtek.reliant.customer.type.ICatchCustomerStreamParam;
import com.icatchtek.reliant.customer.type.ICatchH264StreamParam;
import com.icatchtek.reliant.customer.type.ICatchJPEGStreamParam;
import com.icatchtek.reliant.customer.type.ICatchStreamParam;
import com.tinyai.libmediacomponent.engine.streaming.EventListener;
import com.tinyai.libmediacomponent.engine.streaming.IPanoramaControl;
import com.tinyai.libmediacomponent.engine.streaming.IStreamProvider;
import com.tinyai.libmediacomponent.engine.streaming.IStreamingControl;
import com.tinyai.libmediacomponent.engine.streaming.StreamRequest;
import com.tinyai.libmediacomponent.engine.streaming.type.PreviewStreamParam;
import com.tinyai.libmediacomponent.engine.streaming.type.VideoQuality;

public class PreviewStreamingControl implements IStreamingControl {

    private static final String TAG = PreviewStreamingControl.class.getSimpleName();
    private  ICatchIPancamPreview pancamPreview;
    private ICatchCameraControl cameraControl;
    private ICatchSurfaceContext iCatchSurfaceContext;
    private IPanoramaControl panoramaControl;

    public PreviewStreamingControl(ICatchIPancamPreview pancamPreview, ICatchCameraControl cameraControl){
        this.pancamPreview = pancamPreview;
        this.cameraControl = cameraControl;
    }


    @Override
    public int start(StreamRequest request) {

        boolean ret = false;
//        try {
//            ret = cameraControl.changePreviewMode(ICatchCamPreviewMode.ICH_CAM_VIDEO_PREVIEW_MODE);
//        } catch (IchCameraModeException e) {
//            e.printStackTrace();
//        }
//        AppLog.i(TAG, "end changePreviewMode ret = " + ret);
        boolean enableAudio = !request.isDisableAudio();
        AppLog.d(TAG,"start enableAudio:"+enableAudio);
        int cacheTime = 400;
        ICatchPancamConfig.getInstance().setPreviewCacheParam(cacheTime,200);
        PreviewStreamParam streamParam = request.getPreviewStreamParam();
        if(streamParam == null){
            return -1;
        }
        int codec = streamParam.getCodec();
        ICatchStreamParam iCatchStreamParam = null;
        if(codec == 41){
            //int width, int height, int frameRate, int bitRate
            AppLog.d(TAG, "creat H264StreamParam FrameRate=" + streamParam.getFrameRate() + " BitRate:" + streamParam.getBitRate());
            iCatchStreamParam = new ICatchH264StreamParam(streamParam.getWidth(),streamParam.getHeight(),streamParam.getFrameRate(),streamParam.getBitRate());
        }else if(codec == 64){
            iCatchStreamParam = new ICatchJPEGStreamParam(streamParam.getWidth(),streamParam.getHeight(),streamParam.getFrameRate(),streamParam.getBitRate());
        }

        if(iCatchStreamParam == null){
            return -1;
        }
        AppLog.d(TAG,"start stream Param:["+iCatchStreamParam.getCmdLineParam() +"]");

        try {
            ret = pancamPreview.start(iCatchStreamParam,enableAudio);
        }  catch (IchStreamNotSupportException e) {
            AppLog.d(TAG, "Exception : IchStreamNotSupportException");
            e.printStackTrace();
        } catch (Exception e) {
            AppLog.d(TAG, "Exception : " + e.getClass().getSimpleName());
            e.printStackTrace();
        }

        if(!ret){
            AppLog.d(TAG,"first start stream failed,to start CustomerStream");
            iCatchStreamParam = new ICatchCustomerStreamParam(554 ,"MJPG?W=640&H=360&Q=50&BR=5000000");
            try {
                ret = pancamPreview.start(iCatchStreamParam,enableAudio);
            } catch (IchStreamNotSupportException e) {
                AppLog.d(TAG, "Exception : IchStreamNotSupportException");
                e.printStackTrace();
            } catch (Exception e) {
                AppLog.d(TAG, "Exception : " + e.getClass().getSimpleName());
                e.printStackTrace();
            }
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
            retValue = pancamPreview.stop();
        } catch (Exception e) {
            e.printStackTrace();
            AppLog.e(TAG, "stop Exception=" + e.getClass().getSimpleName());
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
        return false;
    }

    @Override
    public boolean enableRender(Surface surface) {
        boolean ret = false;
//        ICatchPancamConfig.getInstance().setOutputCodec(ICatchCodec.ICH_CODEC_JPEG, ICatchCodec.ICH_CODEC_YUV_I420);
        ICatchPancamConfig.getInstance().setOutputCodec(ICatchCodec.ICH_CODEC_JPEG, ICatchCodec.ICH_CODEC_YUV_NV12);
        iCatchSurfaceContext = new ICatchSurfaceContext(surface);
        AppLog.d(TAG,"enableRender");
        try {
            ret = pancamPreview.enableRender(iCatchSurfaceContext);
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
        ICatchIStreamProvider  iCatchIStreamProvider= null;
        StreamProvider streamProvider = null;
        try {
            iCatchIStreamProvider = pancamPreview.disableRender();
        } catch (Exception e) {
            AppLog.e(TAG, "Exception : " + e.getClass().getSimpleName());
            e.printStackTrace();
        }
        //默认输出codec为YUN_I420，需手动配置为RGBA
        ICatchPancamConfig.getInstance().setOutputCodec(ICatchCodec.ICH_CODEC_JPEG, ICatchCodec.ICH_CODEC_RGBA_8888);
        if(iCatchIStreamProvider != null){
            streamProvider = new StreamProvider(iCatchIStreamProvider);
        }
        AppLog.d(TAG,"end disableRender");
        return streamProvider;
    }

    @Override
    public IPanoramaControl enableGLRender() {
        AppLog.d(TAG,"start enableGLRender");
        ICatchPancamConfig.getInstance().setOutputCodec(ICatchCodec.ICH_CODEC_JPEG, ICatchCodec.ICH_CODEC_YUV_NV12);
        try {
            ICatchIPancamGL iPancamGL = pancamPreview.enableGLRender();
            this.panoramaControl = new PanoramaControl(iPancamGL);
        } catch (IchInvalidSessionException e) {
            e.printStackTrace();
        } catch (IchPermissionDeniedException e) {
            e.printStackTrace();
        }

        AppLog.d(TAG,"end enableGLRender");
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
        return 0;
    }

    @Override
    public int resume() {
        return 0;
    }

    @Override
    public double getDuration() {
        return 0;
    }

    @Override
    public int seek(double pts) {
        return 0;
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

    }

}
