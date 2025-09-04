package com.icatch.mobilecam.ui.Fragment;


import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.icatch.mobilecam.Listener.OnStatusChangedListener;
import com.icatch.mobilecam.Log.AppLog;
import com.icatch.mobilecam.Presenter.LocalMultiPbFragmentPresenter;
import com.icatch.mobilecam.R;
import com.icatch.mobilecam.ui.Interface.LocalMultiPbFragmentView;
import com.tinyai.libmediacomponent.components.filelist.FileItemInfo;
import com.tinyai.libmediacomponent.components.filelist.FileListView;
import com.tinyai.libmediacomponent.components.filelist.OperationMode;
import com.tinyai.libmediacomponent.components.filelist.RefreshMode;
import com.tinyai.libmediacomponent.engine.streaming.type.FileType;

import java.util.List;

public class LocalMultiPbFragment extends Fragment implements LocalMultiPbFragmentView {
    private static final String TAG = LocalMultiPbFragment.class.getSimpleName();
    TextView noContentTxv;
    LocalMultiPbFragmentPresenter presenter;
    private OnStatusChangedListener modeChangedListener;
    private boolean isCreated = false;
    private boolean isVisible = false;
    private int fileType = FileType.FILE_TYPE_IMAGE;
    private FileListView fileListView;

    public LocalMultiPbFragment() {
        // Required empty public constructor
    }

    public static LocalMultiPbFragment newInstance(int param1) {
        LocalMultiPbFragment fragment = new LocalMultiPbFragment();
        Bundle args = new Bundle();
        args.putInt("FILE_TYPE", param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int fileTypeInt = 0;
        if (getArguments() != null) {
            fileTypeInt = getArguments().getInt("FILE_TYPE");
        }
        fileType = fileTypeInt;
        AppLog.d(TAG, "onCreate fileType=" + fileTypeInt);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        AppLog.d(TAG, "MultiPbPhotoFragment onCreateView");
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_local_pb_list, container, false);
        fileListView = view.findViewById(R.id.file_list_view);
        noContentTxv = view.findViewById(R.id.no_content_txv);
        presenter = new LocalMultiPbFragmentPresenter(getActivity(), fileType);
        presenter.setView(this);
        fileListView.setRefreshMode(RefreshMode.DISABLED);
        fileListView.setEditDownloadVisibility(View.GONE);
        fileListView.setGridSpanCount(4);
        fileListView.setItemContentClickListener(new FileListView.ItemContentClickListener() {
            @Override
            public void itemOnClick(FileItemInfo fileItemInfo, int position) {
                presenter.itemClick(fileItemInfo,position);
            }

            @Override
            public void itemOnLongClick(FileItemInfo fileItemInfo, int position) {
                fileListView.enterEditMode(position);
                if(modeChangedListener!=null){
                    modeChangedListener.onChangeOperationMode(com.icatch.mobilecam.data.Mode.OperationMode.MODE_EDIT);
                }
            }

            @Override
            public void downloadOnClick(FileItemInfo fileItemInfo, int position) {

            }

            @Override
            public void deleteOnClick(FileItemInfo fileItemInfo, int position) {

            }
        });

        fileListView.setEditBtnClickListener(new FileListView.EditBtnClickListener() {
            @Override
            public void downloadOnClick(List<FileItemInfo> list) {

            }

            @Override
            public void deleteOnClick(List<FileItemInfo> list, FileListView.DeleteResponse deleteResponse) {
                presenter.delete(list,deleteResponse);
            }
        });

        isCreated = true;
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        AppLog.d(TAG, "start onResume() isVisible=" + isVisible + " presenter=" + presenter);
        if (isVisible) {
            presenter.loadPhotoWall();
        }
        AppLog.d(TAG, "end onResume");
    }

    @Override
    public void onStop() {
        AppLog.d(TAG, "start onStop()");
        super.onStop();
    }

    @Override
    public void onDestroy() {
        AppLog.d(TAG, "start onDestroy()");
        super.onDestroy();
    }

    public void changePreviewType(int layoutType) {
        AppLog.d(TAG, "start changePreviewType presenter=" + presenter);
        fileListView.changeLayoutType(layoutType);
    }

    public void quitEditMode() {
        fileListView.exitEditMode();
        if(modeChangedListener!=null){
            modeChangedListener.onChangeOperationMode(com.icatch.mobilecam.data.Mode.OperationMode.MODE_BROWSE);
        }
    }

    @Override
    public OperationMode getOperationMode() {
        return fileListView.getOperationMode();
    }

    @Override
    public void setFileListViewVisibility(int visibility) {
        if (fileListView.getVisibility() != visibility) {
            fileListView.setVisibility(visibility);
        }
    }

    @Override
    public void setNoContentTxvVisibility(int visibility) {
        int v = noContentTxv.getVisibility();
        if (v != visibility) {
            noContentTxv.setVisibility(visibility);
        }
    }

    @Override
    public void renderList(List<FileItemInfo> list) {
        if(fileListView!=null){
            fileListView.renderList(list);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.i("1122", "MultiPbPhotoFragment onConfigurationChanged");
        presenter.refreshPhotoWall();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        AppLog.d(TAG, "setUserVisibleHint isVisibleToUser=" + isVisibleToUser);
        AppLog.d(TAG, "setUserVisibleHint isCreated=" + isCreated);
        isVisible = isVisibleToUser;
        if (isCreated == false) {
            return;
        }
        if (isVisibleToUser == false) {
            quitEditMode();
        } else {
            presenter.loadPhotoWall();
        }
    }

    public void refreshPhotoWall() {
        presenter.refreshPhotoWall();
    }

    public void setOperationListener(OnStatusChangedListener modeChangedListener) {
        this.modeChangedListener = modeChangedListener;
    }
}

