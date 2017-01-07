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

import android.app.ActivityManager;
import android.app.Instrumentation;
import android.content.Context;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;
import android.view.KeyEvent;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static org.junit.Assert.assertFalse;

@RunWith(AndroidJUnit4.class)
public class AlbumsPlaybackStress {

    private String TAG = "AlbumsPlaybackStress";

    @Rule
    public ActivityTestRule<AlbumBrowserActivity> mActivityRule =
            new ActivityTestRule<>(AlbumBrowserActivity.class);

    /**
     * Test case: Keeps launching music playback from Albums and then go
     * back to the album screen
     * Verification: Check if it is in low memory
     * The test depends on the test media in the sdcard
     */
    @Test
    public void testAlbumPlay() {
        Instrumentation inst = getInstrumentation();
        try {
            inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_RIGHT);
            inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_RIGHT);
            inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_CENTER);
            inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_CENTER);
            inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_CENTER);
            Thread.sleep(MusicPlayerNames.WAIT_LONG_TIME);
            inst.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
            inst.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
            for (int i = 0; i < MusicPlayerNames.NO_ALBUMS_TOBE_PLAYED; i++) {
                inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN);
                inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_CENTER);
                inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_CENTER);
                Thread.sleep(MusicPlayerNames.WAIT_LONG_TIME);
                inst.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
                inst.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
            }
        } catch (Exception e) {
            Log.v(TAG, e.toString());
        }
        inst.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);

        //Verification: check if it is in low memory
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ((ActivityManager) mActivityRule.getActivity().getSystemService(Context.ACTIVITY_SERVICE))
                .getMemoryInfo(mi);
        assertFalse(TAG, mi.lowMemory);
    }
}
