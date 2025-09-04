package com.icatch.mobilecam.Function.Setting;

import android.content.Context;

import androidx.appcompat.widget.SwitchCompat;

import com.icatch.mobilecam.Application.PanoramaApp;
import com.icatch.mobilecam.Function.BaseProrertys;
import com.icatch.mobilecam.data.AppInfo.AppInfo;
import com.icatch.mobilecam.data.type.TimeLapseMode;
import com.icatch.mobilecam.data.entity.SettingMenu;
import com.icatch.mobilecam.MyCamera.MyCamera;
import com.icatch.mobilecam.data.PropertyId.PropertyId;
import com.icatch.mobilecam.R;
import com.icatch.mobilecam.SdkApi.CameraFixedInfo;
import com.icatch.mobilecam.SdkApi.CameraProperties;
import com.icatch.mobilecam.SdkApi.CameraState;
import com.icatch.mobilecam.utils.StorageUtil;
import com.icatchtek.control.customer.type.ICatchCamMode;
import com.icatchtek.control.customer.type.ICatchCamProperty;
import com.tinyai.libmediacomponent.components.setting.SettingGroup;
import com.tinyai.libmediacomponent.components.setting.SettingItem;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class UIDisplaySource {
    public static final int CAPTURE_SETTING_MENU = 1;
    public static final int VIDEO_SETTING_MENU = 2;
    public static final int TIMELAPSE_SETTING_MENU = 3;
    private static UIDisplaySource uiDisplayResource;
    private CameraState cameraState;
    private CameraProperties cameraProperties;
    private BaseProrertys baseProrertys;
    private CameraFixedInfo cameraFixedInfo;
    private MyCamera curCamera;
    private List<SettingGroup> settingMenuList;

    public static UIDisplaySource getinstance() {
        if (uiDisplayResource == null) {
            uiDisplayResource = new UIDisplaySource();
        }
        return uiDisplayResource;
    }

    public synchronized List<SettingGroup> getList(int type, MyCamera currCamera) {
        this.curCamera = currCamera;
        this.cameraState = currCamera.getCameraState();
        this.cameraProperties = currCamera.getCameraProperties();
        this.baseProrertys = currCamera.getBaseProrertys();
        this.cameraFixedInfo = currCamera.getCameraFixedInfo();
        switch (type) {
            case CAPTURE_SETTING_MENU:
                return getForCaptureMode();
            case VIDEO_SETTING_MENU:
                return getForVideoMode();
            case TIMELAPSE_SETTING_MENU:
                return getForTimelapseMode();
            default:
                return null;
        }
    }



    private SettingGroup getInfoGroup(){
        SettingGroup categoryTwo = new SettingGroup();

        if (cameraState.isSupportImageAutoDownload()) {
            categoryTwo.addItem(new SettingItem(-1,R.string.setting_auto_download, false,false,AppInfo.autoDownloadAllow));
            categoryTwo.addItem(new SettingItem(-1,R.string.setting_auto_download_size_limit, "",false,false,true));
        }
        categoryTwo.addItem(new SettingItem(-1,R.string.setting_format, "",false,false,true));
        categoryTwo.addItem(new SettingItem(-1,R.string.setting_storage_location, StorageUtil.getCurStorageLocation(PanoramaApp.getContext()),false,false,true));
        if (cameraProperties.hasFuction(PropertyId.STA_MODE_SSID)){
            categoryTwo.addItem(new SettingItem(-1,R.string.setting_enable_wifi_hotspot, "",false,false,true));
        }
        if (cameraProperties.hasFuction(PropertyId.UP_SIDE)) {
            categoryTwo.addItem(new SettingItem(-1,R.string.upside, baseProrertys.getUpside().getCurrentUiStringInSetting(),false,false,true));
        }
        if (cameraProperties.hasFuction(PropertyId.CAMERA_ESSID)) {//camera password and wifi
            categoryTwo.addItem(new SettingItem(-1,R.string.camera_wifi_configuration, "",false,false,true));
        }
        if (cameraProperties.hasFuction(PropertyId.POWER_ON_AUTO_RECORD)) {
            int curValue = cameraProperties.getCurrentPropertyValue(PropertyId.POWER_ON_AUTO_RECORD);
            boolean isCheched = curValue == 0 ? false : true;
            categoryTwo.addItem(new SettingItem(-1,R.string.setting_title_power_on_auto_record,false,false,isCheched));
        }
        if (cameraProperties.hasFuction(PropertyId.AUTO_POWER_OFF)) {
            categoryTwo.addItem(new SettingItem(-1,R.string.setting_title_auto_power_off, baseProrertys.getAutoPowerOff().getCurrentUiStringInPreview(),false,false,true));
        }
        if (cameraProperties.hasFuction(PropertyId.EXPOSURE_COMPENSATION)) {
            categoryTwo.addItem(new SettingItem(-1,R.string.setting_title_exposure_compensation, baseProrertys.getExposureCompensation()
                    .getCurrentUiStringInPreview(),false,false,true));
        }

        categoryTwo.addItem(new SettingItem(-1,R.string.setting_update_fw,"",false,false,true));
        categoryTwo.addItem(new SettingItem(-1,R.string.setting_app_version, AppInfo.APP_VERSION,false,false,false));
        categoryTwo.addItem(new SettingItem(-1,R.string.setting_product_name, cameraFixedInfo.getCameraName(),false,false,false));
        if (cameraProperties.hasFuction(ICatchCamProperty.ICH_CAM_CAP_FW_VERSION)) {
            categoryTwo.addItem(new SettingItem(-1,R.string.setting_firmware_version, cameraFixedInfo.getCameraVersion(),false,false,false));
        }

        return categoryTwo;
    }

    public List<SettingGroup> getForCaptureMode() {
        if (settingMenuList == null) {
            settingMenuList = new ArrayList<>();
        } else {
            settingMenuList.clear();
        }
        SettingGroup categoryOne = new SettingGroup();

        if (cameraProperties.hasFuction(ICatchCamProperty.ICH_CAM_CAP_IMAGE_SIZE) == true) {
            categoryOne.addItem(new SettingItem(-1,R.string.setting_image_size, baseProrertys.getImageSize().getCurrentUiStringInSetting(),false,false,true));
        }
        if (cameraProperties.hasFuction(ICatchCamProperty.ICH_CAM_CAP_CAPTURE_DELAY) == true) {
            categoryOne.addItem(new SettingItem(-1,R.string.setting_capture_delay, baseProrertys.getCaptureDelay().getCurrentUiStringInPreview(),false,false,true));
        }
        if (cameraProperties.hasFuction(ICatchCamProperty.ICH_CAM_CAP_BURST_NUMBER) == true) {
            categoryOne.addItem(new SettingItem(-1,R.string.title_burst, baseProrertys.getBurst().getCurrentUiStringInSetting(),false,false,true));
        }
        if (cameraProperties.hasFuction(ICatchCamProperty.ICH_CAM_CAP_WHITE_BALANCE)) {
            categoryOne.addItem(new SettingItem(-1,R.string.title_awb, baseProrertys.getWhiteBalance().getCurrentUiStringInSetting(),false,false,true));
        }
        if (cameraProperties.hasFuction(ICatchCamProperty.ICH_CAM_CAP_LIGHT_FREQUENCY)) {
            categoryOne.addItem(new SettingItem(-1,R.string.setting_power_supply, baseProrertys.getElectricityFrequency().getCurrentUiStringInSetting(),false,false,true));
        }
        if (cameraProperties.hasFuction(ICatchCamProperty.ICH_CAM_CAP_DATE_STAMP) == true) {
            categoryOne.addItem(new SettingItem(-1,R.string.setting_datestamp, baseProrertys.getDateStamp().getCurrentUiStringInSetting(),false,false,true));
        }

//        categoryOne.addItem(new SettingItem(-1,R.string.setting_audio_switch, ""));
//        categoryOne.addItem( new SettingItem(-1, R.string.setting_live_address, AppInfo.liveAddress ) );

        settingMenuList.add(categoryOne);
        settingMenuList.add(getInfoGroup());
        return settingMenuList;
    }

    private List<SettingGroup> getForVideoMode() {
        if (settingMenuList == null) {
            settingMenuList = new ArrayList<>();
        } else {
            settingMenuList.clear();
        }
        SettingGroup categoryOne = new SettingGroup();
        if (cameraProperties.hasFuction(ICatchCamProperty.ICH_CAM_CAP_VIDEO_SIZE) == true) {
            categoryOne.addItem(new SettingItem(-1,R.string.setting_video_size, baseProrertys.getVideoSize().getCurrentUiStringInSetting(),false,false,true));
        }
        if (cameraProperties.hasFuction(ICatchCamProperty.ICH_CAM_CAP_WHITE_BALANCE)) {
            categoryOne.addItem(new SettingItem(-1,R.string.title_awb, baseProrertys.getWhiteBalance().getCurrentUiStringInSetting(),false,false,true));
        }
        if (cameraProperties.hasFuction(ICatchCamProperty.ICH_CAM_CAP_LIGHT_FREQUENCY)) {
            categoryOne.addItem(new SettingItem(-1,R.string.setting_power_supply, baseProrertys.getElectricityFrequency().getCurrentUiStringInSetting(),false,false,true));
        }
        if (cameraProperties.hasFuction(ICatchCamProperty.ICH_CAM_CAP_DATE_STAMP) == true) {
            categoryOne.addItem(new SettingItem(-1,R.string.setting_datestamp, baseProrertys.getDateStamp().getCurrentUiStringInSetting(),false,false,true));
        }


        //categoryOne.addItem(new SettingItem(-1,R.string.setting_enable_wifi_hotspot, ""));
        if (cameraProperties.hasFuction(PropertyId.SLOW_MOTION)) {
            categoryOne.addItem(new SettingItem(-1,R.string.slowmotion, baseProrertys.getSlowMotion().getCurrentUiStringInSetting(),false,false,true));
        }

        if (cameraProperties.hasFuction(PropertyId.SCREEN_SAVER)) {
            categoryOne.addItem(new SettingItem(-1,R.string.setting_title_screen_saver, baseProrertys.getScreenSaver().getCurrentUiStringInPreview(),false,false,true));
        }

        if (cameraProperties.hasFuction(PropertyId.IMAGE_STABILIZATION)) {
            int curValue = cameraProperties.getCurrentPropertyValue(PropertyId.IMAGE_STABILIZATION);
            boolean isCheched = curValue == 0 ? false : true;
            categoryOne.addItem(new SettingItem(-1,R.string.setting_title_image_stabilization, false,false,isCheched));
        }
        if (cameraProperties.hasFuction(PropertyId.VIDEO_FILE_LENGTH)) {
            categoryOne.addItem(new SettingItem(-1,R.string.setting_title_video_file_length, baseProrertys.getVideoFileLength().getCurrentUiStringInPreview(),false,false,true));
        }
        if (cameraProperties.hasFuction(PropertyId.FAST_MOTION_MOVIE)) {
            categoryOne.addItem(new SettingItem(-1,R.string.setting_title_fast_motion_movie, baseProrertys.getFastMotionMovie().getCurrentUiStringInPreview(),false,false,true));
        }
        if (cameraProperties.hasFuction(PropertyId.WIND_NOISE_REDUCTION)) {
            int curValue = cameraProperties.getCurrentPropertyValue(PropertyId.WIND_NOISE_REDUCTION);
            boolean isCheched = curValue == 0 ? false : true;
            categoryOne.addItem(new SettingItem(-1,R.string.setting_title_wind_noise_reduction, false,false,isCheched));
        }

        settingMenuList.add(categoryOne);
        settingMenuList.add(getInfoGroup());
        return settingMenuList;
    }

    public List<SettingGroup> getForTimelapseMode() {
        if (settingMenuList == null) {
            settingMenuList = new ArrayList<>();
        } else {
            settingMenuList.clear();
        }
        SettingGroup categoryOne = new SettingGroup();
        if (curCamera.timeLapsePreviewMode == TimeLapseMode.TIME_LAPSE_MODE_STILL) {
            if (cameraProperties.hasFuction(ICatchCamProperty.ICH_CAM_CAP_IMAGE_SIZE) == true) {
                categoryOne.addItem(new SettingItem(-1,R.string.setting_image_size, baseProrertys.getImageSize().getCurrentUiStringInSetting(),false,false,true));
            }
        } else if (curCamera.timeLapsePreviewMode == TimeLapseMode.TIME_LAPSE_MODE_VIDEO) {
            if (cameraProperties.hasFuction(ICatchCamProperty.ICH_CAM_CAP_VIDEO_SIZE) == true) {
                categoryOne.addItem(new SettingItem(-1,R.string.setting_video_size, baseProrertys.getVideoSize().getCurrentUiStringInSetting(),false,false,true));
            }
        }
        if (cameraProperties.hasFuction(ICatchCamProperty.ICH_CAM_CAP_WHITE_BALANCE)) {
            categoryOne.addItem(new SettingItem(-1,R.string.title_awb, baseProrertys.getWhiteBalance().getCurrentUiStringInSetting(),false,false,true));
        }
        if (cameraProperties.hasFuction(ICatchCamProperty.ICH_CAM_CAP_LIGHT_FREQUENCY)) {
            categoryOne.addItem(new SettingItem(-1,R.string.setting_power_supply, baseProrertys.getElectricityFrequency().getCurrentUiStringInSetting(),false,false,true));
        }

        if (cameraProperties.cameraModeSupport(ICatchCamMode.ICH_CAM_MODE_TIMELAPSE)) {
            String curTimeLapseInterval;
            if (curCamera.timeLapsePreviewMode == TimeLapseMode.TIME_LAPSE_MODE_STILL) {
                curTimeLapseInterval = baseProrertys.getTimeLapseStillInterval().getCurrentValue();
            } else {
                curTimeLapseInterval = baseProrertys.getTimeLapseVideoInterval().getCurrentValue();
            }
            categoryOne.addItem(new SettingItem(-1,R.string.title_timelapse_mode, baseProrertys.getTimeLapseMode().getCurrentUiStringInSetting(),false,false,true));
            categoryOne.addItem(new SettingItem(-1,R.string.setting_time_lapse_interval, curTimeLapseInterval,false,false,true));
            categoryOne.addItem(new SettingItem(-1,R.string.setting_time_lapse_duration, baseProrertys.gettimeLapseDuration().getCurrentValue(),false,false,true));
        }


        settingMenuList.add(categoryOne);
        settingMenuList.add(getInfoGroup());
        return settingMenuList;
    }


    public LinkedList<SettingMenu> getUSBList(Context context) {
        LinkedList<SettingMenu> settingMenuList = new LinkedList<SettingMenu>() ;
//        settingMenuList.add(new SettingMenu(R.string.setting_audio_switch, "",R.string.setting_title_switch));
//        settingMenuList.add(new SettingMenu(R.string.setting_title_display_temperature, "", R.string.setting_type_switch));
//        settingMenuList.add(new SettingMenu(R.string.setting_audio_switch, ""));
//        settingMenuList.add(new SettingMenu(R.string.setting_image_size, GlobalInfo.getInstance().getCurImageSize(), R.string.setting_type_general));
//        settingMenuList.add(new SettingMenu(R.string.setting_title_storage_location, context.getResources().getString(R.string.setting_value_internal_storage), R.string.setting_type_other));
        settingMenuList.add(new SettingMenu(R.string.setting_app_version, AppInfo.APP_VERSION));
//            settingMenuList.add(new SettingMenu(R.string.setting_auto_download_size_limit, "",R.string.setting_type_switch));
        return settingMenuList;
    }
}
