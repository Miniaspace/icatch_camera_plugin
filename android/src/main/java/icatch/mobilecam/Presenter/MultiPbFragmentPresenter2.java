package com.icatch.mobilecam.Presenter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import androidx.fragment.app.Fragment;

import com.icatch.mobilecam.Function.CameraAction.PbDownloadManager;
import com.icatch.mobilecam.Log.AppLog;
import com.icatch.mobilecam.MyCamera.CameraManager;
import com.icatch.mobilecam.MyCamera.MyCamera;
import com.icatch.mobilecam.Presenter.Interface.BasePresenter;
import com.icatch.mobilecam.R;
import com.icatch.mobilecam.SdkApi.FileOperation;
import com.icatch.mobilecam.data.SystemInfo.SystemInfo;
import com.icatch.mobilecam.data.entity.MultiPbFileResult;
import com.icatch.mobilecam.data.entity.MultiPbItemInfo;
import com.icatch.mobilecam.ui.ExtendComponent.MyProgressDialog;
import com.icatch.mobilecam.ui.ExtendComponent.MyToast;
import com.icatch.mobilecam.ui.Interface.MultiPbFragmentView2;
import com.icatch.mobilecam.ui.RemoteFileHelper2;
import com.icatch.mobilecam.ui.activity.PhotoPbActivity;
import com.icatch.mobilecam.ui.activity.VideoPbActivity;
import com.icatch.mobilecam.utils.imageloader.ImageLoaderConfig;
import com.icatch.mobilecam.utils.imageloader.TutkUriUtil;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
// import com.icatchtek.baseutil.date.DateConverter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import com.icatchtek.reliant.customer.type.ICatchFile;
import com.tinyai.libmediacomponent.components.filelist.FileItemInfo;
import com.tinyai.libmediacomponent.components.filelist.FileListView;
import com.tinyai.libmediacomponent.components.filelist.OperationMode;
import com.tinyai.libmediacomponent.components.filelist.RefreshMode;
import com.tinyai.libmediacomponent.engine.streaming.type.FileType;

import java.util.LinkedList;
import java.util.List;

public class MultiPbFragmentPresenter2 extends BasePresenter {

    private String TAG = MultiPbFragmentPresenter2.class.getSimpleName();
    private MultiPbFragmentView2 multiPbPhotoView;
    private Activity activity;
//    private OperationMode curOperationMode = OperationMode.MODE_BROWSE;
    private List<MultiPbItemInfo> pbItemInfoList = new LinkedList<>();
    private FileOperation fileOperation = null;
    private Handler handler;
    private int fileType;
    private int fileTotalNum;
    private int curIndex = 1;
    private int maxNum = 15;
    private boolean isMore = true;
    private boolean supportSegmentedLoading = false;
    private Fragment fragment;

    public MultiPbFragmentPresenter2(Activity activity, int fileType) {
        super(activity);
        this.activity = activity;
        handler = new Handler();
        this.fileType = fileType;
        if (CameraManager.getInstance().getCurCamera() != null) {
            fileOperation = CameraManager.getInstance().getCurCamera().getFileOperation();
        }
    }

    public void setView(MultiPbFragmentView2 pbFragmentView) {
        this.multiPbPhotoView = pbFragmentView;
        initCfg();
    }

    public void setFragment(Fragment fragment) {
        this.fragment = fragment;
    }

    public void resetCurIndex() {
        curIndex = 1;
    }

    public synchronized List<MultiPbItemInfo> getRemotePhotoInfoList() {
        if (fileOperation == null) {
            return null;
        }
        if (supportSegmentedLoading) {
            fileTotalNum = RemoteFileHelper2.getInstance().getFileCount(fileOperation, fileType);
            MultiPbFileResult multiPbFileResult = RemoteFileHelper2.getInstance().getRemoteFile(fileOperation, fileType, fileTotalNum, curIndex);
            curIndex = multiPbFileResult.getLastIndex();
            isMore = multiPbFileResult.isMore();
            return multiPbFileResult.getFileList();
        } else {
            return RemoteFileHelper2.getInstance().getRemoteFile(fileOperation, fileType);
        }
    }

    public List<FileItemInfo> loadMoreFile() {
        if (isMore) {
            List<MultiPbItemInfo> tempList = getRemotePhotoInfoList();
            if (tempList != null && tempList.size() > 0) {
                pbItemInfoList.addAll(tempList);
            }
            RemoteFileHelper2.getInstance().setLocalFileList(pbItemInfoList, fileType);
            return convertList(tempList);
        } else {
            // 显示加载到底的提示
            return null;
        }
    }

    public void loadPhotoWall() {
        MyProgressDialog.showProgressDialog(activity, R.string.message_loading);
        supportSegmentedLoading = RemoteFileHelper2.getInstance().isSupportSegmentedLoading();

        multiPbPhotoView.setRefreshMode(supportSegmentedLoading ?  RefreshMode.PULL_FROM_END:RefreshMode.DISABLED);
        new Thread(new Runnable() {
            @Override
            public void run() {

                if (supportSegmentedLoading) {
                    if (fileOperation == null) {
                        return;
                    }
                    fileTotalNum = RemoteFileHelper2.getInstance().getFileCount(fileOperation, fileType);
                    pbItemInfoList.clear();
                    List<MultiPbItemInfo> temp = RemoteFileHelper2.getInstance().getLocalFileList(fileType);
                    if (fileTotalNum > 0 && temp != null && temp.size() > 0) {
                        pbItemInfoList.addAll(temp);
                    } else if (fileTotalNum > 0) {
                        resetCurIndex();
                        List tempList = getRemotePhotoInfoList();
                        if (tempList != null && tempList.size() > 0) {
                            pbItemInfoList.addAll(tempList);
                        }
                        RemoteFileHelper2.getInstance().setLocalFileList(pbItemInfoList, fileType);
                    }
                    AppLog.d(TAG, "pbItemInfoList=" + pbItemInfoList);
                    if (fileTotalNum <= 0 || pbItemInfoList.size() <= 0) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                MyProgressDialog.closeProgressDialog();
                                multiPbPhotoView.setFileListViewVisibility(View.GONE);
                                multiPbPhotoView.setNoContentTxvVisibility(View.VISIBLE);
//                            MyToast.show(activity, "no file");
                            }
                        });
                    } else {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                multiPbPhotoView.setNoContentTxvVisibility(View.GONE);
                                multiPbPhotoView.setFileListViewVisibility(View.VISIBLE);
                                multiPbPhotoView.renderList(convertList(pbItemInfoList));
                                MyProgressDialog.closeProgressDialog();
                            }
                        });
                    }
                } else {
                    pbItemInfoList.clear();
                    List<MultiPbItemInfo> temp = RemoteFileHelper2.getInstance().getLocalFileList(fileType);
                    if (temp != null) {
                        pbItemInfoList.addAll(temp);
                    } else {
                        resetCurIndex();
                        List tempList = getRemotePhotoInfoList();
                        if (tempList != null && tempList.size() > 0) {
                            pbItemInfoList.addAll(tempList);
                        }
                        RemoteFileHelper2.getInstance().setLocalFileList(pbItemInfoList, fileType);
                    }
                    AppLog.d(TAG, "pbItemInfoList=" + pbItemInfoList);
                    if (pbItemInfoList.size() <= 0) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                MyProgressDialog.closeProgressDialog();
                                multiPbPhotoView.setFileListViewVisibility(View.GONE);
                                multiPbPhotoView.setNoContentTxvVisibility(View.VISIBLE);
//                            MyToast.show(activity, "no file");
                            }
                        });
                    } else {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                multiPbPhotoView.setNoContentTxvVisibility(View.GONE);
                                multiPbPhotoView.setFileListViewVisibility(View.VISIBLE);
                                multiPbPhotoView.renderList(convertList(pbItemInfoList));
                                MyProgressDialog.closeProgressDialog();
                            }
                        });
                    }
                }

            }
        }).start();
    }

    private List<FileItemInfo> convertList(List<MultiPbItemInfo> list){
        List<FileItemInfo> fileItemInfos =  new LinkedList<>();

        if(list == null){
            return fileItemInfos;
        }
        for (int i = 0; i < list.size() ; i++) {
            MultiPbItemInfo itemInfo = list.get(i);
            ICatchFile iCatchFile = itemInfo.iCatchFile;
            // int fileHandle, int fileType, String filePath, String fileName, long fileSize, String fileDate, String thumbPath
            //int fileHandle, int fileType, String filePath, String fileName, long fileSize, long time, double frameRate, int fileWidth, int fileHeight, int fileProtection, int fileDuration, String thumbPath
            // 将String类型的日期转换为long类型的时间戳
            long timeStamp = 0;
            try {
                SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd'T'HHmmss", Locale.getDefault());
                Date date = format.parse(iCatchFile.getFileDate());
                timeStamp = date.getTime();
            } catch (Exception e) {
                timeStamp = System.currentTimeMillis();
            }
            fileItemInfos.add(new FileItemInfo(iCatchFile.getFileHandle(),
                            iCatchFile.getFileType(),
                            iCatchFile.getFilePath(),
                            iCatchFile.getFileName(),
                            iCatchFile.getFileSize(),
                            timeStamp,
                            TutkUriUtil.getTutkThumbnailUri(iCatchFile)
                    ));
        }

        return fileItemInfos;
    }

    private LinkedList<ICatchFile> convertFileList(List<FileItemInfo> fileList){
        LinkedList<ICatchFile> iCatchFileList = new LinkedList<>();
        if (fileList == null) {
            return iCatchFileList;
        }
        for (int ii = 0; ii < fileList.size(); ii++) {
            FileItemInfo fileItemInfo = fileList.get(ii);
            //int fileHandle, int fileType, String filePath, String fileName, long fileSize, String fileDate
            ICatchFile iCatchFile = new ICatchFile(fileItemInfo.getFileHandle(),
                    fileItemInfo.getFileType(),
                    fileItemInfo.getFilePath(),
                    fileItemInfo.getFileName(),
                    fileItemInfo.getFileSize(),
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date(fileItemInfo.getTime())));
            iCatchFileList.add(iCatchFile);

        }
        return iCatchFileList;
    }

    public void refreshPhotoWall() {
        Log.i("1122", "refreshPhotoWall");
        if (pbItemInfoList == null || pbItemInfoList.size() <= 0) {
            multiPbPhotoView.setFileListViewVisibility(View.GONE);
            multiPbPhotoView.setNoContentTxvVisibility(View.VISIBLE);
        } else {
            multiPbPhotoView.setNoContentTxvVisibility(View.GONE);
        }
    }


    public synchronized void itemClick(FileItemInfo fileItemInfo, final int position) {
        AppLog.i(TAG, "listViewSelectOrCancelOnce positon=" + position );
        OperationMode curOperationMode = multiPbPhotoView.getOperationMode();
        if ( curOperationMode == OperationMode.MODE_BROWSE) {
            AppLog.i(TAG, "listViewSelectOrCancelOnce curOperationMode=" + curOperationMode);

            if (fileType == FileType.FILE_TYPE_IMAGE) {
                Intent intent = new Intent();
                intent.putExtra("curfilePosition", position);
                intent.putExtra("fileType", fileType);
                intent.setClass(activity, PhotoPbActivity.class);
//                activity.startActivity(intent);
                fragment.startActivityForResult(intent, 1000);
            } else {
                MyProgressDialog.showProgressDialog(activity, R.string.wait);
                MyCamera myCamera = CameraManager.getInstance().getCurCamera();
                if(myCamera != null){
                    myCamera.setLoadThumbnail(false);
                }
                stopLoad();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent();
                        intent.putExtra("curfilePosition", position);
                        intent.putExtra("fileType", fileType);
//                        intent.putExtra("remote",true);
//                        intent.setClass(activity, CommonVideoPlayerActivity.class);
                        intent.setClass(activity, VideoPbActivity.class);
//                        activity.startActivity(intent);
                        fragment.startActivityForResult(intent, 1000);
                        MyProgressDialog.closeProgressDialog();
                    }
                }, 1500);
            }
        }
    }

    public void emptyFileList() {
        RemoteFileHelper2.getInstance().clearFileList(fileType);
    }

    public void deleteFile(final List<FileItemInfo> fileItemInfolist, final  FileListView.DeleteResponse response) {

        if (fileItemInfolist == null || fileItemInfolist.size() <= 0) {
            AppLog.d(TAG, "asytaskList size=" + fileItemInfolist.size());
            MyToast.show(activity, R.string.gallery_no_file_selected);
        } else {
            CharSequence what = activity.getResources().getString(R.string.gallery_delete_des).replace("$1$", String.valueOf(fileItemInfolist.size()));
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setCancelable(false);
            builder.setMessage(what);
            builder.setPositiveButton(activity.getResources().getString(R.string.gallery_cancel), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    if(response != null){
                        response.onComplete(false,null);
                    }
                }
            });

            final int finalFileType = fileType;
            builder.setNegativeButton(activity.getResources().getString(R.string.gallery_delete), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    MyProgressDialog.showProgressDialog(activity, R.string.dialog_deleting);
                    multiPbPhotoView.quitEditMode();
                    //new DeleteFileThread(finalList, finalFileType).run();
                    new Thread(new DeleteFileThread(fileItemInfolist, finalFileType,response)).start();
                }
            });
            builder.create().show();
        }
    }

    public void stopLoad() {
        ImageLoaderConfig.stopLoad();
    }

    private class DeleteFileThread implements Runnable {
        private List<FileItemInfo> fileList;
//        private List<MultiPbItemInfo> deleteFailedList;
        private List<FileItemInfo> deleteSucceedList;
        private Handler handler;
        private FileOperation fileOperation;
        private int fileType;
        FileListView.DeleteResponse response;

        public DeleteFileThread(List<FileItemInfo> fileList, int fileType,  FileListView.DeleteResponse response) {
            this.fileList = fileList;
            this.handler = new Handler();
            if (CameraManager.getInstance().getCurCamera() != null) {
                this.fileOperation = CameraManager.getInstance().getCurCamera().getFileOperation();
            }
            this.fileType = fileType;
            this.response = response;
        }

        @Override
        public void run() {
            AppLog.d(TAG, "DeleteThread");
//            deleteFailedList = new LinkedList<MultiPbItemInfo>();
            if (fileOperation == null) {
                return;
            }
            deleteSucceedList = new LinkedList<>();
            for (FileItemInfo tempFile : fileList) {
                AppLog.d(TAG, "deleteFile f.getFileHandle =" + tempFile.getFileHandle());
                //int fileHandle, int fileType, String filePath, String fileName, long fileSize, String fileDate, String thumbPath
                if (fileOperation.deleteFile(
                        new ICatchFile(tempFile.getFileHandle(),
                        tempFile.getFileType(),
                        tempFile.getFilePath(),
                        tempFile.getFileName(),
                        tempFile.getFileSize(),
                                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date(tempFile.getTime()))
                        ))) {
                    deleteSucceedList.add(tempFile);
                }
            }

            handler.post(new Runnable() {
                @Override
                public void run() {
                    MyProgressDialog.closeProgressDialog();
                    if(deleteSucceedList.size() > 0) {
                        if(response != null){
                            response.onComplete(true,deleteSucceedList);
                        }
//                        pbItemInfoList.removeAll(convertFileList(deleteSucceedList));
                        for (FileItemInfo info:deleteSucceedList
                             ) {
                            removeLocal(info);
                        }
                        RemoteFileHelper2.getInstance().setLocalFileList(pbItemInfoList, fileType);
                    }else {
                        if(response != null){
                            response.onComplete(false,null);
                        }
                    }
                    if (supportSegmentedLoading) {
                        //删除后fileHandle变化，需重新获取列表
                        resetCurIndex();
                        RemoteFileHelper2.getInstance().clearFileList(fileType);
                        loadPhotoWall();
                    } else {
                        refreshPhotoWall();
                    }
                }
            });
        }
    }

    private void removeLocal(FileItemInfo fileItemInfo) {
        MultiPbItemInfo temp = null;
        for (MultiPbItemInfo info:pbItemInfoList
             ) {
            if(info.getFileHandle() == fileItemInfo.getFileHandle()){
                temp= info;
                break;
            }
        }
        if(temp != null){
            pbItemInfoList.remove(temp);
        }
    }

    public void download(List<FileItemInfo> fileItemInfolist) {
        LinkedList<ICatchFile> fileList =  convertFileList(fileItemInfolist);
        long fileSizeTotal = 0;

        if (fileList == null || fileList.size() <= 0) {
            AppLog.d(TAG, "asytaskList size=" + fileList.size());
            MyToast.show(activity, R.string.gallery_no_file_selected);
        } else {
            for (ICatchFile temp : fileList
            ) {
                fileSizeTotal += temp.getFileSize();
            }
            if (SystemInfo.getSDFreeSize(activity) < fileSizeTotal) {
                MyToast.show(activity, R.string.text_sd_card_memory_shortage);
            } else {
                multiPbPhotoView.quitEditMode();
                PbDownloadManager downloadManager = new PbDownloadManager(activity,fileList);
                downloadManager.show();
            }
        }
    }
}