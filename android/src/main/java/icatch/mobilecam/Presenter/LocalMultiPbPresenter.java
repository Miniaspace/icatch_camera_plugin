package com.icatch.mobilecam.Presenter;

import android.app.Activity;
import android.content.Intent;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.icatch.mobilecam.Listener.OnStatusChangedListener;
import com.icatch.mobilecam.Log.AppLog;
import com.icatch.mobilecam.Presenter.Interface.BasePresenter;
import com.icatch.mobilecam.R;
import com.icatch.mobilecam.data.AppInfo.AppInfo;
import com.icatch.mobilecam.data.Mode.OperationMode;
import com.icatch.mobilecam.ui.Fragment.LocalMultiPbFragment2;
import com.icatch.mobilecam.ui.Interface.LocalMultiPbView;
import com.icatch.mobilecam.ui.adapter.ViewPagerAdapter;
import com.tinyai.libmediacomponent.components.filelist.PhotoWallLayoutType;
import com.tinyai.libmediacomponent.engine.streaming.type.FileType;


public class LocalMultiPbPresenter extends BasePresenter {
    private static final String TAG = LocalMultiPbPresenter.class.getSimpleName();
    private LocalMultiPbView multiPbView;
    private Activity activity;
    private LocalMultiPbFragment2 multiPbPhotoFragment;
    private LocalMultiPbFragment2 multiPbVideoFragment;
    private OperationMode curOperationMode = OperationMode.MODE_BROWSE;
    private ViewPagerAdapter adapter;
    private int photoWallLayoutType = PhotoWallLayoutType.PREVIEW_TYPE_GRID;

    public LocalMultiPbPresenter(Activity activity) {
        super(activity);
        this.activity = activity;
        Intent intent = activity.getIntent();
        AppInfo.currentViewpagerPosition = intent.getIntExtra("CUR_POSITION", 0);
    }

    public void setView(LocalMultiPbView multiPbView) {
        this.multiPbView = multiPbView;
        initCfg();
    }

    public void loadViewPager() {
        initViewpager();
    }

    public void reset() {
        photoWallLayoutType = PhotoWallLayoutType.PREVIEW_TYPE_LIST;
        AppInfo.currentViewpagerPosition = 0;
        AppInfo.curVisibleItem = 0;
    }

    private void initViewpager() {
        if (multiPbPhotoFragment == null) {
            multiPbPhotoFragment = LocalMultiPbFragment2.newInstance(FileType.FILE_TYPE_IMAGE);
        }
        multiPbPhotoFragment.setOperationListener(new OnStatusChangedListener() {
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

        });
        if (multiPbVideoFragment == null) {
            multiPbVideoFragment = LocalMultiPbFragment2.newInstance(FileType.FILE_TYPE_VIDEO);
        }
        multiPbVideoFragment.setOperationListener(new OnStatusChangedListener() {
            @Override
            public void onChangeOperationMode(OperationMode operationMode) {
                curOperationMode = operationMode;
                if (curOperationMode == OperationMode.MODE_BROWSE) {
                    multiPbView.setViewPagerScanScroll(true);
                    multiPbView.setTabLayoutClickable(true);
                    AppLog.d(TAG, "multiPbVideoFragment quit EditMode");
                } else {
                    multiPbView.setViewPagerScanScroll(false);
                    multiPbView.setTabLayoutClickable(false);
                }
            }

            @Override
            public void onSelectedItemsCountChanged(int SelectedNum) {
            }

        });
        FragmentManager manager = ((FragmentActivity) activity).getSupportFragmentManager();
        adapter = new ViewPagerAdapter(manager);
        adapter.addFragment(multiPbPhotoFragment, activity.getResources().getString(R.string.title_photo));
        adapter.addFragment(multiPbVideoFragment, activity.getResources().getString(R.string.title_video));
        multiPbView.setViewPageAdapter(adapter);
        multiPbView.setViewPageCurrentItem(AppInfo.currentViewpagerPosition);
    }

    public void updateViewpagerStatus(int arg0) {
        AppLog.d(TAG, "updateViewpagerStatus arg0=" + arg0);
        AppInfo.currentViewpagerPosition = arg0;
    }


    public void changePreviewType() {
        if (curOperationMode == OperationMode.MODE_BROWSE) {
            if (photoWallLayoutType == PhotoWallLayoutType.PREVIEW_TYPE_LIST) {
                photoWallLayoutType = PhotoWallLayoutType.PREVIEW_TYPE_GRID;
                multiPbView.setMenuPhotoWallTypeIcon(R.drawable.ic_view_list_white_24dp);
            } else {
                photoWallLayoutType = PhotoWallLayoutType.PREVIEW_TYPE_LIST;
                multiPbView.setMenuPhotoWallTypeIcon(R.drawable.ic_view_grid_white_24dp);
            }
            multiPbPhotoFragment.changePreviewType(photoWallLayoutType);
            multiPbVideoFragment.changePreviewType(photoWallLayoutType);
            AppLog.d(TAG, " changePreviewType AppInfo.photoWallPreviewType");
        }
    }

    public void reback() {
        if (curOperationMode == OperationMode.MODE_BROWSE) {
            activity.finish();
        } else if (curOperationMode == OperationMode.MODE_EDIT) {
            curOperationMode = OperationMode.MODE_BROWSE;
            if (AppInfo.currentViewpagerPosition == 0) {
                multiPbPhotoFragment.quitEditMode();
            } else if (AppInfo.currentViewpagerPosition == 1) {
                multiPbVideoFragment.quitEditMode();
            }
        }

    }
}
