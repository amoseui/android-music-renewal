package com.amoseui.music;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class MusicListBaseFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(
                R.layout.music_list_base_fragment, container, false);
        Bundle args = getArguments();
        ((TextView) rootView.findViewById(R.id.fragment_text)).setText(
                args.getString(this.getClass().getName()));
        return rootView;
    }
}
