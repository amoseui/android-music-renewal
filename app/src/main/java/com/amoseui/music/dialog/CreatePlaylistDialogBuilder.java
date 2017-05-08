package com.amoseui.music.dialog;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.amoseui.music.R;
import com.amoseui.music.utils.MusicUtils;

public class CreatePlaylistDialogBuilder extends AlertDialog.Builder
        implements MusicUtils.Defs {

    private static final String TAG = "CreatePlaylistDialogBuilder";

    private Context mContext;

    private boolean mIsNewPlaylist = true;

    private EditText mPlaylistEditText;
    private Button mPositiveButton;

    TextWatcher mTextWatcher = new TextWatcher() {

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // don't care about this one
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
            setSaveButton();
        }

        public void afterTextChanged(Editable s) {
            // don't care about this one
        }
    };

    public CreatePlaylistDialogBuilder(Context context) {
        super(context);
        mContext = context;

        View view = View.inflate(mContext, R.layout.create_playlist, null);
        TextView prompt = (TextView) view.findViewById(R.id.prompt);
        mPlaylistEditText = (EditText) view.findViewById(R.id.playlist);

        String playlistName = makePlaylistName();
        if (playlistName == null) {
            Log.e(TAG, "Create failed: playlistName is null");
            return;
        }

        String promptFormat =
                mContext.getString(R.string.create_playlist_create_text_prompt);
        prompt.setText(String.format(promptFormat, playlistName));
        mPlaylistEditText.setText(playlistName);
        mPlaylistEditText.setSelection(playlistName.length());
        mPlaylistEditText.addTextChangedListener(mTextWatcher);
        setView(view);
        setPositiveButton(R.string.create_playlist_create_text,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name = mPlaylistEditText.getText().toString();
                        if (name.length() > 0) {
                            ContentResolver resolver = mContext.getContentResolver();
                            int id = getIdForPlaylist(name);
                            Uri uri;
                            if (id >= 0) {
                                uri = ContentUris.withAppendedId(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, id);
                                MusicUtils.clearPlaylist(mContext, id);
                            } else {
                                ContentValues values = new ContentValues(1);
                                values.put(MediaStore.Audio.Playlists.NAME, name);
                                uri = resolver.insert(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, values);
                            }

                            if (uri != null && mContext instanceof PlaylistDialogCallback) {
                                ((PlaylistDialogCallback) mContext).onPositiveButtonClicked(uri, mIsNewPlaylist);
                            }
                            dialog.dismiss();
                        }
                    }
                });
        setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing
                    }
                });
    }

    @Override
    public AlertDialog show() {
        AlertDialog dialog = super.show();
        mPositiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        return dialog;
    }

    public CreatePlaylistDialogBuilder setNewPlaylist(boolean isNewPlaylist) {
        mIsNewPlaylist = isNewPlaylist;
        return this;
    }

    private int getIdForPlaylist(String name) {
        Cursor c = MusicUtils.query(
                mContext, MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Audio.Playlists._ID },
                MediaStore.Audio.Playlists.NAME + "=?",
                new String[] { name },
                MediaStore.Audio.Playlists.NAME);
        int id = -1;
        if (c != null) {
            c.moveToFirst();
            if (!c.isAfterLast()) {
                id = c.getInt(0);
            }
            c.close();
        }
        return id;
    }

    private void setSaveButton() {
        String typedName = mPlaylistEditText.getText().toString();
        if (typedName.trim().length() == 0) {
            mPositiveButton.setEnabled(false);
        } else {
            mPositiveButton.setEnabled(true);
            if (getIdForPlaylist(typedName) >= 0) {
                mPositiveButton.setText(R.string.create_playlist_overwrite_text);
            } else {
                mPositiveButton.setText(R.string.create_playlist_create_text);
            }
        }
    }

    @Nullable
    private String makePlaylistName() {
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
}
