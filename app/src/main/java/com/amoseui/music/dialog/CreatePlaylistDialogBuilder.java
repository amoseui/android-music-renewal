package com.amoseui.music.dialog;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.Nullable;

import com.amoseui.music.R;
import com.amoseui.music.utils.MusicUtils;

public class CreatePlaylistDialogBuilder extends PlaylistDialogBuilder
        implements MusicUtils.Defs {

    private boolean mIsNewPlaylist = true;

    public CreatePlaylistDialogBuilder(Context context) {
        super(context, 0);
    }

    public CreatePlaylistDialogBuilder setNewPlaylist(boolean isNewPlaylist) {
        mIsNewPlaylist = isNewPlaylist;
        return this;
    }

    @Override
    @Nullable
    protected String getPlaylistName(long id) {
        String template =
                mContext.getString(R.string.new_playlist_name_template);
        int num = 1;

        String[] cols = new String[] {
                MediaStore.Audio.Playlists.NAME
        };
        ContentResolver resolver = mContext.getContentResolver();
        String whereClause = MediaStore.Audio.Playlists.NAME + " != ''";
        Cursor c = resolver.query(
                MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                cols, whereClause, null, MediaStore.Audio.Playlists.NAME);
        if (c == null) {
            return null;
        }

        String suggestedName = String.format(template, num++);

        // Need to loop until we've made 1 full pass through without finding a match.
        // Looping more than once shouldn't happen very often, but will happen if
        // you have playlists named "New Playlist 1"/10/2/3/4/5/6/7/8/9, where
        // making only one pass would result in "New Playlist 10" being erroneously
        // picked for the new name.
        boolean done = false;
        while (!done) {
            done = true;
            c.moveToFirst();
            while (! c.isAfterLast()) {
                String playlistname = c.getString(0);
                if (playlistname.compareToIgnoreCase(suggestedName) == 0) {
                    suggestedName = String.format(template, num++);
                    done = false;
                }
                c.moveToNext();
            }
        }
        c.close();
        return suggestedName;
    }

    @Override
    protected String getString() {
        return mContext.getString(R.string.create_playlist_create_text_prompt);
    }

    @Override
    protected void setPlaylist(String name) {
        ContentResolver resolver = mContext.getContentResolver();
        int id = getIdForPlaylist(name);
        Uri uri;
        if (id >= 0) {
            uri = ContentUris.withAppendedId(
                    MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, id);
            MusicUtils.clearPlaylist(mContext, id);
        } else {
            ContentValues values = new ContentValues(1);
            values.put(MediaStore.Audio.Playlists.NAME, name);
            uri = resolver.insert(
                    MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, values);
        }

        if (uri != null && mContext instanceof OnButtonClickListener) {
            ((OnButtonClickListener) mContext)
                    .onPositiveButtonClicked(uri, mIsNewPlaylist);
        }
    }
}
