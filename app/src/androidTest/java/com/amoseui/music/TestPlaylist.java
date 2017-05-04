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

import android.app.Activity;
import android.app.Instrumentation;
import android.app.Instrumentation.ActivityMonitor;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.provider.MediaStore;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.KeyEvent;

import com.amoseui.music.utils.MusicUtils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Junit / Instrumentation test case for the PlaylistBrowserActivity
 * This test case need to run in the landscape mode and opened keyboard
 */
@RunWith(AndroidJUnit4.class)
public class TestPlaylist {

    private static String TAG = "TestPlaylist";

    @Rule
    public ActivityTestRule<PlaylistBrowserActivity> mActivityRule =
            new ActivityTestRule<>(PlaylistBrowserActivity.class);
    
    private void clearSearchString(int length){
        Instrumentation inst = getInstrumentation();
        for (int j=0; j< length; j++)
            inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DEL);
    }
    /**
     * Remove playlist
     */
    public void deletePlaylist(String playlistname) throws Exception{
        Instrumentation inst = getInstrumentation();
        inst.sendStringSync(playlistname);
        Thread.sleep(MusicPlayerNames.WAIT_SHORT_TIME);
        inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN);       
        inst.invokeContextMenuAction(
                mActivityRule.getActivity(), MusicUtils.Defs.CHILD_MENU_BASE + 1, 0);
        Thread.sleep(MusicPlayerNames.WAIT_SHORT_TIME);
        clearSearchString(playlistname.length());
        
    }

    /**
     *  Start the trackBrowserActivity and add the new playlist
     */
    public void addNewPlaylist(String playListName) throws Exception{
        Instrumentation inst = getInstrumentation();
        Activity trackBrowserActivity;
        ActivityMonitor trackBrowserMon = inst.addMonitor("TrackBrowserActivity",
                null, false);
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_PICK);
        intent.setClassName("com.amoseui.music", "TrackBrowserActivity");
        mActivityRule.getActivity().startActivity(intent);
        Thread.sleep(MusicPlayerNames.WAIT_LONG_TIME);
        trackBrowserActivity = trackBrowserMon.waitForActivityWithTimeout(2000);
        inst.invokeContextMenuAction(trackBrowserActivity, MusicUtils.Defs.NEW_PLAYLIST, 0);
        Thread.sleep(MusicPlayerNames.WAIT_SHORT_TIME);
        //Remove the default playlist name
        clearSearchString(MusicPlayerNames.DEFAULT_PLAYLIST_LENGTH);
        inst.sendStringSync(playListName);
        inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN);
        inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_CENTER);
        Thread.sleep(MusicPlayerNames.WAIT_LONG_TIME);
        trackBrowserActivity.finish();
        clearSearchString(playListName.length());

    }
    
    /**
     * Rename playlist
     */
    public void renamePlaylist(String oldPlaylistName, String newPlaylistName) throws Exception{
        Instrumentation inst = getInstrumentation();
        inst.sendStringSync(oldPlaylistName);
        Thread.sleep(MusicPlayerNames.WAIT_SHORT_TIME);
        inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN);       
        inst.invokeContextMenuAction(
                mActivityRule.getActivity(), MusicUtils.Defs.CHILD_MENU_BASE + 3, 0);
        Thread.sleep(MusicPlayerNames.WAIT_SHORT_TIME);
        //Remove the old playlist name
        clearSearchString(oldPlaylistName.length());
        inst.sendStringSync(newPlaylistName);
        inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN);
        inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_CENTER);
        Thread.sleep(MusicPlayerNames.WAIT_LONG_TIME);
        clearSearchString(oldPlaylistName.length());
    }

    public boolean verifyPlaylist(String playlistname) throws Exception{
        Cursor mCursor;
        boolean isEmptyPlaylist = true;
        String[] cols = new String[] {
                MediaStore.Audio.Playlists.NAME
        };
        ContentResolver resolver = mActivityRule.getActivity().getContentResolver();
        if (resolver == null) {
            System.out.println("resolver = null");
            assertNull(TAG, resolver);
        } else {
            String whereclause = MediaStore.Audio.Playlists.NAME + " = '" + playlistname +"'";
            mCursor = resolver.query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                    cols, whereclause, null,
                    MediaStore.Audio.Playlists.NAME);
            isEmptyPlaylist = mCursor.moveToFirst();
        }
        return isEmptyPlaylist;
    }

    /**
     * Test case 1: Add a playlist and delet the playlist just added.
     * Verification: The mediastore playlist should be empty
     */
    @Test
    public void testDeletePlaylist() throws Exception{
        boolean isEmptyPlaylist = true;
        addNewPlaylist(MusicPlayerNames.DELETE_PLAYLIST_NAME);
        deletePlaylist(MusicPlayerNames.DELETE_PLAYLIST_NAME);  
        isEmptyPlaylist = verifyPlaylist(MusicPlayerNames.DELETE_PLAYLIST_NAME);
        assertFalse("testDeletePlaylist", isEmptyPlaylist);
    }
    
    /**
     * Test case 2: Add playlist and rename the playlist just added.
     * Verification: The mediastore playlist should contain the updated name.
     */
    @Test
    public void testRenamePlaylist() throws Exception{
        boolean isEmptyPlaylist = true;
        addNewPlaylist(MusicPlayerNames.ORIGINAL_PLAYLIST_NAME);
        renamePlaylist(MusicPlayerNames.ORIGINAL_PLAYLIST_NAME, MusicPlayerNames.RENAMED_PLAYLIST_NAME);  
        isEmptyPlaylist = verifyPlaylist(MusicPlayerNames.RENAMED_PLAYLIST_NAME);
        deletePlaylist(MusicPlayerNames.RENAMED_PLAYLIST_NAME);
        assertTrue("testDeletePlaylist", isEmptyPlaylist);
    }

}    
