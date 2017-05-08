package com.amoseui.music.dialog;

import android.net.Uri;

public interface PlaylistDialogCallback {

    void onPositiveButtonClicked(Uri uri, boolean isNewPlaylist);
}
