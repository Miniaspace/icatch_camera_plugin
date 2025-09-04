package com.icatch.mobilecam.utils.fileutils;

import android.app.Activity;
import android.app.RecoverableSecurityException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.IntentSender;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.icatch.mobilecam.utils.fileutils.FileUtil;
import com.icatch.mobilecam.Log.AppLog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

/**
 * Description TODO
 * Author b.jiang
 * Date 2022/8/9 17:20
 */
public class StorageManage {
    private static final String TAG = StorageManage.class.getSimpleName();

    public static String getLogPath(Context context, String folder) {
        String path;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            path = context.getExternalCacheDir().toString() + folder;
        } else {
            path = Environment.getExternalStorageDirectory().toString() + folder;
        }
        return path;
    }

    public static String getCachePath(Context context, String folder) {
        String path = context.getExternalCacheDir().toString() + folder;
        return path;
    }



    public static boolean isExist(Context context, String directory, String fileName) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            String displayName = MediaStore.MediaColumns.DISPLAY_NAME;
            String data = MediaStore.MediaColumns._ID;
            Uri contentUri = null;
            if (fileName.endsWith(".mp4")) {
                contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            } else if (fileName.endsWith(".jpg") || fileName.endsWith(".png") || fileName.endsWith(".bmp")) {
                contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            } else if (fileName.endsWith(".aac")) {
                contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            }

            if (displayName == null) {
                return false;
            }

            String imagePath = Environment.getExternalStorageDirectory().getAbsolutePath() + directory;
            //selection: 指定查询条件
            String selection = MediaStore.Images.Media.DATA + " like ?";
            //设定查询目录,
            //定义selectionArgs：
            String[] selectionArgs = {imagePath + "%"};
            Cursor cursor = context.getContentResolver().query(contentUri, null, selection, selectionArgs, null);
            while (cursor.moveToNext()) {
                try {
                    //取出路径
                    String path = cursor.getString(cursor.getColumnIndex(data));
                    String name = cursor.getString(cursor.getColumnIndex(displayName));
                    AppLog.d(TAG, "getImagePath name:" + name);
                    AppLog.d(TAG, "getImagePath path:" + path);
                    if (path.contains(fileName) || name.contains(fileName)) {
                        return true;
                    }
                } catch (Exception e) {
                    Log.d("test", e.getLocalizedMessage());
                }
            }
            return false;

        } else {
            String path = Environment.getExternalStorageDirectory().toString() + directory;
            File file = new File(path + fileName);
            return file.exists();
        }
    }


    /**
     * 获取外部存储已存在文件的Uri
     * @param context
     * @param filePath
     * @param fileName
     * @return
     */
    public static Uri getExternalExistMediaFileUri(Context context, String filePath, String fileName) {
        Uri externalUri = null;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (!filePath.startsWith(File.separator)) {
                filePath = File.separator + filePath;
            }
            if (!filePath.endsWith(File.separator)) {
                filePath = filePath + File.separator;
            }
            String externalFile = Environment.getExternalStorageDirectory().toString() + filePath;
            File destFile = new File(externalFile + fileName);
            if(destFile.exists()){
                externalUri = Uri.fromFile(destFile);
            }

        } else {
            String displayName = MediaStore.MediaColumns.DISPLAY_NAME;
            String data = MediaStore.MediaColumns._ID;
            Uri contentUri = null;

            if (fileName.endsWith(".mp4") || fileName.endsWith(".mov")) {
                contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            } else if (fileName.endsWith(".jpg") || fileName.endsWith(".png") || fileName.endsWith(".bmp")) {
                contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            } else if (fileName.endsWith(".aac")) {
                contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            }else {
                contentUri = MediaStore.Downloads.EXTERNAL_CONTENT_URI;
            }


            String imagePath = Environment.getExternalStorageDirectory().getAbsolutePath() + filePath;
            //selection: 指定查询条件
            String selection = MediaStore.MediaColumns.DATA + " like ?";
            //设定查询目录,
            //定义selectionArgs：
            String[] selectionArgs = {imagePath + "%"};
            Cursor cursor = context.getContentResolver().query(contentUri, null, selection, selectionArgs, null);
            while (cursor.moveToNext()) {
                try {
                    //取出路径
                    String path = cursor.getString(cursor.getColumnIndex(data));
                    String name = cursor.getString(cursor.getColumnIndex(displayName));
                    AppLog.d(TAG, "getImagePath name:" + name);
                    AppLog.d(TAG, "getImagePath path:" + path);
                    if (path.contains(fileName) || name.contains(fileName)) {
                        int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
                        AppLog.d(TAG, "getImagePath id:" + id);
                        return Uri.parse(MediaStore.Images.Media.EXTERNAL_CONTENT_URI + "/" + id);
                    }
                } catch (Exception e) {
                    Log.d("test", e.getLocalizedMessage());
                }
            }
        }

        return externalUri;
    }

    public static Uri getExternalNewMediaFileUri(Context context, String filePath, String fileName) {
        Uri externalUri = null;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (!filePath.startsWith(File.separator)) {
                filePath = File.separator + filePath;
            }
            if (!filePath.endsWith(File.separator)) {
                filePath = filePath + File.separator;
            }
            String externalFile = Environment.getExternalStorageDirectory().toString() + filePath;
            File destFile = new File(externalFile + fileName);
            com.icatch.mobilecam.utils.fileutils.FileUtil.createOrExistsFile(destFile);
            externalUri = Uri.fromFile(destFile);
        } else {
            ContentResolver resolver = context.getContentResolver();
            ContentValues values = new ContentValues();
            String relativePath;
            if (filePath.startsWith("/")) {
                relativePath = filePath.substring(1);
            } else {
                relativePath = filePath;
            }
            Uri contentUri = null;
            if (fileName.endsWith(".mp4")) {
                contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            } else if (fileName.endsWith(".jpg") || fileName.endsWith(".png") || fileName.endsWith(".bmp")) {
                contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            } else if (fileName.endsWith(".aac")) {
                contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            } else {
                contentUri = MediaStore.Downloads.EXTERNAL_CONTENT_URI;
            }
            AppLog.d(TAG, "getExternalMediaFileUri fileName:" + fileName);
            AppLog.d(TAG, "getExternalMediaFileUri relativePath:" + relativePath);
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath);//保存路径
            Uri uri = contentUri;
            externalUri = resolver.insert(uri, values);
        }

        return externalUri;
    }


    /**
     * 拷贝沙盒中的文件到外部存储区域
     *
     * @param filePath    沙盒文件路径
     * @param externalUri 外部存储文件的 uri
     */
    public static boolean copySandFileToExternalUri(Context context, String filePath, Uri externalUri) {
        ContentResolver contentResolver = context.getContentResolver();
        InputStream inputStream = null;
        OutputStream outputStream = null;
        boolean ret = false;
        try {
            outputStream = contentResolver.openOutputStream(externalUri);
            File sandFile = new File(filePath);
            if (sandFile.exists()) {
                inputStream = new FileInputStream(sandFile);

                int readCount = 0;
                byte[] buffer = new byte[1024];
                while ((readCount = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, readCount);
                    outputStream.flush();
                }
            }
            ret = true;
        } catch (Exception e) {
            Log.e(TAG, "copy SandFile To ExternalUri. e = " + e.toString());
            ret = false;
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
                Log.d(TAG, " input stream and output stream close successful.");
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, " input stream and output stream close fail. e = " + e.toString());
            }
            return ret;
        }

//        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){
//            if(context instanceof Activity) {
//                context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, externalUri));
//            }
//        }
    }

    public static void saveFile(Context context, String name, Bitmap bitmap) {
// 拿到 MediaStore.Images 表的uri
        OutputStream os = null;
        String[] temp = name.split(".");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Uri tableUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            // 创建图片索引
            ContentValues value = new ContentValues();
            value.put(MediaStore.Images.Media.DISPLAY_NAME, name);
//            value.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            value.put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/BpSCTest/Image");
            value.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis());
            // 将该索引信息插入数据表，获得图片的Uri
            Uri imageUri = context.getContentResolver().insert(tableUri, value);
            AppLog.d(TAG, "save path:" + imageUri.getPath());
            try {
                os = context.getContentResolver().openOutputStream(imageUri);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            String path = Environment.getExternalStorageDirectory().toString() + "/DCIM/BpSCTest/";
            File file = new File(path + name + ".jpg");
            FileUtil.createOrExistsFile(file);
            try {
                os = new FileOutputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

//        OutputStream os = null;
//        String path = Environment.getExternalStorageDirectory().toString() + "/DCIM/BpSCTest/";
//        File file = new File(path + name + ".jpg");
//        FileUtil.createOrExistsFile(file);
//        try {
//            os = new FileOutputStream(file);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }

        if (os != null) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
        }
    }

    public static void saveBitmap(Context context, Uri uri, Bitmap bitmap) {
        OutputStream os = null;
        try {
            os = context.getContentResolver().openOutputStream(uri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (os != null) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
        }
    }

    public static void getImagePath(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
//        Uri uri = MediaStore.Files.getContentUri(Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/BpSCTest");
//        Cursor cursor = contentResolver.query(uri, null, null, null, null);

        String imagePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/BpSCTest/Image";
//selection: 指定查询条件
        String selection = MediaStore.Images.Media.DATA + " like ?";
//设定查询目录,
//定义selectionArgs：
        String[] selectionArgs = {imagePath + "%"};
        Cursor cursor = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, selection, selectionArgs, null);
        while (cursor.moveToNext()) {

            try {
                //取出路径
                String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA));
                String name = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    name = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DISPLAY_NAME));
                    AppLog.d(TAG, "getImagePath name:" + name);
                }
                String relative_path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.RELATIVE_PATH));
                AppLog.d(TAG, "getImagePath path:" + path);
                AppLog.d(TAG, "getImagePath relative_path:" + relative_path);
                int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
                AppLog.d(TAG, "getImagePath id:" + id);

                Bitmap bitmap = BitmapFactory.decodeFile(path);
            } catch (Exception e) {
                Log.d("test", e.getLocalizedMessage());
            }
//            break;
        }
    }

    public static Uri getFirstImagePath(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
//        Uri uri = MediaStore.Files.getContentUri(Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/BpSCTest");
//        Cursor cursor = contentResolver.query(uri, null, null, null, null);

        String imagePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/BpSCTest/Image";

//selection: 指定查询条件
        String selection = MediaStore.Images.Media.DATA + " like ?";
//设定查询目录,
//定义selectionArgs：
        String[] selectionArgs = {imagePath + "%"};
        Cursor cursor = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, selection, selectionArgs, null);
        while (cursor.moveToNext()) {

            try {
                //取出路径
                String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA));
                String name = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    name = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DISPLAY_NAME));
                    AppLog.d(TAG, "getImagePath name:" + name);
                }
                String relativePath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.RELATIVE_PATH));
                AppLog.d(TAG, "getImagePath path:" + path);
                AppLog.d(TAG, "getImagePath relativePath:" + relativePath);
                int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
                AppLog.d(TAG, "getImagePath id:" + id);
                // 获取文件名
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.TITLE));
                AppLog.d(TAG, "getImagePath title:" + title);
                return Uri.parse(MediaStore.Images.Media.EXTERNAL_CONTENT_URI + "/" + id);
            } catch (Exception e) {
                Log.d("test", e.getLocalizedMessage());
            }
//            break;
        }
        return null;
    }


    public static void deleteFile(Context context, Uri uri) {
        try {
            AppLog.d(TAG, "delete uri:" + uri);

            // 通过 ContentResolver 删除指定的 content uri 的文件
            ContentResolver contentResolver = context.getContentResolver();
            int deleteCount = 0;
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//                deleteCount = contentResolver.delete(uri, null);
//            }

            deleteCount = contentResolver.delete(
                    uri,
                    null,
                    null);
            AppLog.d(TAG, "delete count:" + deleteCount);
        } catch (Exception ex) {
            // 如果你想删除非本 app 创建的文件，就会收到类似这样的异常 android.app.RecoverableSecurityException: com.webabcd.androiddemo has no access to content://media/external/images/media/102
            AppLog.e(TAG, "delete error:" + ex.toString());
            //捕获 RecoverableSecurityException异常，发起请求
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (ex instanceof RecoverableSecurityException) {
                    RecoverableSecurityException recoverableSecurityException = (RecoverableSecurityException) ex;
                    IntentSender intentSender = recoverableSecurityException.getUserAction().getActionIntent().getIntentSender();
                    if (intentSender != null) {
                        AppLog.e(TAG, "delete startIntentSender:");
                        try {
//                            context.startIntentSender(intentSender, null, 0, 0, 0);
                            ((Activity) context).startIntentSenderForResult(intentSender,
                                    100, null, 0, 0, 0);
                        } catch (IntentSender.SendIntentException sendIntentException) {
                            sendIntentException.printStackTrace();
                        }
                    }
                }

            }


//            PendingIntent deleteRequest = null;
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//                ArrayList<Uri> uris = new ArrayList<>();
//                uris.add(uri);
//                deleteRequest = MediaStore.createDeleteRequest(context.getContentResolver(), uris);
//                try {
//                    ((Activity)context).startIntentSenderForResult(deleteRequest.getIntentSender(), 100, null, 0, 0, 0, null);
//                } catch (IntentSender.SendIntentException e) {
//                    AppLog.e(TAG, "delete error:" + e.toString());
//                    e.printStackTrace();
//                }
//            }


        }
    }
//    public static void delete(Uri ){
////这里的imgUri是使用上述代码获取的
//        val queryUri = imgUri
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
//            try {
//                val projection = arrayOf(MediaStore.Images.Media.DATA)
//                val cursor = contentResolver.query(queryUri, projection,
//                        null, null, null)
//                cursor?.let{
//                    val columnIndex = it.getColumnIndex(MediaStore.Images.Media.DATA)
//                    if (columnIndex > -1) {
//                        val file = File(it.getString(columnIndex))
//                        file.delete()
//                    }
//                }
//                cursor?.close()
//            } catch (e: IOException) {
//                Log.e(TAG, "delete failed :${e.message}")
//            }
//        } else {
//            try {
//                contentResolver.delete(queryUri, null, null)
//            } catch (e: IOException) {
//                Log.e(TAG, "delete failed :${e.message}")
//            } catch (e1: RecoverableSecurityException) {
//                //捕获 RecoverableSecurityException异常，发起请求
//                try {
//                    startIntentSenderForResult(e1.userAction.actionIntent.intentSender,
//                            REQUEST_CODE, null, 0, 0, 0)
//                } catch (e2: IntentSender.SendIntentException) {
//                    e2.printStackTrace()
//                }
//            }
//        }
//    }

    // 通过 MediaStore 在图片目录新建一个图片文件
    private void createFile(Context context, Bitmap bitmap) {

        // 需要创建的图片对象
//        BitmapDrawable bitmapDrawable = (BitmapDrawable) getResources().getDrawable(R.drawable.te, null);
//        Bitmap bitmap = bitmapDrawable.getBitmap();

        ContentValues contentValues = new ContentValues();
        // 指定文件保存的文件夹名称
        // Environment.DIRECTORY_PICTURES 值为 Pictures
        // Environment.DIRECTORY_DCIM 值为 DCIM
        // Environment.DIRECTORY_MOVIES 值为 Movies
        // Environment.DIRECTORY_MUSIC 值为 Music
        // Environment.DIRECTORY_DOWNLOADS 值为 Download
        // 如果想获取上述文件夹的真实地址可以通过这样的方式 Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath() 获取，他返回的值类似 /storage/emulated/0/Pictures
        contentValues.put(MediaStore.Images.ImageColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/wanglei_test/");
        // 指定文件名
        contentValues.put(MediaStore.Images.ImageColumns.DISPLAY_NAME, "son");
        // 指定文件的 mime（比如 image/jpeg, application/vnd.android.package-archive 之类的）
        contentValues.put(MediaStore.Images.ImageColumns.MIME_TYPE, "image/jpeg");
        contentValues.put(MediaStore.Images.ImageColumns.WIDTH, bitmap.getWidth());
        contentValues.put(MediaStore.Images.ImageColumns.HEIGHT, bitmap.getHeight());

        ContentResolver contentResolver = context.getContentResolver();
        // 通过 ContentResolver 在指定的公共目录下按照指定的 ContentValues 创建文件，会返回文件的 content uri（类似这样的地址 content://media/external/images/media/102）
        // 可以通过 MediaStore 保存文件的公共目录有：
        // MediaStore.Images.Media.EXTERNAL_CONTENT_URI - 图片目录，只能保存 mime 为图片类型的文件
        //     图片目录包括 Environment.DIRECTORY_PICTURES, Environment.DIRECTORY_DCIM 文件夹
        // MediaStore.Audio.Media.EXTERNAL_CONTENT_URI - 音频目录，只能保存 mime 为音频类型的文件
        //     音频目录包括 Environment.DIRECTORY_MUSIC, Environment.DIRECTORY_RINGTONES 等等文件夹
        // MediaStore.Video.Media.EXTERNAL_CONTENT_URI - 视频目录，只能保存 mime 为视频类型的文件
        //     视频目录包括 DIRECTORY_MOVIES, Environment.DIRECTORY_PICTURES, Environment.DIRECTORY_DCIM 文件夹
        // MediaStore.Downloads.EXTERNAL_CONTENT_URI - 下载目录，可以保存任意类型的文件
        //     下载目录包括 Environment.DIRECTORY_DOWNLOADS 文件夹
        Uri uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        if (uri == null) {
            Log.d(TAG, "创建文件失败");
        } else {
            Log.d(TAG, "创建文件成功：" + uri.toString());
        }

        // 写入图片数据
        OutputStream outputStream = null;
        try {
            outputStream = contentResolver.openOutputStream(uri);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream);
        } catch (Exception ex) {
            Log.d(TAG, "写入数据失败：" + ex.toString());
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.flush();
                    outputStream.close();
                }
            } catch (Exception ex) {

            }
        }
    }

    // 通过 MediaStore 在图片目录遍历图片文件
    // 注：
    // 1、如果你想遍历出非本 app 创建的文件，则需要先申请 READ_EXTERNAL_STORAGE 权限
    // 2、如果你的 app 卸载后再重装的话系统不会认为是同一个 app（也就是你卸载之前创建的文件，再次安装 app 后必须先申请 READ_EXTERNAL_STORAGE 权限后才能获取到）
    public static int queryFileFirst(Context context) {

        int contentId = -1;

        // 通过 ContentResolver 遍历指定公共目录中的文件
        ContentResolver contentResolver = context.getContentResolver();
        // 第 2 个参数（projection）：指定需要返回的字段，null 会返回所有字段
        // 第 3 个参数（selection）：查询的 where 语句，类似 xxx = ?
        // 第 4 个参数（selectionArgs）：查询的 where 语句的值，类似 new String[] { xxx }
        // 第 5 个参数（selectionArgs）：排序语句，类似 xxx DESC
        Cursor cursor = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, null);

        AppLog.d(TAG, "count:" + cursor.getCount());
        while (cursor.moveToNext()) {
            // 比如这个地址 content://media/external/images/media/102 它的后面的 102 就是它的 id
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            // 获取文件名
            String title = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.TITLE));
            // 获取文件的真实路径，类似 /storage/emulated/0/Pictures/wanglei_test/son.jpg
            String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));

            AppLog.d(TAG, String.format("id:%d, title:%s, path:%s", id, title, path));
            contentId = id;
        }
        cursor.close();

        // 返回指定的公共目录中的第一个文件的 id
        return contentId;
    }

    // 通过 MediaStore 读取图片目录中的图片，并显示
    public static void showFile(Context context) {
        // 先拿到需要显示的图片的 content uri（类似这样的地址 content://media/external/images/media/102）
        int contentId = queryFileFirst(context);
        Uri contentUri = Uri.parse(MediaStore.Images.Media.EXTERNAL_CONTENT_URI + "/" + contentId);
        Log.d(TAG, "show uri:" + contentUri);

        Cursor cursor = null;
        try {
            String[] projection = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, projection, null, null, null);
            cursor.moveToFirst();
            // 拿到指定的 content uri 的真实路径
            String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
            Log.d(TAG, String.format(Locale.ENGLISH, "contentUri:%s, path:%s", contentUri, path));

            // 显示指定路径的图片
            Bitmap bitmap = BitmapFactory.decodeFile(path);
//            _imageView1.setImageBitmap(bitmap);
        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    // 通过 MediaStore 删除图片目录中的图片文件（只能删除本 app 创建的文件）
    // 注：如果你的 app 卸载后再重装的话系统不会认为是同一个 app（也就是你卸载之前创建的文件，再次安装 app 后是无法通过这种方式删除它的）
    public static void deleteFileFirst(Context context) {

        try {
            // 先拿到需要删除的图片的 content uri（类似这样的地址 content://media/external/images/media/102）
            int contentId = queryFileFirst(context);
            Uri contentUri = Uri.parse(MediaStore.Images.Media.EXTERNAL_CONTENT_URI + "/" + contentId);
            Log.d(TAG, "delete uri:" + contentUri);

            // 通过 ContentResolver 删除指定的 content uri 的文件
            ContentResolver contentResolver = context.getContentResolver();
            int deleteCount = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                deleteCount = contentResolver.delete(contentUri, null);
            }
            Log.d(TAG, "delete count:" + deleteCount);
        } catch (Exception ex) {
            // 如果你想删除非本 app 创建的文件，就会收到类似这样的异常 android.app.RecoverableSecurityException: com.webabcd.androiddemo has no access to content://media/external/images/media/102
            Log.e(TAG, "delete error:" + ex.toString());
        }
    }
}
