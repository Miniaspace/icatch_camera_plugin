package com.tinyai.libmediacomponent.components.filelist;

public class FileItemInfo {
    private int fileHandle;
    private int fileType;
    private String filePath;
    private String fileName;
    private long fileSize;
    private long time;
    private String thumbPath;
    private boolean isPanorama;
    
    public FileItemInfo(int fileHandle, int fileType, String filePath, String fileName, long fileSize, long time, String thumbPath) {
        this.fileHandle = fileHandle;
        this.fileType = fileType;
        this.filePath = filePath;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.time = time;
        this.thumbPath = thumbPath;
        this.isPanorama = false;
    }
    
    public FileItemInfo(int fileHandle, int fileType, String filePath, String fileName, long fileSize, long time, String thumbPath, boolean isPanorama) {
        this.fileHandle = fileHandle;
        this.fileType = fileType;
        this.filePath = filePath;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.time = time;
        this.thumbPath = thumbPath;
        this.isPanorama = isPanorama;
    }
    
    // Getters
    public int getFileHandle() {
        return fileHandle;
    }
    
    public int getFileType() {
        return fileType;
    }
    
    public String getFilePath() {
        return filePath;
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public long getFileSize() {
        return fileSize;
    }
    
    public long getTime() {
        return time;
    }
    
    public String getThumbPath() {
        return thumbPath;
    }
    
    public boolean isPanorama() {
        return isPanorama;
    }
    
    // Setters
    public void setFileHandle(int fileHandle) {
        this.fileHandle = fileHandle;
    }
    
    public void setFileType(int fileType) {
        this.fileType = fileType;
    }
    
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
    
    public void setTime(long time) {
        this.time = time;
    }
    
    public void setThumbPath(String thumbPath) {
        this.thumbPath = thumbPath;
    }
    
    public void setPanorama(boolean panorama) {
        this.isPanorama = panorama;
    }
    
    // 添加getUri方法以兼容现有代码
    public android.net.Uri getUri() {
        // 返回基于文件路径的Uri
        if (filePath != null) {
            return android.net.Uri.fromFile(new java.io.File(filePath));
        }
        return null;
    }
    
    // 添加setUri方法以兼容现有代码
    public void setUri(android.net.Uri uri) {
        // 从Uri设置文件路径
        if (uri != null) {
            this.filePath = uri.getPath();
        }
    }
}