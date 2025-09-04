package com.icatch.mobilecam.ui.activity;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.AccelerateInterpolator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.icatch.mobilecam.Log.AppLog;
import com.icatch.mobilecam.Presenter.LocalMultiPbPresenter;
import com.icatch.mobilecam.R;
import com.icatch.mobilecam.ui.Interface.LocalMultiPbView;
import com.icatch.mobilecam.utils.FixedSpeedScroller;

import java.lang.reflect.Field;

public class LocalMultiPbActivity extends MobileCamBaseActivity implements LocalMultiPbView {
    private String TAG = "LocalMultiPbActivity";
    private ViewPager viewPager;//页卡内容V
    private LocalMultiPbPresenter presenter;
    MenuItem menuPhotoWallType;
    TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_multi_pb);
        Toolbar toolbar =  findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        viewPager =  findViewById(R.id.vPager);
        //viewPager.setPageMargin((int) getResources().getDimensionPixelOffset(R.dimen.space_10));

        tabLayout =  findViewById(R.id.tabs);
        presenter = new LocalMultiPbPresenter(this);
        presenter.setView(this);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                presenter.updateViewpagerStatus(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }
        });

        presenter.loadViewPager();
        tabLayout.setupWithViewPager(viewPager);

        //修改viewPager切换滑动速度 JIRA ICOM-3509 20160721
        try {
            Field field = ViewPager.class.getDeclaredField("mScroller");
            field.setAccessible(true);
            FixedSpeedScroller scroller = new FixedSpeedScroller(viewPager.getContext(),
                    new AccelerateInterpolator());
            field.set(viewPager, scroller);
            scroller.setmDuration(280);
        } catch (Exception e) {
            AppLog.e(TAG, "FixedSpeedScroller Exception");
        }
//        tabLayout.setTabsFromPagerAdapter();
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.submitAppInfo();
        AppLog.d(TAG, "onResume()");
    }

    @Override
    protected void onStop() {
        super.onStop();
//        presenter.isAppBackground();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.reset();
        presenter.removeActivity();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_local_multi_pb, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_multi_pb_preview_type) {
            menuPhotoWallType = item;
            presenter.changePreviewType();
        } else if (id == android.R.id.home) {
            presenter.reback();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_HOME:
                Log.d("AppStart", "home");
                break;
            case KeyEvent.KEYCODE_BACK:
                Log.d("AppStart", "back");
                presenter.reback();
                break;
            default:
                return super.onKeyDown(keyCode, event);
        }
        return true;
    }

    @Override
    public void setViewPageAdapter(FragmentPagerAdapter adapter) {
        viewPager.setAdapter(adapter);
    }

    @Override
    public void setViewPageCurrentItem(int item) {
        AppLog.d(TAG, "setViewPageCurrentItem item=" + item);
        viewPager.setCurrentItem(item);
    }

    @Override
    public void setMenuPhotoWallTypeIcon(int iconRes) {
        menuPhotoWallType.setIcon(iconRes);
    }

    @Override
    public void setViewPagerScanScroll(boolean isCanScroll) {
//        viewPager.setScanScroll(isCanScroll);
    }

    @Override
    public void setTabLayoutClickable(boolean value) {
        AppLog.d(TAG, "setTabLayoutClickable value=" + value);
        tabLayout.setClickable(value);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            tabLayout.setContextClickable(value);
        }
        tabLayout.setFocusable(value);
        tabLayout.setLongClickable(value);
        tabLayout.setEnabled(value);
    }

}
