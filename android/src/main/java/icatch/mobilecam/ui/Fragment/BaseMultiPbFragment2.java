package com.icatch.mobilecam.ui.Fragment;

import androidx.fragment.app.Fragment;

import com.icatch.mobilecam.Listener.OnStatusChangedListener;

public abstract class BaseMultiPbFragment2 extends Fragment {
    public abstract void setOperationListener(OnStatusChangedListener modeChangedListener);
    public abstract void changePreviewType(int layoutType);
    public abstract void quitEditMode();
    public abstract void loadPhotoWall();


}
