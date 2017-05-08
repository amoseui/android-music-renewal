package com.amoseui.music.dialog;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.amoseui.music.R;
import com.amoseui.music.utils.MusicUtils;

public class RenamePlaylistDialogBuilder extends PlaylistDialogBuilder {

    private long mPlaylistId;

    public RenamePlaylistDialogBuilder(Context context, long id) {
        super(context, id);
        mPlaylistId = id;
    }

    @Override
    @Nullable
    protected String getPlaylistName(long id) {
        Cursor c = MusicUtils.query(
                mContext, MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Playlists.NAME},
                MediaStore.Audio.Playlists._ID + "=?",
                new String[]{Long.valueOf(id).toString()},
                MediaStore.Audio.Playlists.NAME);
        String name = null;
        if (c != null) {
            c.moveToFirst();
            if (!c.isAfterLast()) {
                name = c.getString(0);
            }
            c.close();
        }
        return name;
    }

    @Override
    protected String getString() {
        return mContext.getString(R.string.rename_playlist_same_prompt);
    }

    @Override
    protected void setPlaylist(String name) {
        ContentResolver resolver = mContext.getContentResolver();
        ContentValues values = new ContentValues(1);
        values.put(MediaStore.Audio.Playlists.NAME, name);
        resolver.update(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                values,
                MediaStore.Audio.Playlists._ID + "=?",
                new String[]{Long.valueOf(mPlaylistId).toString()});
        Toast.makeText(
                mContext, R.string.playlist_renamed_message, Toast.LENGTH_SHORT)
                .show();
    }
}
