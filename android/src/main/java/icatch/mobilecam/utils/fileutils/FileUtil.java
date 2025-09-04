package com.icatch.mobilecam.utils.fileutils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;

import androidx.annotation.RequiresApi;

import com.icatch.mobilecam.Log.AppLog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

/**
 * Description TODO
 * Author b.jiang
 * Date 2023/5/19 17:53
 */
public class FileUtil {
    private static final String TAG = FileUtil.class.getSimpleName();
    //复制沙盒私有文件到Download公共目录下
    //orgFilePath是要复制的文件私有目录路径
    //displayName复制后文件要显示的文件名称带后缀（如xx.txt）

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static Uri copyPrivateToDCIM(Context context, String orgFilePath, String desFilePath, String fileName) {
        return copyPrivateToDCIM(context, orgFilePath, desFilePath, fileName, false);
    }
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static Uri copyPrivateToDCIM(Context context, String orgFilePath, String desFilePath, String fileName, boolean deleteOrgFile){
        if (context == null) {
            return null;
        }

        AppLog.d(TAG,"copyPrivateToDCIM orgFilePath:" + orgFilePath + " desFilePath:" + desFilePath + " fileName:" + fileName);
        if (desFilePath.startsWith("/")) {
            desFilePath = desFilePath.substring(1);
        }
        ContentValues values = new ContentValues();
        //values.put(MediaStore.Images.Media.DESCRIPTION, "This is a file");
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
        String mimeType = "image/*";
        String destMediatype = "video"; //default
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        if (fileName.endsWith("jpg") || fileName.endsWith("JPG")) {
            mimeType = "image/jpeg";
            uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            destMediatype = "image";
        } else if (fileName.endsWith("png") || fileName.endsWith("PNG")) {
            mimeType = "image/png";
            uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            destMediatype = "image";
        } else if (fileName.endsWith("mp4") || fileName.endsWith("MP4")) {
            mimeType = "video/mp4";
            uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        } else if (fileName.endsWith("ts") || fileName.endsWith("TS")) {
            mimeType = "video/ts";
            uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        } else if (fileName.endsWith("mov") || fileName.endsWith("MOV")) {
            mimeType = "video/mov";
            uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        }
        values.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);//MediaStore对应类型名
        values.put(MediaStore.MediaColumns.TITLE, fileName);
//        values.put(MediaStore.MediaColumns.RELATIVE_PATH, desFilePath);//公共目录下目录名
//        values.put(MediaStore.Video.Media.DATE_ADDED, (int) (System.currentTimeMillis() / 1000)); // 寫入時間
//        values.put(MediaStore.MediaColumns.DATE_TAKEN, System.currentTimeMillis() / 1000);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_DCIM + "/MobileCam/"+destMediatype);
            // video handle maybe delay to process, see it 1 to lock only can access via myself.
            values.put(MediaStore.Video.Media.IS_PENDING, 1);
//            insertUri = context.getContentResolver().insert(uri, values);
        } else {
            values.put(MediaStore.Video.Media.DATA, desFilePath + fileName);
//            insertUri = context.getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
        }
        // values.put(MediaStore.MediaColumns.DATA, desFilePath + fileName);

        ContentResolver resolver = context.getContentResolver();

        Uri insertUri = null;
        InputStream ist= null;
        OutputStream ost = null;
        File orgFile = null;
        try {
            insertUri = resolver.insert(uri, values);//使用ContentResolver创建需要操作的文件
            orgFile = new File(orgFilePath);
            ist = new FileInputStream(orgFile);
            if (insertUri != null) {
                ost = resolver.openOutputStream(insertUri);
            }
            if (ost != null) {
                byte[] buffer = new byte[4096];
                int byteCount = 0;
                while ((byteCount = ist.read(buffer)) != -1) {  // 循环从输入流读取 buffer字节
                    ost.write(buffer, 0, byteCount);        // 将读取的输入流写入到输出流
                }
                // write what you want
            }

            if (Build.VERSION.SDK_INT >= 29) {
                values.clear();
                values.put(MediaStore.Video.Media.IS_PENDING, 0);
                resolver.update(insertUri, values, null, null);
            }

            return insertUri;
        } catch (IOException e) {
            AppLog.e("copyPrivateToDCIM--","fail: " + e.getMessage() + ", cause: " + e.getCause());
        } finally {
            try {
                if (ist != null) {
                    ist.close();
                }
                if (ost != null) {
                    ost.close();
                }
                AppLog.i("copyPrivateToDCIM--","ready for delete orig file"+orgFile);
                if (deleteOrgFile) {
                    if (orgFile != null) {
                        orgFile.delete();
                    }
                }
            } catch (IOException e) {
                AppLog.e("copyPrivateToDCIM--","fail in close: " + e.getMessage() + ", cause: " + e.getCause());
            }
        }
        return insertUri;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static Uri copyFileToCache(Context context, Uri orgFileUri, String desFilePath, String fileName){
        if (context == null) {
            return null;
        }

        AppLog.d(TAG,"copyFileToCache desFilePath:" + desFilePath + " fileName:" + fileName);
        ContentResolver resolver = context.getContentResolver();
        InputStream ist= null;
        OutputStream ost = null;
//        File orgFile = null;
        File desFile = null;
        try {
//            insertUri = resolver.insert(uri, values);//使用ContentResolver创建需要操作的文件
            desFile = new File(desFilePath);
            ost = new FileOutputStream(desFile);
            if (orgFileUri != null) {
                ist = resolver.openInputStream(orgFileUri);
            }
            if (ost != null) {
                byte[] buffer = new byte[4096];
                int byteCount = 0;
                while ((byteCount = ist.read(buffer)) != -1) {  // 循环从输入流读取 buffer字节
                    ost.write(buffer, 0, byteCount);        // 将读取的输入流写入到输出流
                }
                // write what you want
            }
            return orgFileUri;
        } catch (IOException e) {
            AppLog.e("copyPrivateToDCIM--","fail: " + e.getMessage() + ", cause: " + e.getCause());
        } finally {
            try {
                if (ist != null) {
                    ist.close();
                }
                if (ost != null) {
                    ost.close();
                }
            } catch (IOException e) {
                AppLog.e("copyPrivateToDCIM--","fail in close: " + e.getMessage() + ", cause: " + e.getCause());
            }
        }
        return orgFileUri;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static int deleteFile(Context context, LocalFileInfo fileInfo) {
        int ret = -1;
        try {
            ContentResolver resolver = context.getContentResolver();
            ret = resolver.delete(fileInfo.getUri(), null, null);
            File file = new File(context.getExternalFilesDir(null) + "/" + fileInfo.relativePath + fileInfo.name);
            if (file.exists()) {
                file.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
            AppLog.e(TAG, "deleteFile: " + e.getMessage());
        }
        return ret;

    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static int deleteFile(Context context, Uri uri) {
        int ret = -1;
        try {
            ContentResolver resolver = context.getContentResolver();
            ret = resolver.delete(uri, null, null);
        } catch (Exception e) {
            e.printStackTrace();
            AppLog.e(TAG, "deleteFile: " + e.getMessage());
        }
        return ret;

    }


    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static int deleteImageFile(Context context, long id) {
        ContentResolver resolver = context.getContentResolver();
        String selection = MediaStore.MediaColumns._ID + "=?";
        String[] args = new String[]{String.valueOf(id)};
        return resolver.delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection, args);

    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static List<LocalFileInfo> queryVideoFileList(Context context, String relativePath) {
        AppLog.d(TAG,"queryVideoFileList relativePath:" + relativePath);
        if (relativePath.startsWith("/")) {
            relativePath = relativePath.substring(1);
        }
        return queryFileList(context, relativePath, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static List<LocalFileInfo> queryImageFileList(Context context, String relativePath) {
        AppLog.d(TAG,"queryImageFileList relativePath:" + relativePath);
        if (relativePath.startsWith("/")) {
            relativePath = relativePath.substring(1);
        }
        return queryFileList(context, relativePath, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static List<LocalFileInfo> queryFileList(Context context, String filePath, Uri fileType) {
        Uri queryUri = null;
        Uri external = fileType;
        String selection = MediaStore.MediaColumns.RELATIVE_PATH+"=?";
        String[] args = new String[]{filePath};
        String[] projection = new String[]{
                MediaStore.MediaColumns._ID,
                MediaStore.MediaColumns.DISPLAY_NAME,
                MediaStore.MediaColumns.DATE_MODIFIED,
                MediaStore.MediaColumns.SIZE,
                MediaStore.MediaColumns.DURATION,
                MediaStore.MediaColumns.WIDTH,
                MediaStore.MediaColumns.HEIGHT,
                MediaStore.MediaColumns.MIME_TYPE,
                MediaStore.MediaColumns.RELATIVE_PATH,
                MediaStore.MediaColumns.DATA};
        String orderBy = MediaStore.MediaColumns.DATE_ADDED + " desc ";
        Cursor cursor = context.getContentResolver().query(external, projection, selection, args, orderBy);
        List<LocalFileInfo> fileInfoList = new LinkedList<>();
        if (cursor != null && cursor.moveToFirst()) {
            AppLog.d(TAG, "selectSingle 查询成功，cursor getCount: "+ cursor.getCount());
            do {
                LocalFileInfo fileInfo = new LocalFileInfo();

                for(int i = 0; i < cursor.getColumnCount(); i++) {
                    if (i == 0) {
                        fileInfo.id = cursor.getLong(i);
                    } else if (i == 1) {
                        fileInfo.name = cursor.getString(i);
                    } else if (i == 2) {
                        fileInfo.modifyTime = cursor.getLong(i) * 1000;
                    } else if (i == 3) {
                        fileInfo.size = cursor.getLong(i);
                    } else if (i == 4) {
                        fileInfo.duration = cursor.getLong(i);
                    } else if (i == 5) {
                        fileInfo.width = cursor.getInt(i);
                    } else if (i == 6) {
                        fileInfo.height = cursor.getInt(i);
                    } else if (i == 7) {
                        fileInfo.mimeType = cursor.getString(i);
                    } else if (i == 8) {
                        fileInfo.relativePath = cursor.getString(i);
                    } else if (i == 9) {
                        fileInfo.absolutePath = cursor.getString(i);
                    }
                    AppLog.d(TAG, "selectSingle 查询成功[" + cursor.getPosition() + "], " + cursor.getColumnName(i) + ", String： " + cursor.getString(i));
                }
                fileInfoList.add(fileInfo);
            } while (cursor.moveToNext());

            cursor.close();
        }
        return fileInfoList;

//        Cursor cursor = MediaStore.Images.Media.query(context.getContentResolver(), MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
//                new String[]{MediaStore.MediaColumns.DISPLAY_NAME, MediaStore.MediaColumns.DATE_TAKEN, }  ,MediaStore.MediaColumns.RELATIVE_PATH + "="+ filePath, MediaStore.MediaColumns.DATE_TAKEN + " desc ");
//        return cursor;
    }
    
    // 添加createOrExistsFile方法以兼容现有代码
    public static boolean createOrExistsFile(File file) {
        if (file == null) return false;
        if (file.exists()) return file.isFile();
        if (!createOrExistsDir(file.getParentFile())) return false;
        try {
            return file.createNewFile();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private static boolean createOrExistsDir(File file) {
        return file != null && (file.exists() ? file.isDirectory() : file.mkdirs());
    }
}
