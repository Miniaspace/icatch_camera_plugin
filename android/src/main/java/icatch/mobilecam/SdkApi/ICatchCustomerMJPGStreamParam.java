package com.icatch.mobilecam.SdkApi;

import com.icatchtek.reliant.customer.type.ICatchJPEGStreamParam;
import com.icatchtek.reliant.customer.type.ICatchStreamParam;

import org.json.JSONException;
import org.json.JSONObject;

public class ICatchCustomerMJPGStreamParam implements ICatchStreamParam {
    private int codec = 64;
    private int qSize = 50;
    private int width;
    private int height;
    private int bitRate;
    private int frameRate;

    public ICatchCustomerMJPGStreamParam() {
        this.width = 1920;
        this.height = 960;
        this.bitRate = 5000000;
        this.frameRate = 30;
    }

    public ICatchCustomerMJPGStreamParam(int width, int height) {
        this.width = width;
        this.height = height;
        this.bitRate = 5000000;
        this.frameRate = 30;
    }

    public ICatchCustomerMJPGStreamParam(int width, int height, int frameRate) {
        this.width = width;
        this.height = height;
        this.bitRate = 5000000;
        this.frameRate = frameRate;
    }

    public ICatchCustomerMJPGStreamParam(int width, int height, int frameRate, int bitRate) {
        this.width = width;
        this.height = height;
        this.bitRate = bitRate;
        this.frameRate = frameRate;
    }

    public int getTransportType() {
        return -1;
    }

    public int getCodec() {
        return 64;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public int getBitRate() {
        return this.bitRate;
    }

    public int getFrameRate() {
        return this.frameRate;
    }

    public String getCmdLineParam() {
        StringBuilder builder = new StringBuilder();
        builder.append("MJPG?");
        builder.append("W=").append(this.width).append("&");
        builder.append("H=").append(this.height).append("&");
        builder.append("Q=").append(this.qSize).append("&");
        builder.append("BR=").append(this.bitRate);
        return builder.toString();
    }

    public String toString() {
        JSONObject attributes = new JSONObject();

        try {
            attributes.put("codecName", "JPEG");
            attributes.put("codec", this.codec);
            attributes.put("qSize", this.qSize);
            attributes.put("videoW", this.width);
            attributes.put("videoH", this.height);
            attributes.put("bitRate", this.bitRate);
            attributes.put("frameRate", this.frameRate);
        } catch (JSONException var3) {
            var3.printStackTrace();
            return null;
        }

        return attributes.toString();
    }

}