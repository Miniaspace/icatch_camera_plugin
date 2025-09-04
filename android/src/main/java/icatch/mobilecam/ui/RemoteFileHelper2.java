package com.icatch.mobilecam.ui;

import com.icatch.mobilecam.Log.AppLog;
import com.icatch.mobilecam.MyCamera.CameraManager;
import com.icatch.mobilecam.MyCamera.MyCamera;
import com.icatch.mobilecam.SdkApi.CameraProperties;
import com.icatch.mobilecam.SdkApi.FileOperation;
import com.icatch.mobilecam.data.PropertyId.PropertyId;
import com.icatch.mobilecam.data.entity.MultiPbFileResult;
import com.icatch.mobilecam.data.entity.MultiPbItemInfo;
import com.icatch.mobilecam.utils.ConvertTools;
import com.icatch.mobilecam.utils.FileFilter;
import com.icatch.mobilecam.utils.PanoramaTools;
import com.icatchtek.control.customer.type.ICatchCamFeatureID;
import com.icatchtek.control.customer.type.ICatchCamListFileFilter;
import com.icatchtek.reliant.customer.type.ICatchFile;
import com.icatchtek.reliant.customer.type.ICatchFileType;
import com.tinyai.libmediacomponent.engine.streaming.type.FileType;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * @author b.jiang
 * @date 2020/1/9
 * @description
 */
public class RemoteFileHelper2 {
    private String TAG = RemoteFileHelper2.class.getSimpleName();
    private static RemoteFileHelper2 instance;
    public HashMap<Integer, List<MultiPbItemInfo>> listHashMap = new HashMap<>();
    private int curFilterFileType = ICatchCamListFileFilter.ICH_OFC_FILE_TYPE_ALL_MEDIA;
    private FileFilter fileFilter = null;
    private final int MAX_NUM = 30;
    private boolean supportSegmentedLoading = false;
    private boolean supportSetFileListAttribute = false;
    private int sensorsNum = 1;

    //    ICatchCameraProperty
//    ICatchCamFeatureID
//
    public static synchronized RemoteFileHelper2 getInstance() {
        if (instance == null) {
            instance = new RemoteFileHelper2();
        }
        return instance;
    }

    public void initSupportCapabilities() {
        MyCamera camera = CameraManager.getInstance().getCurCamera();
        CameraProperties cameraProperties = null;
        if (camera != null) {
            cameraProperties = camera.getCameraProperties();
        }
        if (cameraProperties != null
                && cameraProperties.hasFuction(PropertyId.CAMERA_PB_LIMIT_NUMBER)
                && cameraProperties.checkCameraCapabilities(ICatchCamFeatureID.ICH_CAM_NEW_PAGINATION_GET_FILE)) {
            supportSegmentedLoading = true;
            supportSetFileListAttribute = true;
        } else {
            supportSegmentedLoading = false;
            supportSetFileListAttribute = false;
        }

        if(cameraProperties != null){
            sensorsNum = cameraProperties.getNumberOfSensors();
        }
    }

    public int getSensorsNum() {
        return sensorsNum;
    }

    public boolean isSupportSegmentedLoading() {
        return supportSegmentedLoading;
    }

    public boolean isSupportSetFileListAttribute() {
        return supportSetFileListAttribute;
    }

    public List<MultiPbItemInfo> getRemoteFile(FileOperation fileOperation, int fileType) {
        int icatchFileType = ICatchFileType.ICH_FILE_TYPE_IMAGE;
        if (fileType == FileType.FILE_TYPE_IMAGE) {
            icatchFileType = ICatchFileType.ICH_FILE_TYPE_IMAGE;
        } else if (fileType == FileType.FILE_TYPE_VIDEO) {
            icatchFileType = ICatchFileType.ICH_FILE_TYPE_VIDEO;
        } else if (fileType == FileType.FILE_TYPE_EMERGENCY_VIDEO) {
            icatchFileType = ICatchFileType.ICH_FILE_TYPE_VIDEO;
        }
        MyCamera camera = CameraManager.getInstance().getCurCamera();
        CameraProperties cameraProperties = null;
        List<MultiPbItemInfo> tempItemInfos;
        if (camera != null) {
            cameraProperties = camera.getCameraProperties();
        }
        if(cameraProperties != null && cameraProperties.hasFuction(PropertyId.CAMERA_PB_LIMIT_NUMBER))
        {
            tempItemInfos = getFileList(fileOperation,icatchFileType,500);
        }else {
            setFileListAttribute(fileOperation, fileType);
            List<ICatchFile> fileList= fileOperation.getFileList(icatchFileType);
            tempItemInfos = getList(fileList, fileFilter);
        }
//        setFileListAttribute(fileOperation, fileType);
//        List<ICatchFile> fileList= fileOperation.getFileList(icatchFileType);
//        tempItemInfos = getList(fileList, fileFilter);
        return tempItemInfos;
    }

    public MultiPbFileResult getRemoteFile(FileOperation fileOperation, int fileType, int fileTotalNum, int startIndex) {
        AppLog.d(TAG, "getRemoteFile fileType:" + fileType + " fileTotalNum:" + fileTotalNum + " startIndex:" + startIndex + " maxNum:" + MAX_NUM);
        if (startIndex > fileTotalNum) {
            MultiPbFileResult result = new MultiPbFileResult();
            result.setLastIndex(startIndex);
            result.setMore(false);
            result.setFileList(null);
            return result;
        }
        int endIndex = MAX_NUM + startIndex - 1;
        if (endIndex >= fileTotalNum) {
            endIndex = fileTotalNum;
        }
        setFileListAttribute(fileOperation, fileType);

        List<ICatchFile> fileList = fileOperation.getFileList(ICatchFileType.ICH_FILE_TYPE_ALL, startIndex, endIndex);
//        List<ICatchFile> fileList = fileOperation.getFileList(icatchFileType, startIndex, endIndex);
        if (fileFilter == null || fileFilter.getTimeFilterType() == FileFilter.TIME_TYPE_ALL_TIME) {
            List<MultiPbItemInfo> pbItemInfos = getList(fileList, null);
            int lastIndex = endIndex + 1;
            boolean isMore = lastIndex < fileTotalNum;
            MultiPbFileResult result = new MultiPbFileResult();
            result.setFileList(pbItemInfos);
            result.setLastIndex(lastIndex);
            result.setMore(isMore);
            AppLog.d(TAG, "End getRemoteFile filelist size:" + pbItemInfos.size());

            return result;
        } else {
            List<MultiPbItemInfo> tempItemInfos = getList(fileList, fileFilter);
            List<MultiPbItemInfo> pbItemInfos = new LinkedList<>();
            if (tempItemInfos != null && tempItemInfos.size() > 0) {
                pbItemInfos.addAll(tempItemInfos);
            }
            int lastIndex = endIndex + 1;
            boolean isMore;
            while (pbItemInfos.size() < MAX_NUM && lastIndex < fileTotalNum) {
                if (fileList != null && fileList.size() > 0 && fileFilter.isLess(fileList.get(fileList.size() - 1))) {
                    break;
                }
                endIndex = MAX_NUM + lastIndex - 1;
                if (endIndex >= fileTotalNum) {
                    endIndex = fileTotalNum;
                }
                fileList = fileOperation.getFileList(ICatchFileType.ICH_FILE_TYPE_ALL, lastIndex, endIndex);
                tempItemInfos = getList(fileList, fileFilter);
                if (tempItemInfos != null && tempItemInfos.size() > 0) {
                    pbItemInfos.addAll(tempItemInfos);
                }
                lastIndex = endIndex + 1;

            }
            if (fileList != null && fileList.size() > 0 && fileFilter.isLess(fileList.get(fileList.size() - 1))) {
                isMore = false;
            } else {
                isMore = lastIndex < fileTotalNum;
            }
            MultiPbFileResult result = new MultiPbFileResult();
            result.setFileList(pbItemInfos);
            result.setLastIndex(lastIndex);
            result.setMore(isMore);
            AppLog.d(TAG, "End getRemoteFile and fileFilter filelist size:" + pbItemInfos.size());
            return result;
        }
    }

    public List<MultiPbItemInfo> getFileList(FileOperation fileOperation,int type, int maxNum) {
        AppLog.i(TAG, "begin getFileList type: " + type + " maxNum：" + maxNum);
        if(fileOperation == null){
            AppLog.i(TAG, "cameraPlayback is null");
            return null;
        }
        int startIndex = 0;
        int endIndex;
        int fileCount = -1;
        List<ICatchFile> photoList = new LinkedList<>();
        List<ICatchFile> videoList = new LinkedList<>();

        fileCount = fileOperation.getFileCount();
        if(fileCount <=0){
            return null;
        }
        if(fileCount  < maxNum){
            startIndex = 1 ;
            endIndex = fileCount;
        }else {
            startIndex = 1;
            endIndex = maxNum;
        }
        while (fileCount >= startIndex){
            AppLog.i(TAG, "start getFileList startIndex=" + startIndex + " endIndex=" + endIndex);
            try {
                List<ICatchFile> templist = fileOperation.getFileList(ICatchFileType.ICH_FILE_TYPE_ALL,startIndex, endIndex);//timeout 20s
                if(templist != null) {
                    AppLog.i(TAG, "end getFileList tempList =" + templist.size());
                }
                if(templist != null && templist.size() > 0){
                    for (ICatchFile file: templist
                    ) {
                        AppLog.i(TAG, "getFileList fileInfo[" + file.toString() + "]");
                        if(file != null && file.getFileType() == ICatchFileType.ICH_FILE_TYPE_VIDEO){
                            videoList.add(file);
                        }else if(file != null && file.getFileType() == ICatchFileType.ICH_FILE_TYPE_IMAGE){
                            photoList.add(file);
                        }
                    }
//				 	list.addAll(templist);
                }

                AppLog.i(TAG, "end getFileList photoList size=" + photoList.size());
                AppLog.i(TAG, "end getFileList videoList size=" + videoList.size());
            } catch (Exception e) {
                AppLog.e(TAG, "Exception e:" + e.getClass().getSimpleName());
                e.printStackTrace();
            }

            startIndex = endIndex + 1;
            if(endIndex + maxNum > fileCount){
                endIndex =fileCount;
            }else {
                endIndex = endIndex + maxNum;
            }
            AppLog.i(TAG, "end getFileList startIndex=" + startIndex + " endIndex=" + endIndex);
        }

        List<MultiPbItemInfo> photoInfoList = getList(photoList,fileFilter);
        List<MultiPbItemInfo> videoInfoList = getList(videoList,fileFilter);
//        GlobalInfo.getInstance().photoInfoList = photoInfoList;
//        GlobalInfo.getInstance().videoInfoList = videoInfoList;
        setLocalFileList(photoInfoList,FileType.FILE_TYPE_IMAGE);
        setLocalFileList(videoInfoList,FileType.FILE_TYPE_VIDEO);
        if(type == ICatchFileType.ICH_FILE_TYPE_VIDEO) {
            return videoInfoList;
        }else if(type == ICatchFileType.ICH_FILE_TYPE_IMAGE){
            return photoInfoList;
        }else {
            return null;
        }
    }

    private List<MultiPbItemInfo> getList(List<ICatchFile> fileList, FileFilter fileFilter) {
        List<MultiPbItemInfo> multiPbItemInfoList = new LinkedList<>();
        if (fileList == null) {
            return multiPbItemInfoList;
        }
        String fileDate;
        String fileSize;
        String fileTime;
        String fileDuration;
        boolean isPanorama;
        for (int ii = 0; ii < fileList.size(); ii++) {
            ICatchFile iCatchFile = fileList.get(ii);
            fileDate = ConvertTools.getTimeByfileDate(iCatchFile.getFileDate());
            fileSize = ConvertTools.ByteConversionGBMBKB(iCatchFile.getFileSize());;
            fileTime = ConvertTools.getDateTimeString(iCatchFile.getFileDate());
            fileDuration = ConvertTools.millisecondsToMinuteOrHours(iCatchFile.getFileDuration());
            isPanorama = PanoramaTools.isPanorama(iCatchFile.getFileWidth(), iCatchFile.getFileHeight());
            if (fileFilter != null) {
                if (fileFilter.isMatch(iCatchFile)) {
                    MultiPbItemInfo mGridItem = new MultiPbItemInfo(iCatchFile, 0,isPanorama ,fileSize,fileTime,fileDate,fileDuration);
                    multiPbItemInfoList.add(mGridItem);
                }
            } else {
                MultiPbItemInfo mGridItem = new MultiPbItemInfo(iCatchFile, 0, isPanorama,fileSize,fileTime,fileDate,fileDuration);
                multiPbItemInfoList.add(mGridItem);
            }
        }
        return multiPbItemInfoList;
    }


    public void setFileListAttribute(FileOperation fileOperation, int fileType) {
        if (!supportSetFileListAttribute) {
            return;
        }
        int filterFileType = ICatchCamListFileFilter.ICH_OFC_TYPE_IMAGE;
        if (fileType == FileType.FILE_TYPE_IMAGE) {
            filterFileType = ICatchCamListFileFilter.ICH_OFC_TYPE_IMAGE;
        } else if (fileType == FileType.FILE_TYPE_VIDEO) {
            filterFileType = ICatchCamListFileFilter.ICH_OFC_TYPE_VIDEO;
        } else if (fileType == FileType.FILE_TYPE_EMERGENCY_VIDEO) {
            filterFileType = ICatchCamListFileFilter.ICH_OFC_TYPE_EMERGENCY_VIDEO;
        }
//        if (curFilterFileType != filterFileType) {
//            fileOperation.setFileListAttribute(filterFileType);
//            curFilterFileType = filterFileType;
//        } else {
//            AppLog.d(TAG, "Current is already fileType:" + fileType);
//        }
        if (fileFilter != null) {
            fileOperation.setFileListAttribute(filterFileType, fileFilter.getSensorType());
        } else {
            fileOperation.setFileListAttribute(filterFileType, ICatchCamListFileFilter.ICH_TAKEN_BY_ALL_SENSORS);
        }

    }

    public int getFileCount(FileOperation fileOperation, int fileType) {
        setFileListAttribute(fileOperation, fileType);
        int fileCount = fileOperation.getFileCount();
        AppLog.d(TAG, "fileCount:" + fileCount);
        return fileCount;
    }

    public void setFileFilter(FileFilter fileFilter) {
        this.fileFilter = fileFilter;
    }

    public FileFilter getFileFilter() {
        return fileFilter;
    }

    public void setLocalFileList(List<MultiPbItemInfo> pbItemInfoList, int fileType) {
        if(pbItemInfoList == null){
            return;
        }
        if (listHashMap.containsKey(fileType)) {
            listHashMap.remove(fileType);
        }
        List<MultiPbItemInfo> temp = new LinkedList<>();
        temp.addAll(pbItemInfoList);
        listHashMap.put(fileType, temp);
    }

    public List<MultiPbItemInfo> getLocalFileList(int fileType) {
        return listHashMap.get(fileType);
    }

    public void clearFileList(int fileType) {
        if (listHashMap.containsKey(fileType)) {
            listHashMap.remove(fileType);
        }
    }

    public void remove(MultiPbItemInfo file, int fileType) {
        List<MultiPbItemInfo> multiPbItemInfos = getLocalFileList(fileType);
        if (multiPbItemInfos != null) {
            multiPbItemInfos.remove(file);
        }
    }

    public void clearAllFileList() {
        listHashMap.clear();
    }

    public boolean needFilter() {
        if (fileFilter != null) {
            return true;
        } else {
            return false;
        }
    }

    public boolean needFilterMoreFile(ICatchFile lastFile) {
        if (fileFilter != null) {
            return !fileFilter.isLess(lastFile);
        } else {
            return true;
        }
    }
}