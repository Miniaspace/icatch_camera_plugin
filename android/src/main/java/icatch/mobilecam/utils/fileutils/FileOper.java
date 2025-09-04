package com.icatch.mobilecam.utils.fileutils;

import android.content.Context;
import android.os.Environment;

import com.icatch.mobilecam.Log.AppLog;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by yh.zhang C001012 on 2015/10/15:16:25.
 * Fucntion:
 */
public class FileOper {

    private static final String TAG = FileOper.class.getSimpleName();

    public  static  File getAppSpecificAlbumStorageDir(Context context, String albumName) {
        // Get the pictures directory that's inside the app-specific directory on
        // external storage.
        File file = new File(context.getExternalFilesDir(
                Environment.DIRECTORY_DCIM), albumName);
        if (!file.exists()) {
            file.mkdirs();
//            AppLog.e(TAG, "Directory not created");
        }
        AppLog.e(TAG, "getAppSpecificAlbumStorageDir path:" + file.getAbsolutePath());

        return file;
    }


    public static void createDirectory(String directoryPath){
        if (directoryPath != null) {
            File directory = new File(directoryPath);
            if (!directory.exists()) {
                directory.mkdirs();
            }
        }
    }

    public static void createFile(String directoryPath, String fileName) {
        AppLog.i("FileOper","start createFile");
        if (directoryPath != null) {
            File directory = new File(directoryPath);
            if (!directory.exists()) {
                directory.mkdirs();
            }
        }

        File file = new File(directoryPath+fileName);
        AppLog.i("FileOper","directoryPath+fileName ="+directoryPath+fileName);
        if (!file.exists()) {
            AppLog.i("FileOper","file is not exists,need to create!");
            try {
                file.createNewFile();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                AppLog.i("FileOper", "FileNotFoundException");
            } catch (IOException e) {
                AppLog.i("FileOper","IOException");
                e.printStackTrace();
            }

        }
    }
}
