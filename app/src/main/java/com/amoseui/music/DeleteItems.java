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
import android.media.AudioManager;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.amoseui.music.utils.MusicUtils;

public class DeleteItems extends Activity {
    private long [] mItemList;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.confirm_delete);
        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                                    WindowManager.LayoutParams.WRAP_CONTENT);

        TextView prompt = (TextView)findViewById(R.id.prompt);
        Button button = (Button) findViewById(R.id.delete);
        button.setOnClickListener(mButtonClicked);

        findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

        Bundle b = getIntent().getExtras();
        String desc = b.getString("description");
        mItemList = b.getLongArray("items");

        prompt.setText(desc);
    }
    
    private View.OnClickListener mButtonClicked = new View.OnClickListener() {
        public void onClick(View v) {
            // delete the selected item(s)
            MusicUtils.deleteTracks(DeleteItems.this, mItemList);
            finish();
        }
    };
}
