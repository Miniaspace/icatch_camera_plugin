package com.icatch.mobilecam.data.entity;

import com.icatch.mobilecam.MyCamera.CameraAddType;
import com.icatch.mobilecam.MyCamera.CameraType;

public class CameraSlot {
    // public boolean isRegister;
    public boolean isOccupied;
    public boolean isReady;
    public int slotPosition;
    public String cameraName;
    public int cameraType;
    public byte[] cameraPhoto;
    public int addType;
    public String wifiPassword;

    public CameraSlot(int slotPosition, boolean isOccupied, String cameraName, byte[] cameraPhoto) {
        this.slotPosition = slotPosition;
        this.isOccupied = isOccupied;
        this.cameraName = cameraName;
        this.cameraPhoto = cameraPhoto;
        this.isReady = false;
        this.cameraType = CameraType.UNDEFIND_CAMERA;
        this.addType = CameraAddType.DEFAULT;
        this.wifiPassword = "";
    }

    public CameraSlot(int slotPosition, boolean isOccupied, String cameraName, int cameraType, byte[] cameraPhoto, boolean isReady, int addType, String wifiPassword) {
        this.slotPosition = slotPosition;
        this.isOccupied = isOccupied;
        this.cameraName = cameraName;
        this.cameraPhoto = cameraPhoto;
        this.isReady = isReady;
        this.cameraType = cameraType;
        this.addType = addType;
        this.wifiPassword = wifiPassword;
    }
}
