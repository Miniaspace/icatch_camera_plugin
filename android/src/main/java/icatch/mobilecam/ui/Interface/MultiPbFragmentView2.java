package com.icatch.mobilecam.ui.Interface;

import com.tinyai.libmediacomponent.components.filelist.FileItemInfo;
import com.tinyai.libmediacomponent.components.filelist.FileListView;
import com.tinyai.libmediacomponent.components.filelist.OperationMode;

import java.util.List;

public interface MultiPbFragmentView2 {
    void setFileListViewVisibility(int visibility);
    void setNoContentTxvVisibility(int visibility);
    void renderList(List<FileItemInfo> list);
    void setRefreshMode(int refreshMode);
    void quitEditMode();
    OperationMode getOperationMode();

}
