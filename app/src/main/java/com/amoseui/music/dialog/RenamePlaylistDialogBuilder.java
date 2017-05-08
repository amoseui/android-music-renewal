package com.amoseui.music.dialog;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.amoseui.music.R;
import com.amoseui.music.utils.MusicUtils;

public class RenamePlaylistDialogBuilder extends AlertDialog.Builder {

    private static final String TAG = "RenamePlaylistDialogBuilder";

    private Context mContext;

    private EditText mPlaylistEditText;
    private Button mPositiveButton;
    private String mOriginalName;

    private TextWatcher mTextWatcher = new TextWatcher() {

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // don't care about this one
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // check if playlist with current name exists already, and warn the user if so.
            setSaveButton();
        }

        public void afterTextChanged(Editable s) {
            // don't care about this one
        }
    };

    public RenamePlaylistDialogBuilder(Context context, long id) {
        super(context);
        mContext = context;

        View view = View.inflate(mContext, R.layout.create_playlist, null);
        TextView prompt = (TextView) view.findViewById(R.id.prompt);
        mPlaylistEditText = (EditText) view.findViewById(R.id.playlist);
        final long renameId = id;
        mOriginalName = nameForId(renameId);
        if (renameId < 0 || mOriginalName == null) {
            Log.i(TAG, "Rename failed: " + renameId + "/" + mOriginalName);
            return;
        }

        String promptFormat =
                mContext.getString(R.string.rename_playlist_same_prompt);
        prompt.setText(
                String.format(promptFormat, mOriginalName));
        mPlaylistEditText.setText(mOriginalName);
        mPlaylistEditText.setSelection(mOriginalName.length());
        mPlaylistEditText.addTextChangedListener(mTextWatcher);
        setCancelable(true);
        setView(view);
        setPositiveButton(R.string.create_playlist_create_text,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name = mPlaylistEditText.getText().toString();
                        if (name.length() > 0) {
                            ContentResolver resolver = mContext.getContentResolver();
                            ContentValues values = new ContentValues(1);
                            values.put(MediaStore.Audio.Playlists.NAME, name);
                            resolver.update(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                                    values,
                                    MediaStore.Audio.Playlists._ID + "=?",
                                    new String[]{Long.valueOf(renameId).toString()});
                            Toast.makeText(mContext, R.string.playlist_renamed_message, Toast.LENGTH_SHORT).show();
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

    private String nameForId(long id) {
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
            if (getIdForPlaylist(typedName) >= 0
                    && !mOriginalName.equals(typedName)) {
                mPositiveButton.setText(R.string.create_playlist_overwrite_text);
            } else {
                mPositiveButton.setText(R.string.create_playlist_create_text);
            }
        }

    }
}
