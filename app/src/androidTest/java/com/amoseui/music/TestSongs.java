/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.amoseui.music;

import android.app.Instrumentation;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;
import android.view.KeyEvent;

import com.amoseui.music.utils.MusicUtils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Junit / Instrumentation test case for the TrackBrowserActivity
 */
@RunWith(AndroidJUnit4.class)
public class TestSongs {

    private static String TAG = "TestSongs";

    @Rule
    public ActivityTestRule<TrackBrowserActivity> mActivityRule =
            new ActivityTestRule<>(TrackBrowserActivity.class);

    /**
     * Add 10 new playlists with unsorted title order
     */
    public void addNewPlaylist() throws Exception {
        Instrumentation inst = getInstrumentation();
        for (int i = 0; i < MusicPlayerNames.NO_OF_PLAYLIST; i++) {
            inst.invokeContextMenuAction(mActivityRule.getActivity(), MusicUtils.Defs.NEW_PLAYLIST, 0);
            Thread.sleep(MusicPlayerNames.WAIT_SHORT_TIME);
            //Remove the default playlist name
            for (int j = 0; j < MusicPlayerNames.DEFAULT_PLAYLIST_LENGTH; j++)
                inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DEL);
            inst.sendStringSync(MusicPlayerNames.unsortedPlaylistTitle[i]);
            inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN);
            inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_CENTER);
            Thread.sleep(MusicPlayerNames.WAIT_LONG_TIME);
            inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN);
            Thread.sleep(MusicPlayerNames.WAIT_LONG_TIME);
        }
    }

    private void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
        Log.v(TAG, "Copy file");
    }

    //Rescan the sdcard after copy the file
    private void rescanSdcard() throws Exception {
        Intent scanIntent = new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://"
                + Environment.getExternalStorageDirectory()));
        Log.v(TAG, "start the intent");
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_MEDIA_SCANNER_STARTED);
        intentFilter.addDataScheme("file");
        mActivityRule.getActivity().sendBroadcast(
                new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://"
                        + Environment.getExternalStorageDirectory())));
        Thread.sleep(MusicPlayerNames.WAIT_VERY_LONG_TIME);
    }


    /**
     * Test case 1: tests the new playlist added with sorted order.
     * Verification: The new playlist title should be sorted in alphabetical order
     */
    @Test
    public void testAddPlaylist() throws Exception {
        Cursor mCursor;
        addNewPlaylist();

        //Verify the new playlist is created, check the playlist table
        String[] cols = new String[]{
                MediaStore.Audio.Playlists.NAME
        };
        ContentResolver resolver = mActivityRule.getActivity().getContentResolver();
        if (resolver == null) {
            System.out.println("resolver = null");
        } else {
            String whereclause = MediaStore.Audio.Playlists.NAME + " != ''";
            mCursor = resolver.query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                    cols, whereclause, null,
                    MediaStore.Audio.Playlists.NAME);
            //Check the new playlist
            mCursor.moveToFirst();

            for (int j = 0; j < 10; j++) {
                assertEquals("New sorted Playlist title:", MusicPlayerNames.expectedPlaylistTitle[j], mCursor.getString(0));
                mCursor.moveToNext();
            }
        }
    }

    /**
     * Test case 2: Set a song as ringtone
     * Test case precondition: The testing device should wipe data before
     * run the test case.
     * Verification: The count of audio.media.is_ringtone equal to 1.
     */
    @Test
    public void testSetRingtone() throws Exception {
        Cursor mCursor;
        Instrumentation inst = getInstrumentation();
        inst.invokeContextMenuAction(mActivityRule.getActivity(), MusicUtils.Defs.USE_AS_RINGTONE, 0);
        //This only check if there only 1 ringtone set in music player
        ContentResolver resolver = mActivityRule.getActivity().getContentResolver();
        if (resolver == null) {
            System.out.println("resolver = null");
        } else {
            String whereclause = MediaStore.Audio.Media.IS_RINGTONE + " = 1";
            mCursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    null, whereclause, null, null);
            //Check the new playlist
            mCursor.moveToFirst();
            int isRingtoneSet = mCursor.getCount();
            assertEquals(TAG, MusicPlayerNames.EXPECTED_NO_RINGTONE, isRingtoneSet);
        }
    }

    /**
     * Test case 3: Delete a song
     * Test case precondition: Copy a song and rescan the sdcard
     * Verification: The song is deleted from the sdcard and mediastore
     */
    @Test
    public void testDeleteSong() throws Exception {
        Instrumentation inst = getInstrumentation();
        Cursor mCursor;

        //Copy a song from the golden directory
        Log.v(TAG, "Copy a temp file to the sdcard");
        File goldenfile = new File(MusicPlayerNames.GOLDENSONG);
        File toBeDeleteSong = new File(MusicPlayerNames.DELETESONG);
        copy(goldenfile, toBeDeleteSong);
        rescanSdcard();

        //Delete the file from music player
        Thread.sleep(MusicPlayerNames.WAIT_LONG_TIME);
        inst.sendStringSync(MusicPlayerNames.TOBEDELETESONGNAME);
        Thread.sleep(MusicPlayerNames.WAIT_LONG_TIME);
        inst.invokeContextMenuAction(mActivityRule.getActivity(), MusicUtils.Defs.DELETE_ITEM, 0);
        inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN);
        inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_CENTER);
        Thread.sleep(MusicPlayerNames.WAIT_LONG_TIME);

        //Clear the search string
        for (int j = 0; j < MusicPlayerNames.TOBEDELETESONGNAME.length(); j++)
            inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DEL);

        //Verfiy the item is removed from sdcard
        File checkDeletedFile = new File(MusicPlayerNames.DELETESONG);
        assertFalse(TAG, checkDeletedFile.exists());

        ContentResolver resolver = mActivityRule.getActivity().getContentResolver();
        if (resolver == null) {
            System.out.println("resolver = null");
        } else {
            String whereclause = MediaStore.Audio.Media.DISPLAY_NAME + " = '" +
                    MusicPlayerNames.TOBEDELETESONGNAME + "'";
            mCursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    null, whereclause, null, null);
            boolean isEmptyCursor = mCursor.moveToFirst();
            assertFalse(TAG, isEmptyCursor);
        }
    }
}
 
