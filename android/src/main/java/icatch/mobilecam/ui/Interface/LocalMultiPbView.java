package com.icatch.mobilecam.ui.Interface;

import androidx.fragment.app.FragmentPagerAdapter;

public interface LocalMultiPbView {
    void setViewPageAdapter(FragmentPagerAdapter adapter);

    void setViewPageCurrentItem(int item);

    void setMenuPhotoWallTypeIcon(int iconRes);

    void setViewPagerScanScroll(boolean isCanScroll);

    void setTabLayoutClickable(boolean value);

}
