package com.icatch.mobilecam.utils.fileutils;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.Serializable;

/**
 * Description TODO
 * Author b.jiang
 * Date 2023/5/19 17:53
 */
public class LocalFileInfo implements Serializable {
    public long id;
    public String name;
    public long modifyTime;
    public long size;
    public long duration;
    public int width;
    public int height;
    public String mimeType;
    public String relativePath;
    public String absolutePath;

    public LocalFileInfo() {
    }

    public Uri getUri() {
        Uri external = null;
        if (mimeType.startsWith("video")) {
            external = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        } else if (mimeType.startsWith("image")) {
            external = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        } else if (mimeType.startsWith("audio")) {
            external = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        } else {
            external = MediaStore.Files.getContentUri("external");
        }
        return ContentUris.withAppendedId(external, id);
    }

}
