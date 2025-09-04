package com.icatch.mobilecam.SdkApi.mobileapi;

import android.view.Surface;

import com.tinyai.libmediacomponent.engine.streaming.ISurfaceContext;

public class SurfaceContext implements ISurfaceContext {
    Surface surface;

    public SurfaceContext(Surface surface){
        this.surface = surface;
    }

    @Override
    public Surface getSurface(){
        return this.surface;
    }

    @Override
    public int getSurfaceID() {
        return 0;
    }

    @Override
    public boolean setViewPort(int var1, int var2, int var3, int var4) {
        return false;
    }
}
