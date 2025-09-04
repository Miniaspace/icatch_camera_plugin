package com.icatch.mobilecam.ui.activity;

import android.os.Bundle;

import com.icatch.mobilecam.R;
// import com.icatchtek.basecomponent.activitymanager.BaseActivity;
import androidx.appcompat.app.AppCompatActivity;

import androidx.annotation.Nullable;

/**
 * Created by sha.liu on 2023/8/10.
 */
public class MobileCamBaseActivity extends AppCompatActivity { // 使用AppCompatActivity替代BaseActivity
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        StatusBarCompat.setStatusBarColor(this, this.getResources().getColor(R.color.primary), false);
    }
}
