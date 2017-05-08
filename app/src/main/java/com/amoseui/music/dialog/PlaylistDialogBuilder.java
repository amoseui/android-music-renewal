package com.amoseui.music.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.amoseui.music.R;
import com.amoseui.music.utils.MusicUtils;

public abstract class PlaylistDialogBuilder extends AlertDialog.Builder
        implements MusicUtils.Defs {

    private static final String TAG = "PlaylistDialogBuilder";

    protected Context mContext;

    private EditText mPlaylistEditText;
    private Button mPositiveButton;
    private String mPlaylistName;

    public interface OnButtonClickListener {
        void onPositiveButtonClicked(Uri uri, boolean isNewPlaylist);
    }

    public PlaylistDialogBuilder(Context context, long id) {
        super(context);
        mContext = context;

        View view = View.inflate(mContext, R.layout.create_playlist, null);
        TextView prompt = (TextView) view.findViewById(R.id.prompt);
        mPlaylistEditText = (EditText) view.findViewById(R.id.playlist);
        mPlaylistName = getPlaylistName(id);
        if (id < 0 || mPlaylistName == null) {
            Log.e(TAG, "mPlaylistName is null");
            return;
        }

        String promptFormat = getString();
        prompt.setText(String.format(promptFormat, mPlaylistName));
        mPlaylistEditText.setText(mPlaylistName);
        mPlaylistEditText.setSelection(mPlaylistName.length());
        mPlaylistEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                setSaveButton();
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Do nothing
            }
        });
        setView(view);
        setPositiveButton(R.string.create_playlist_create_text,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name = mPlaylistEditText.getText().toString();
                        if (name.length() > 0) {
                            setPlaylist(name);
                        }
                        dialog.dismiss();
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

    protected int getIdForPlaylist(String name) {
        Cursor c = MusicUtils.query(
                mContext, MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Playlists._ID},
                MediaStore.Audio.Playlists.NAME + "=?",
                new String[]{name},
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
                    && !mPlaylistName.equals(typedName)) {
                mPositiveButton.setText(R.string.create_playlist_overwrite_text);
            } else {
                mPositiveButton.setText(R.string.create_playlist_create_text);
            }
        }
    }

    protected abstract String getPlaylistName(long id);

    protected abstract String getString();

    protected abstract void setPlaylist(String name);
}
