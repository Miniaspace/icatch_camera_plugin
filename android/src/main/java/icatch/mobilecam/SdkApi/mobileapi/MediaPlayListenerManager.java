package com.icatch.mobilecam.SdkApi.mobileapi;


import com.icatch.mobilecam.Log.AppLog;
import com.icatchtek.pancam.customer.ICatchIPancamControl;
import com.icatchtek.pancam.customer.ICatchIPancamListener;
import com.icatchtek.pancam.customer.type.ICatchGLEvent;
import com.icatchtek.pancam.customer.type.ICatchGLEventID;
import com.icatchtek.reliant.customer.exception.IchInvalidSessionException;
import com.icatchtek.reliant.customer.exception.IchListenerExistsException;
import com.icatchtek.reliant.customer.exception.IchListenerNotExistsException;
import com.tinyai.libmediacomponent.engine.streaming.EventListener;
import com.tinyai.libmediacomponent.engine.streaming.type.PlayState;

/**
 *
 *
 */

public class MediaPlayListenerManager {

    private static final String TAG = MediaPlayListenerManager.class.getSimpleName();
//    private final Handler handler;
    private StreamStateListener streamStateListener;
    private ICatchIPancamControl pancamControl;
    private EventListener eventListener;
    private final static int CACHE_LOADING = 1;
    private final static int CACHE_LOADED = 2;

    public MediaPlayListenerManager(EventListener eventListener, ICatchIPancamControl pancamControl) {
        this.eventListener = eventListener;
        this.pancamControl =  pancamControl;
    }


    public void setEventListener(EventListener eventListener) {
        this.eventListener = eventListener;
    }

    public void addListener() {
        AppLog.d(TAG,"addListener");
        streamStateListener = new StreamStateListener();
        try {
            pancamControl.addEventListener(ICatchGLEventID.ICH_GL_EVENT_VIDEO_STREAM_PLAYING_STATUS, streamStateListener );
            pancamControl.addEventListener(ICatchGLEventID.ICH_GL_EVENT_VIDEO_STREAM_PLAYING_ENDED, streamStateListener );
            pancamControl.addEventListener(ICatchGLEventID.ICH_GL_EVENT_VIDEO_PLAYBACK_CACHING_CHANGED, streamStateListener );
            pancamControl.addEventListener(ICatchGLEventID.ICH_GL_EVENT_VIDEO_PLAYBACK_CACHING_PROGRESS, streamStateListener );
            AppLog.d(TAG,"addListener end");
        } catch (IchInvalidSessionException e) {
            e.printStackTrace();
            AppLog.d(TAG,"addListener IchInvalidSessionException");
        } catch (IchListenerExistsException e) {
            e.printStackTrace();
            AppLog.d(TAG,"addListener IchListenerExistsException");
        }
    }

    public void removeListener() {
        AppLog.d(TAG,"removeListener");
        try {
            if (streamStateListener != null) {
                pancamControl.removeEventListener(ICatchGLEventID.ICH_GL_EVENT_VIDEO_STREAM_PLAYING_STATUS, streamStateListener );
                pancamControl.removeEventListener(ICatchGLEventID.ICH_GL_EVENT_VIDEO_STREAM_PLAYING_ENDED, streamStateListener );
                pancamControl.removeEventListener(ICatchGLEventID.ICH_GL_EVENT_VIDEO_PLAYBACK_CACHING_CHANGED, streamStateListener );
                pancamControl.removeEventListener(ICatchGLEventID.ICH_GL_EVENT_VIDEO_PLAYBACK_CACHING_PROGRESS, streamStateListener );
                streamStateListener = null;
                AppLog.d(TAG,"removeListener end");
            }

        } catch (IchListenerNotExistsException e) {

            e.printStackTrace();
            AppLog.d(TAG,"addListener IchListenerNotExistsException");
        } catch (IchInvalidSessionException e) {
            e.printStackTrace();
            AppLog.d(TAG,"addListener IchInvalidSessionException");
        }


    }

    public class StreamStateListener implements ICatchIPancamListener {

        @Override
        public void eventNotify(ICatchGLEvent iCatchGLEvent) {
            int eventId = iCatchGLEvent.getEventID();
            switch (eventId){
                case ICatchGLEventID.ICH_GL_EVENT_VIDEO_STREAM_PLAYING_STATUS:
//                    AppLog.d(TAG, "receive ICH_GL_EVENT_VIDEO_STREAM_PLAYING_STATUS.......value=" + iCatchGLEvent.getDoubleValue1());
                    if(eventListener!= null){
                        eventListener.onPlayProgressChanged(iCatchGLEvent.getDoubleValue1());
                    }
//                    handler.obtainMessage(StreamMessage.EVENT_VIDEO_PLAY_PTS, iCatchGLEvent.getDoubleValue1()).sendToTarget();
                    break;
                case ICatchGLEventID.ICH_GL_EVENT_VIDEO_STREAM_PLAYING_ENDED:
                    AppLog.i(TAG, "--------------receive ICH_GL_EVENT_VIDEO_STREAM_PLAYING_ENDED");

                    if(eventListener!= null) {
                        eventListener.onPlaybackStateChanged(PlayState.END);
                    }
//                    handler.obtainMessage( StreamMessage.EVENT_VIDEO_PLAY_COMPLETED, 0, 0 ).sendToTarget();
                    break;
                case ICatchGLEventID.ICH_GL_EVENT_VIDEO_PLAYBACK_CACHING_CHANGED:
                    AppLog.d(TAG, "receive ICH_GL_EVENT_VIDEO_PLAYBACK_CACHING_CHANGED.......value=" + iCatchGLEvent.getLongValue1());
                    if(eventListener!= null) {
                        eventListener.onLoadingChanged((int) iCatchGLEvent.getLongValue1());
                    }
//                    handler.obtainMessage(StreamMessage.EVENT_CACHE_STATE_CHANGED, (int) iCatchGLEvent.getLongValue1(), 0)
//                            .sendToTarget();
                    break;
                case ICatchGLEventID.ICH_GL_EVENT_VIDEO_PLAYBACK_CACHING_PROGRESS:
                    int temp = new Double(iCatchGLEvent.getDoubleValue1()).intValue();
//                    AppLog.d(TAG, "receive ICH_GL_EVENT_VIDEO_PLAYBACK_CACHING_PROGRESS.......value=" + iCatchGLEvent.getLongValue1() + " value2=" + temp);
                    if(eventListener!= null) {
                        eventListener.onBufferingProgressChanged((int) iCatchGLEvent.getLongValue1(),temp);
                    }
//                    handler.obtainMessage(StreamMessage.EVENT_CACHE_PROGRESS_NOTIFY, (int) iCatchGLEvent.getLongValue1(),temp )
//                            .sendToTarget();
                    break;
            }

        }
    }

}
