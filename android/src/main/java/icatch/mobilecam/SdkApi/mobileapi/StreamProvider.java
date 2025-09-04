package com.icatch.mobilecam.SdkApi.mobileapi;

import com.icatch.mobilecam.Log.AppLog;
import com.icatchtek.pancam.customer.stream.ICatchIStreamProvider;
import com.icatchtek.reliant.customer.exception.IchDeprecatedException;
import com.icatchtek.reliant.customer.exception.IchInvalidSessionException;
import com.icatchtek.reliant.customer.exception.IchStreamNotRunningException;
import com.icatchtek.reliant.customer.exception.IchTransportException;
import com.icatchtek.reliant.customer.exception.IchTryAgainException;
import com.icatchtek.reliant.customer.type.ICatchAudioFormat;
import com.icatchtek.reliant.customer.type.ICatchFrameBuffer;
import com.icatchtek.reliant.customer.type.ICatchVideoFormat;
import com.tinyai.libmediacomponent.engine.streaming.IStreamProvider;
import com.tinyai.libmediacomponent.engine.streaming.exception.TryAgainException;
import com.tinyai.libmediacomponent.engine.streaming.type.FrameBuffer;
import com.tinyai.libmediacomponent.engine.streaming.type.MAudioFormat;
import com.tinyai.libmediacomponent.engine.streaming.type.MVideoFormat;

public class StreamProvider implements IStreamProvider {

    private final String TAG = "StreamProvider";
    ICatchIStreamProvider streamProvider;
    public StreamProvider(ICatchIStreamProvider provider){
        this.streamProvider = provider;
    }

    @Override
    public boolean containsVideoStream() {
        boolean ret = false;
        AppLog.d(TAG,"begin containsVideoStream");
        try {
            ret = streamProvider.containsVideoStream();
        } catch (Exception e) {
            AppLog.d(TAG,"containsVideoStream Exception:" + e.getClass().getSimpleName());
            e.printStackTrace();
        }
        AppLog.d(TAG,"end containsVideoStream ");
        return ret;
    }

    @Override
    public boolean containsAudioStream() {
        AppLog.d(TAG,"begin containsAudioStream");
        boolean ret = false;
        try {
            ret = streamProvider.containsAudioStream();
        } catch (Exception e) {
            AppLog.d(TAG,"containsAudioStream Exception:" + e.getClass().getSimpleName());
            e.printStackTrace();
        }
        AppLog.d(TAG,"end containsAudioStream");
        return ret;
    }

    @Override
    public MVideoFormat getVideoFormat() {
        AppLog.d(TAG,"begin getVideoFormat");
        ICatchVideoFormat format= null;
        try {
            format = streamProvider.getVideoFormat();
        } catch (Exception e) {
            AppLog.d(TAG,"getVideoFormat Exception:" + e.getClass().getSimpleName());
            e.printStackTrace();
        }
        AppLog.d(TAG,"end getVideoFormat format:" + format);
        if(format != null){

            MVideoFormat mVideoFormat = new MVideoFormat(format.getCodec(),format.getVideoW(),format.getVideoH(),format.getFrameRate());
//            private int bitrate;
//            private String mineType;
//            private int durationUs;
//            private int maxInputSize;
//            private int fps;
//            private int gop;
//            private byte[] csd_0;
//            private byte[] csd_1;
//            private int csd_0_size;
//            private int csd_1_size;
//            private int streamType;
            mVideoFormat.setBitrate(format.getBitrate());
            mVideoFormat.setMineType(format.getMineType());
            mVideoFormat.setDurationUs(format.getDurationUs());
            mVideoFormat.setMaxInputSize(format.getMaxInputSize());
            if(format.getCsd_0() != null){
                mVideoFormat.setCsd_0(format.getCsd_0(),format.getCsd_0_size());
            }
            if(format.getCsd_1()!= null){
                mVideoFormat.setCsd_1(format.getCsd_1(),format.getCsd_1_size());
            }

            return mVideoFormat;
        }
        return null;
    }

    @Override
    public MAudioFormat getAudioFormat() {
        ICatchAudioFormat format = null;
        AppLog.d(TAG,"begin getAudioFormat");
        MAudioFormat mAudioFormat = null;
        try {
            format = streamProvider.getAudioFormat();
        } catch (Exception e) {
            AppLog.d(TAG,"getAudioFormat Exception:" + e.getClass().getSimpleName());
            e.printStackTrace();
        }
        AppLog.d(TAG,"end getAudioFormat format:" + format);
        if(format != null){
            mAudioFormat = new MAudioFormat(format.getCodec(),format.getFrequency(),format.getNChannels(),format.getSampleBits());
            return mAudioFormat;
        }
        return null;
    }

    @Override
    public boolean getNextVideoFrame(FrameBuffer var1) throws TryAgainException {
        ICatchFrameBuffer iCatchFrameBuffer = new ICatchFrameBuffer(var1.getBuffer());
        try {
            boolean ret = streamProvider.getNextVideoFrame(iCatchFrameBuffer);
            if(ret){
//                var1.setBuffer(iCatchFrameBuffer.getBuffer());
                var1.setFrameSize(iCatchFrameBuffer.getFrameSize());
                var1.setPresentationTime(iCatchFrameBuffer.getPresentationTime());
//                AppLog.d(TAG,"getNextVideoFrame FrameSize:"+iCatchFrameBuffer.getFrameSize() + " PresentationTime:" + iCatchFrameBuffer.getPresentationTime());
            }
        } catch (IchTryAgainException e) {
            AppLog.d(TAG,"getNextVideoFrame IchTryAgainException");
            e.printStackTrace();
            throw new TryAgainException();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

//    @Override
//    public boolean getNextVideoFrame(ICatchFrameBuffer buffer) throws TryAgainException {
//
//        if (streamProvider == null) {
//            AppLog.e(TAG, "iCatchIStreamProvider is null");
//            return false;
//        }
//        boolean ret = false;
//        try {
//            ret = streamProvider.getNextVideoFrame(buffer);
//        }  catch (IchTryAgainException e) {
//            e.printStackTrace();
//            throw new TryAgainException();
//        }catch (Exception e) {
//            e.printStackTrace();
//        }
//        return ret;
//    }

    @Override
    public boolean getNextAudioFrame(FrameBuffer var1) throws TryAgainException {
        ICatchFrameBuffer iCatchFrameBuffer = new ICatchFrameBuffer(var1.getBuffer());
        try {
            boolean ret = streamProvider.getNextAudioFrame(iCatchFrameBuffer);
            if(ret){
//                var1.setBuffer(iCatchFrameBuffer.getBuffer());
                var1.setFrameSize(iCatchFrameBuffer.getFrameSize());
                var1.setPresentationTime(iCatchFrameBuffer.getPresentationTime());
            }
        } catch (IchTryAgainException e) {
            AppLog.d(TAG,"getNextAudioFrame IchTryAgainException");
            e.printStackTrace();
            throw new TryAgainException();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
}
