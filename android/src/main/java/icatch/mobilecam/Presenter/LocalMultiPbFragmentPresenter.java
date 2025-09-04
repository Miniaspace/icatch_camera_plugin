package com.icatch.mobilecam.Presenter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.view.View;

import com.icatch.mobilecam.Log.AppLog;
import com.icatch.mobilecam.Presenter.Interface.BasePresenter;
import com.icatch.mobilecam.R;
import com.icatch.mobilecam.data.AppInfo.AppInfo;
import com.icatch.mobilecam.data.GlobalApp.GlobalInfo;
import com.icatch.mobilecam.ui.ExtendComponent.MyProgressDialog;
import com.icatch.mobilecam.ui.ExtendComponent.MyToast;
import com.icatch.mobilecam.ui.Interface.LocalMultiPbFragmentView;
import com.icatch.mobilecam.ui.activity.LocalPhotoPbActivity;
import com.icatch.mobilecam.ui.activity.LocalVideoPbActivity;
import com.icatch.mobilecam.utils.PanoramaTools;
import com.icatch.mobilecam.utils.StorageUtil;
import com.icatch.mobilecam.utils.fileutils.MFileTools;
import com.tinyai.libmediacomponent.components.filelist.FileItemInfo;
import com.tinyai.libmediacomponent.components.filelist.FileListView;
import com.tinyai.libmediacomponent.components.filelist.OperationMode;
import com.tinyai.libmediacomponent.engine.streaming.type.FileType;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by b.jiang on 2017/5/19.
 */

public class LocalMultiPbFragmentPresenter extends BasePresenter {

    private String TAG = LocalMultiPbFragmentPresenter.class.getSimpleName();
    private LocalMultiPbFragmentView multiPbPhotoView;
    private Activity activity;
    private List<FileItemInfo> pbItemInfoList;
    private Handler handler;
    private int fileType;

    public LocalMultiPbFragmentPresenter(Activity activity, int fileType) {
        super(activity);
        this.activity = activity;
        handler = new Handler();
        this.fileType = fileType;
    }

    public void setView(LocalMultiPbFragmentView localPhotoWallView) {
        this.multiPbPhotoView = localPhotoWallView;
        initCfg();
    }

    public List<FileItemInfo> getPhotoInfoList(int fileType) {
        String fileDate;
        String rootPath = StorageUtil.getRootPath(activity);
        final List<FileItemInfo> photoList = new ArrayList<>();
        List<File> fileList;
        if (fileType == FileType.FILE_TYPE_IMAGE) {
            String filePath = rootPath + AppInfo.DOWNLOAD_PATH_PHOTO;
            fileList = MFileTools.getPhotosOrderByDate(filePath);
        } else {
            String filePath = rootPath + AppInfo.DOWNLOAD_PATH_VIDEO;
            fileList = MFileTools.getVideosOrderByDate(filePath);
        }
        if (fileList == null || fileList.size() <= 0) {
            return null;
        }

        AppLog.i(TAG, "fileList size=" + fileList.size());

        for (int ii = 0; ii < fileList.size(); ii++) {
            File file = fileList.get(ii);
            boolean isPanorama = fileType == FileType.FILE_TYPE_IMAGE ? PanoramaTools.isPanorama(file.getAbsolutePath())
                    :PanoramaTools.isPanoramaForVideo(file.getAbsolutePath());
            //int fileHandle, int fileType, String filePath, String fileName, long fileSize, long time, String thumbPath
            FileItemInfo mGridItem = new FileItemInfo(0,
                    fileType,
                    file.getAbsolutePath(),
                    file.getName(),
                    file.length(),
                    file.lastModified(),
                    "file://" + file.getAbsolutePath()
                    );
            mGridItem.setPanorama(isPanorama);
            photoList.add(mGridItem);

        }

        if (fileType == FileType.FILE_TYPE_IMAGE) {
            GlobalInfo.getInstance().setLocalPhotoList(photoList);
        } else {
            GlobalInfo.getInstance().setLocalVideoList(photoList);
        }
        return photoList;
    }

    public void loadPhotoWall() {
        MyProgressDialog.showProgressDialog(activity, "Loading...");
        new Thread(new Runnable() {
            @Override
            public void run() {
                pbItemInfoList =  getPhotoInfoList(fileType);
                if (pbItemInfoList == null || pbItemInfoList.size() <= 0) {
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
                            multiPbPhotoView.renderList(pbItemInfoList);
                            MyProgressDialog.closeProgressDialog();
                        }
                    });
                }
            }
        }).start();
    }


    public void refreshPhotoWall() {
        AppLog.d(TAG, "refreshPhotoWall layoutType=" + AppInfo.photoWallLayoutType);
        pbItemInfoList = getPhotoInfoList(fileType);
        if (pbItemInfoList == null || pbItemInfoList.size() <= 0) {
            multiPbPhotoView.setFileListViewVisibility(View.GONE);
            multiPbPhotoView.setNoContentTxvVisibility(View.VISIBLE);
        } else {
            multiPbPhotoView.setNoContentTxvVisibility(View.GONE);
//            setAdaper();
        }
    }

    public void itemClick(FileItemInfo fileItemInfo, int position) {
        AppLog.i(TAG, "listViewSelectOrCancelOnce positon=" + position + " AppInfo.photoWallPreviewType=" + AppInfo.photoWallLayoutType);
        String videoPath = pbItemInfoList.get(position).getFilePath();
        OperationMode curOperationMode = multiPbPhotoView.getOperationMode();
        if (curOperationMode == OperationMode.MODE_BROWSE) {
            AppLog.i(TAG, "listViewSelectOrCancelOnce curOperationMode=" + curOperationMode);
            if (fileType == FileType.FILE_TYPE_IMAGE) {
                Intent intent = new Intent();
                intent.putExtra("curfilePosition", position);
                intent.setClass(activity, LocalPhotoPbActivity.class);
                activity.startActivity(intent);
            } else {
                Intent intent = new Intent();
                intent.putExtra("curfilePath", videoPath);
                intent.putExtra("curfilePosition", position);
                intent.putExtra("remote",false);
                intent.setClass(activity, LocalVideoPbActivity.class);
                activity.startActivity(intent);
            }
        }
    }



        public void delete(List<FileItemInfo> list, final FileListView.DeleteResponse deleteResponse){
//        List<FileItemInfo> list = null;
//        FileType fileType = FileType.FILE_PHOTO;
//        AppLog.d(TAG, "delete AppInfo.currentViewpagerPosition=" + AppInfo.currentViewpagerPosition);
//        if (AppInfo.currentViewpagerPosition == 0) {
//            list = multiPbPhotoFragment.getSelectedList();
//            fileType = FileType.FILE_PHOTO;
//        } else if (AppInfo.currentViewpagerPosition == 1) {
//            list = multiPbVideoFragment.getSelectedList();
//            fileType = FileType.FILE_VIDEO;
//        }
        if (list == null || list.size() <= 0) {
            AppLog.d(TAG, "asytaskList size=" + list.size());
            MyToast.show(activity, R.string.gallery_no_file_selected);
        } else {
            CharSequence what = activity.getResources().getString(R.string.gallery_delete_des).replace("$1$", String.valueOf(list.size()));
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setCancelable(false);
            builder.setMessage(what);
            builder.setPositiveButton(activity.getResources().getString(R.string.gallery_cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            final List<FileItemInfo> finalList = list;
            final int finalFileType = fileType;
            builder.setNegativeButton(activity.getResources().getString(R.string.gallery_delete), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    MyProgressDialog.showProgressDialog(activity, R.string.dialog_deleting);
//                    new DeleteFileThread(finalList, finalFileType).run();
                    new Thread(new DeleteFileThread(finalList, finalFileType,deleteResponse)).start();
                }
            });
            builder.create().show();
        }
    }


    class DeleteFileThread implements Runnable {
        private List<FileItemInfo> fileList;
        private List<FileItemInfo> deleteFailedList;
        private List<FileItemInfo> deleteSucceedList;
        private Handler handler;
        private int fileType;
        FileListView.DeleteResponse response = null;

        public DeleteFileThread(List<FileItemInfo> fileList, int fileType, FileListView.DeleteResponse deleteResponse) {
            this.fileList = fileList;
            this.handler = new Handler();
            this.fileType = fileType;
            this.response =deleteResponse;
        }

        @Override
        public void run() {

            AppLog.d(TAG, "DeleteThread");

            if (deleteFailedList == null) {
                deleteFailedList = new LinkedList<>();
            } else {
                deleteFailedList.clear();
            }
            if (deleteSucceedList == null) {
                deleteSucceedList = new LinkedList<>();
            } else {
                deleteSucceedList.clear();
            }
            for (FileItemInfo tempFile : fileList) {
                File file = new File(tempFile.getFilePath());
                if (file.delete() == false) {
                    deleteFailedList.add(tempFile);
                } else {
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

                    }else {
                        if(response != null){
                            response.onComplete(false,null);
                        }
                    }
                    multiPbPhotoView.quitEditMode();
                }
            });
        }
    }
}
