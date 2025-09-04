package com.icatch.mobilecam.ui.Fragment;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.icatch.mobilecam.Listener.OnStatusChangedListener;
import com.icatch.mobilecam.Log.AppLog;
import com.icatch.mobilecam.Presenter.MultiPbFragmentPresenter2;
import com.icatch.mobilecam.R;
import com.icatch.mobilecam.ui.Interface.MultiPbFragmentView2;
import com.icatch.mobilecam.ui.RemoteFileHelper2;
import com.tinyai.libmediacomponent.components.filelist.FileItemInfo;
import com.tinyai.libmediacomponent.components.filelist.FileListView;
import com.tinyai.libmediacomponent.components.filelist.OperationMode;

import java.util.List;

public class RemoteMultiPbFragment2 extends BaseMultiPbFragment2 implements MultiPbFragmentView2 {
    private static final String TAG = "RemoteMultiPbFragment2";
//    RecyclerView recyclerView;
    private FileListView fileListView;
    MultiPbFragmentPresenter2 presenter;
    private OnStatusChangedListener modeChangedListener;
    private boolean isCreated = false;
    private boolean isVisible = false;
    private TextView noContentTxv;
    private int fileType;
    private boolean hasDeleted = false;

    public RemoteMultiPbFragment2() {
        // Required empty public constructor
    }

    public static RemoteMultiPbFragment2 newInstance(int param1) {
        RemoteMultiPbFragment2 fragment = new RemoteMultiPbFragment2();
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
        this.fileType = fileTypeInt;
        AppLog.d(TAG, "onCreate fileType=" + fileType);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        AppLog.d(TAG, "onCreateView fileType=" + fileType);
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_multi_pb2, container, false);
        fileListView = view.findViewById(R.id.file_list_view);
        noContentTxv = (TextView) view.findViewById(R.id.no_content_txv);

        presenter = new MultiPbFragmentPresenter2(getActivity(), fileType);
        presenter.setView(this);
        presenter.setFragment(this);
        fileListView.setGridSpanCount(4);
        fileListView.setItemContentClickListener(new FileListView.ItemContentClickListener() {
            @Override
            public void itemOnClick(FileItemInfo fileItemInfo,int position) {
                presenter.itemClick(fileItemInfo,position);
            }

            @Override
            public void itemOnLongClick(FileItemInfo fileItemInfo,int position) {
                if (isVisible) {
                    fileListView.enterEditMode(position);
                    if(modeChangedListener!=null){
                        modeChangedListener.onChangeOperationMode(com.icatch.mobilecam.data.Mode.OperationMode.MODE_EDIT);
                    }
                }
            }

            @Override
            public void downloadOnClick(FileItemInfo fileItemInfo,int position) {

            }

            @Override
            public void deleteOnClick(FileItemInfo fileItemInfo,int position) {

            }
        });

        fileListView.setEditBtnClickListener(new FileListView.EditBtnClickListener() {
            @Override
            public void downloadOnClick(List<FileItemInfo> list) {
                presenter.download(list);
            }

            @Override
            public void deleteOnClick(List<FileItemInfo> list, FileListView.DeleteResponse response) {
                presenter.deleteFile(list,response);
            }
        });
        isCreated = true;
        fileListView.setRefreshListener(new FileListView.RefreshCallback() {
            @Override
            public List<FileItemInfo> refreshData() {
               return null;
            }

            @Override
            public List<FileItemInfo> getModeData() {
                //模拟获取下一页数据逻辑

                return presenter.loadMoreFile();
            }

        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        AppLog.d(TAG, "start onResume() isVisible=" + isVisible + " fileType=" + fileType);
        if (isVisible) {
            if(hasDeleted &&RemoteFileHelper2.getInstance().isSupportSegmentedLoading()){
                presenter.resetCurIndex();
                RemoteFileHelper2.getInstance().clearFileList(fileType);
            }
            presenter.loadPhotoWall();
        }
        hasDeleted = false;
        AppLog.d(TAG, "end onResume");
    }

    @Override
    public void onStop() {
        AppLog.d(TAG, "start onStop()");
        //presenter.stopLoad();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        AppLog.d(TAG, "start onDestroy()");
        super.onDestroy();
        presenter.emptyFileList();
    }

    @Override
    public void changePreviewType(int layoutType) {
        AppLog.d(TAG, "start changePreviewType presenter=" + presenter);
       fileListView.changeLayoutType(layoutType);

    }

    @Override
    public void quitEditMode() {
        fileListView.exitEditMode();
        if(modeChangedListener!=null){
            modeChangedListener.onChangeOperationMode(com.icatch.mobilecam.data.Mode.OperationMode.MODE_BROWSE);
        }
    }

    @Override
    public void setFileListViewVisibility(int visibility) {
        fileListView.setVisibility(visibility);
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
    public void setRefreshMode(int refreshMode) {
        if(fileListView!=null){
            fileListView.setRefreshMode(refreshMode);
        }
    }

    @Override
    public OperationMode getOperationMode() {
        if(fileListView!=null){
            return fileListView.getOperationMode();
        }
        return OperationMode.MODE_BROWSE;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.i("1122", "RemoteMultiPbPhotoFragment onConfigurationChanged");
        presenter.refreshPhotoWall();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        AppLog.d(TAG, "setUserVisibleHint isVisibleToUser=" + isVisibleToUser + " fileType=" + fileType);
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

    public void setOperationListener(OnStatusChangedListener modeChangedListener) {
        this.modeChangedListener = modeChangedListener;
    }

    public int getFileType() {
        return fileType;
    }

    public void loadPhotoWall(){
        if (isVisible) {
            presenter.loadPhotoWall();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        AppLog.d(TAG,"onActivityResult requestCode=" + requestCode);
        AppLog.d(TAG,"onActivityResult data=" + data);
        AppLog.d(TAG,"onActivityResult curfileType=" + fileType);
        if(data != null){
            hasDeleted = data.getBooleanExtra("hasDeleted",false);
            int fileTypeInt = data.getIntExtra("fileType",-1);
            AppLog.d(TAG,"onActivityResult hasDeleted=" + hasDeleted + " fileType=" +fileTypeInt);
        }
    }

}
