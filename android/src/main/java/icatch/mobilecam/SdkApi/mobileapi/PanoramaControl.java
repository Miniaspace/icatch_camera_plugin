package com.icatch.mobilecam.SdkApi.mobileapi;

import android.view.Surface;

import com.icatch.mobilecam.Log.AppLog;
import com.icatchtek.pancam.customer.exception.IchGLSurfaceNotSetException;
import com.icatchtek.pancam.customer.gl.ICatchIPancamGL;
import com.icatchtek.pancam.customer.gl.ICatchIPancamGLTransform;
import com.icatchtek.pancam.customer.surface.ICatchSurfaceContext;
import com.icatchtek.pancam.customer.type.ICatchGLPoint;
import com.icatchtek.pancam.customer.type.ICatchGLSurfaceType;
import com.icatchtek.reliant.customer.exception.IchDeprecatedException;
import com.icatchtek.reliant.customer.exception.IchInvalidArgumentException;
import com.tinyai.libmediacomponent.engine.streaming.GLPoint;
import com.tinyai.libmediacomponent.engine.streaming.IPanoramaControl;

/**
 * Description TODO
 * Author b.jiang
 * Date 2022/5/25 14:39
 */
public class PanoramaControl implements IPanoramaControl {

    private static final String TAG = PanoramaControl.class.getSimpleName();
    private ICatchIPancamGL pancamGL;
    private ICatchSurfaceContext iCatchSurfaceContext;
    private int surfaceType = ICatchGLSurfaceType.ICH_GL_SURFACE_TYPE_SPHERE;

    public PanoramaControl(ICatchIPancamGL pancamGL){
        this.pancamGL = pancamGL;
    }

    @Override
    public boolean changePanoramaType(int panoramaType) {
        if(pancamGL == null){
            return false;
        }
        AppLog.d(TAG, "start changePanoramaType panoramaType:" + panoramaType);
        boolean ret = false;
        try {
            ret = pancamGL.changePanoramaType(panoramaType);
        } catch (Exception e) {
            AppLog.d(TAG, "changePanoramaType Exception:" + e.getClass().getSimpleName());
            e.printStackTrace();
        }
        AppLog.d(TAG, "end changePanoramaType ret:" + ret);
        return ret;
    }

    @Override
    public boolean init(int panoramaType) {
        if(pancamGL == null){
            return false;
        }
        boolean ret = false;
        AppLog.d(TAG, "start init ret=" + ret);
        try {
            ret = pancamGL.init(panoramaType);
        } catch (Exception e) {
            AppLog.d(TAG, "init Exception " + e.getClass().getSimpleName());
            e.printStackTrace();
        }
        AppLog.d(TAG, "end init ret=" + ret);
        return ret;
    }

    @Override
    public boolean release() {
        if(pancamGL == null){
            return false;
        }
        AppLog.d(TAG, "start release");
        boolean ret = false;
        try {
            ret = pancamGL.release();
        } catch (Exception e) {
            AppLog.d(TAG, "release Exception " + e.getClass().getSimpleName());
            e.printStackTrace();
        }
        AppLog.d(TAG, "end release ret=" + ret);
        return ret;
    }

    @Override
    public boolean setSurface(int ichSurfaceIdSphere, Surface surface) {
        if(pancamGL == null){
            return false;
        }
        AppLog.d(TAG, "start setSurface");
        boolean ret = false;
        try {
            iCatchSurfaceContext = new ICatchSurfaceContext(surface);
            surfaceType = ichSurfaceIdSphere;
            ret = pancamGL.setSurface(ichSurfaceIdSphere, iCatchSurfaceContext);
        } catch (Exception e) {
            AppLog.d(TAG, "setSurface Exception" + e.getClass().getSimpleName());
            e.printStackTrace();
        }
        AppLog.d(TAG, "end setSurface ret=" + ret);
        return ret;
    }

    @Override
    public boolean removeSurface(int var1) {
        if(pancamGL == null ||iCatchSurfaceContext == null){
            return false;
        }
        AppLog.d(TAG, "start removeSurface");
        boolean ret = false;
        try {
            ret = pancamGL.removeSurface(surfaceType, iCatchSurfaceContext);
        } catch (Exception e) {
            AppLog.d(TAG, "removeSurface Exception" + e.getClass().getSimpleName());
            e.printStackTrace();
        }
        AppLog.d(TAG, "end removeSurface ret=" + ret);
        return ret;
    }

    @Override
    public boolean glTransformLocate(float var1) {
        ICatchIPancamGLTransform glTransform = getPancamGLTransform();
        if (glTransform == null) {
            return false;
        }
        boolean ret = false;
        try {
            ret = glTransform.locate(var1);
        } catch (IchInvalidArgumentException e) {
            e.printStackTrace();
        } catch (IchDeprecatedException e) {
            e.printStackTrace();
        }
        return ret;
    }

    @Override
    public boolean glTransformLocate(int var1, float var2, float var3, float var4, long var5) {
        ICatchIPancamGLTransform glTransform = getPancamGLTransform();
        if (glTransform == null) {
            return false;
        }
        boolean ret = false;
        try {
            ret = glTransform.rotate(var1, var2, var3, var4, var5);
        } catch (IchInvalidArgumentException e) {
            e.printStackTrace();
        } catch (IchDeprecatedException e) {
            e.printStackTrace();
        }
//        AppLog.d(TAG,"end rotate ret=" + ret);
        return ret;
    }

    @Override
    public boolean rotate(GLPoint var1, GLPoint var2) {
        ICatchIPancamGLTransform glTransform = getPancamGLTransform();
        if (glTransform == null) {
            return false;
        }
        boolean ret = false;
        ICatchGLPoint prev = new ICatchGLPoint(var1.getX(), var1.getY());
        ICatchGLPoint curr = new ICatchGLPoint(var2.getX(), var2.getY());
        try {
            ret = glTransform.rotate(prev, curr);
        } catch (IchInvalidArgumentException e) {
            e.printStackTrace();
        } catch (IchDeprecatedException e) {
            e.printStackTrace();
        }
//        AppLog.d(TAG,"end rotate ret=" + ret);
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

    private ICatchIPancamGLTransform getPancamGLTransform() {
        if (pancamGL == null) {
            return null;
        }
        ICatchIPancamGLTransform glTransform = null;
        try {
            glTransform = pancamGL.getPancamGLTransform();
        } catch (IchDeprecatedException e) {
            e.printStackTrace();
        }
        return glTransform;
    }
}
