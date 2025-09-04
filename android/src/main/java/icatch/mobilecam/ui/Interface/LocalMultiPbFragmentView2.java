package com.icatch.mobilecam.ui.Interface;
import com.tinyai.libmediacomponent.components.filelist.FileItemInfo;
import com.tinyai.libmediacomponent.components.filelist.OperationMode;

import java.util.List;


/**
 * Created by b.jiang on 2017/5/19.
 */

public interface LocalMultiPbFragmentView2 {
    void setFileListViewVisibility(int visibility);

    void setNoContentTxvVisibility(int visibility);

    void renderList(List<FileItemInfo> list);
    void quitEditMode();

    OperationMode getOperationMode();
}
