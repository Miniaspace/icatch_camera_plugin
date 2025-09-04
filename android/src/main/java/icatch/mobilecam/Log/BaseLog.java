package com.icatch.mobilecam.Log;

import android.content.Context;

// import com.icatchtek.baseutil.log.Logger;
import android.util.Log;

import java.io.File;

/**
 * Description TODO
 * Author b.jiang
 * Date 2023/5/9 11:31
 */
public class BaseLog { // 移除Logger接口实现
    public void initLog(Context context) {

    }

    public void reInitLog() {

    }

    public void i(String s, String s1) {
        AppLog.i(s,s1);
    }

    public void w(String s, String s1) {
        AppLog.w(s,s1);
    }

    public void e(String s, String s1) {
        AppLog.e(s,s1);
    }

    public void d(String s, String s1) {
        AppLog.d(s,s1);
    }

    public String getRelativeLogFileName() {
        File file = AppLog.getWriteLogFile();
        if(file != null){
            return file.getName();
        }
        return null;
    }

    public String getAbsoluteLogFileName() {
        File file = AppLog.getWriteLogFile();
        if(file != null){
            return file.getAbsolutePath();
        }
        return null;
    }

    public void checkLogFileExist() {

    }
}
