package com.icatch.mobilecam.ui.Interface;

import androidx.fragment.app.FragmentPagerAdapter;

public interface MultiPbView2 {
    void setViewPageAdapter(FragmentPagerAdapter adapter);
    void setViewPageCurrentItem(int item);
    void setMenuPhotoWallTypeIcon(int iconRes);
    void setViewPagerScanScroll(boolean isCanScroll);
    void setTabLayoutClickable(boolean value);
    int getViewPageIndex();
    void setFilterItemVisibiliy(boolean visibility);
}
