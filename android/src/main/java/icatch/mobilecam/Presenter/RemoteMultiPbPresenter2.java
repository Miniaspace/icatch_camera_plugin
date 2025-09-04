package com.icatch.mobilecam.Presenter;

import android.app.Activity;
import android.os.Handler;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.icatch.mobilecam.Function.SDKEvent;
import com.icatch.mobilecam.Listener.OnStatusChangedListener;
import com.icatch.mobilecam.Log.AppLog;
import com.icatch.mobilecam.MyCamera.CameraManager;
import com.icatch.mobilecam.MyCamera.MyCamera;
import com.icatch.mobilecam.Presenter.Interface.BasePresenter;
import com.icatch.mobilecam.R;
import com.icatch.mobilecam.SdkApi.CameraProperties;
import com.icatch.mobilecam.data.AppInfo.AppInfo;
import com.icatch.mobilecam.data.GlobalApp.GlobalInfo;
import com.icatch.mobilecam.data.Mode.OperationMode;
import com.icatch.mobilecam.data.PropertyId.PropertyId;
import com.icatch.mobilecam.ui.ExtendComponent.MyProgressDialog;
import com.icatch.mobilecam.ui.ExtendComponent.MyToast;
import com.icatch.mobilecam.ui.Fragment.BaseMultiPbFragment2;
import com.icatch.mobilecam.ui.Fragment.RemoteMultiPbFragment2;
import com.icatch.mobilecam.ui.Interface.MultiPbView2;
import com.icatch.mobilecam.ui.RemoteFileHelper2;
import com.icatch.mobilecam.ui.adapter.ViewPagerAdapter;
import com.icatch.mobilecam.utils.FileFilter;
import com.icatch.mobilecam.utils.imageloader.ImageLoaderConfig;
import com.icatchtek.control.customer.type.ICatchCamFeatureID;
import com.tinyai.libmediacomponent.engine.streaming.type.FileType;

import java.util.ArrayList;
import java.util.List;


public class RemoteMultiPbPresenter2 extends BasePresenter {
    private static final String TAG = RemoteMultiPbPresenter2.class.getSimpleName();
    private MultiPbView2 multiPbView;
    private Activity activity;
    private List<BaseMultiPbFragment2> fragments;
    OperationMode curOperationMode = OperationMode.MODE_BROWSE;
    ViewPagerAdapter adapter;
    Handler handler = new Handler();

    public RemoteMultiPbPresenter2(Activity activity) {
        super(activity);
        this.activity = activity;
    }

    public void setView(MultiPbView2 multiPbView) {
        this.multiPbView = multiPbView;
        initCfg();
    }

    public void loadViewPager() {
        RemoteFileHelper2.getInstance().initSupportCapabilities();
        initViewpager();
    }


    public void reset() {
        AppInfo.currentViewpagerPosition = 0;
        AppInfo.curVisibleItem = 0;
        RemoteFileHelper2.getInstance().setFileFilter(null);
        MyCamera camera = CameraManager.getInstance().getCurCamera();

        CameraProperties cameraProperties = camera.getCameraProperties();
        if (cameraProperties != null
                && cameraProperties.hasFuction(PropertyId.DEFALUT_TO_PREVIEW)
                && cameraProperties.checkCameraCapabilities(ICatchCamFeatureID.ICH_CAM_APP_DEFAULT_TO_PLAYBACK)) {
            camera.disconnect();
        }
    }

    private OnStatusChangedListener onStatusChangedListener = new OnStatusChangedListener() {
        @Override
        public void onChangeOperationMode(OperationMode operationMode) {
            curOperationMode = operationMode;
            if (curOperationMode == OperationMode.MODE_BROWSE) {
                multiPbView.setViewPagerScanScroll(true);
                multiPbView.setTabLayoutClickable(true);
                AppLog.d(TAG, "multiPbPhotoFragment quit EditMode");
            } else {
                multiPbView.setViewPagerScanScroll(false);
                multiPbView.setTabLayoutClickable(false);
            }
        }

        @Override
        public void onSelectedItemsCountChanged(int SelectedNum) {

        }
    };

    private void initViewpager() {
        if (fragments == null) {
            fragments = new ArrayList<>();
        } else {
            fragments.clear();
        }
        FragmentManager manager = ((FragmentActivity) activity).getSupportFragmentManager();
        adapter = new ViewPagerAdapter(manager);
        //图片
        BaseMultiPbFragment2 multiPbPhotoFragment = RemoteMultiPbFragment2.newInstance(FileType.FILE_TYPE_IMAGE);
//        BaseMultiPbFragment multiPbPhotoFragment = RemoteMultiPbPhotoFragment.newInstance(FileType.FILE_PHOTO.ordinal());
        multiPbPhotoFragment.setOperationListener(onStatusChangedListener);
        fragments.add(multiPbPhotoFragment);
        adapter.addFragment(multiPbPhotoFragment, activity.getResources().getString(R.string.title_photo));
        //视频
        BaseMultiPbFragment2 multiPbVideoFragment = RemoteMultiPbFragment2.newInstance(FileType.FILE_TYPE_VIDEO);
//        BaseMultiPbFragment multiPbVideoFragment = RemoteMultiPbPhotoFragment.newInstance(FileType.FILE_VIDEO.ordinal());
        multiPbVideoFragment.setOperationListener(onStatusChangedListener);
        fragments.add(multiPbVideoFragment);
        adapter.addFragment(multiPbVideoFragment, activity.getResources().getString(R.string.title_video));
        //紧急录影
//        RemoteMultiPbFragment multiPbEmergencyVideoFragment = RemoteMultiPbFragment.newInstance(FileType.FILE_EMERGENCY_VIDEO.ordinal());
//        multiPbEmergencyVideoFragment.setOperationListener(onStatusChangedListener);
//        fragments.add(multiPbEmergencyVideoFragment);
//        adapter.addFragment(multiPbEmergencyVideoFragment, activity.getResources().getString(R.string.title_emergency_video));

        multiPbView.setViewPageAdapter(adapter);
        multiPbView.setViewPageCurrentItem(AppInfo.currentViewpagerPosition);
    }

    public void updateViewpagerStatus(int arg0) {
        AppLog.d(TAG, "updateViewpagerStatus arg0=" + arg0);
        AppInfo.currentViewpagerPosition = arg0;
    }

    public void changePreviewType(int layoutType) {
        if (curOperationMode == OperationMode.MODE_BROWSE) {
            if (fragments != null) {
                for (BaseMultiPbFragment2 fragment : fragments
                ) {
                    fragment.changePreviewType(layoutType);
                }
            }
            AppLog.d(TAG, " changePreviewType AppInfo.photoWallPreviewType");
        } else {
            MyToast.show(activity, R.string.editing_unable_switch);
        }
    }

    public void reback() {
        AppLog.i(TAG,"reback curOperationMode:" + curOperationMode);
        if (curOperationMode == OperationMode.MODE_BROWSE) {
            MyProgressDialog.showProgressDialog(activity, R.string.wait);
            MyCamera camera = CameraManager.getInstance().getCurCamera();
            if(camera != null){
                camera.setLoadThumbnail(false);
            }
            ImageLoaderConfig.stopLoad();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    MyProgressDialog.closeProgressDialog();
                    activity.finish();
                }
            }, 1500);

        } else if (curOperationMode == OperationMode.MODE_EDIT) {
            curOperationMode = OperationMode.MODE_BROWSE;
            int index = multiPbView.getViewPageIndex();
            BaseMultiPbFragment2 fragment = fragments.get(index);
            if (fragment != null) {
                fragment.quitEditMode();
            }
        }
    }


    private void reloadFileList(){
        RemoteFileHelper2.getInstance().clearAllFileList();
        if (fragments != null && fragments.size() > 0) {
            BaseMultiPbFragment2 fragment = fragments.get(multiPbView.getViewPageIndex());
            if (fragment != null) {
                fragment.loadPhotoWall();
            }
        }
    }

    public void setFileFilter(FileFilter fileFilter) {
        RemoteFileHelper2.getInstance().setFileFilter(fileFilter);
        reloadFileList();
    }

    public void setSdCardEventListener() {
        GlobalInfo.getInstance().setOnEventListener(new GlobalInfo.OnEventListener() {
            @Override
            public void eventListener(int sdkEventId) {
                switch (sdkEventId){
                    case SDKEvent.EVENT_SDCARD_REMOVED:
                        MyToast.show(activity,R.string.dialog_card_removed);
                        reloadFileList();
                        break;
                    case SDKEvent.EVENT_SDCARD_INSERT:
                        MyToast.show(activity,R.string.dialog_card_inserted);
                        reloadFileList();
                        break;
                }
            }
        });
    }
}
