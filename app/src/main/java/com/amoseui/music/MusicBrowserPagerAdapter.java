package com.amoseui.music;

import android.os.Bundle;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

public class MusicBrowserPagerAdapter extends FragmentPagerAdapter {

    private enum FragmentType {
        ARTISTS,
        ALBUMS,
        SONGS,
        PLAYLIST,
    }

    public MusicBrowserPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = new MusicListBaseFragment();
        Bundle args = new Bundle();
        args.putString(MusicListBaseFragment.class.getName(),
                FragmentType.values()[position].name());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getCount() {
        return FragmentType.values().length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return FragmentType.values()[position].name();
    }
}
