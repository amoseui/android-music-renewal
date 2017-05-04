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
import android.app.ActivityManager;
import android.app.Instrumentation;
import android.app.Instrumentation.ActivityMonitor;
import android.content.Context;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;
import android.view.KeyEvent;

import com.amoseui.music.utils.MusicUtils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static org.junit.Assert.assertFalse;

@RunWith(AndroidJUnit4.class)
public class MusicPlaybackStress {

    private static String TAG = "MusicPlaybackStress";

    @Rule
    public ActivityTestRule<TrackBrowserActivity> mActivityRule =
            new ActivityTestRule<>(TrackBrowserActivity.class);

    @Test
    public void testPlayAllSongs() {
        Activity mediaPlaybackActivity;
        try {
            Instrumentation inst = getInstrumentation();
            ActivityMonitor mediaPlaybackMon = inst.addMonitor("MediaPlaybackActivity",
                    null, false);
            inst.invokeMenuActionSync(
                    mActivityRule.getActivity(), MusicUtils.Defs.CHILD_MENU_BASE + 3, 0);
            Thread.sleep(MusicPlayerNames.WAIT_LONG_TIME);
            mediaPlaybackActivity = mediaPlaybackMon.waitForActivityWithTimeout(2000);
            for (int i = 0; i < MusicPlayerNames.NO_SKIPPING_SONGS; i++) {
                Thread.sleep(MusicPlayerNames.SKIP_WAIT_TIME);
                if (i == 0) {
                    //Set the repeat all
                    inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_RIGHT);
                    inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_UP);
                    inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_CENTER);

                    //Set focus on the next button
                    inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN);
                }
                inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_CENTER);
            }
            mediaPlaybackActivity.finish();
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }

        //Verification: check if it is in low memory
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ((ActivityManager) mActivityRule.getActivity().getSystemService(Context.ACTIVITY_SERVICE))
                .getMemoryInfo(mi);
        assertFalse(TAG, mi.lowMemory);
    }
}
