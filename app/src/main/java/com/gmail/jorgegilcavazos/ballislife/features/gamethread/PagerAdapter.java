package com.gmail.jorgegilcavazos.ballislife.features.gamethread;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.gmail.jorgegilcavazos.ballislife.features.boxscore.BoxScoreFragment;
import com.gmail.jorgegilcavazos.ballislife.features.model.GameThreadType;

import java.util.HashMap;
import java.util.Map;

/**
 * Pager for the TabLayout in CommentsActivity.
 */
public class PagerAdapter extends FragmentStatePagerAdapter {

    private int numOfTabs;
    private Bundle bundle;
    private Map<Integer, Fragment> fragmentMap;

    public static final int GAME_THREAD_TAB = 0;
    public static final int BOX_SCORE_TAB = 1;
    public static final int POST_GAME_TAB = 2;

    public PagerAdapter(FragmentManager fragmentManager, int numOfTabs, Bundle bundle) {
        super(fragmentManager);
        this.numOfTabs = numOfTabs;
        this.bundle = bundle;

        fragmentMap = new HashMap<>();
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case GAME_THREAD_TAB:
                bundle.putSerializable(GameThreadFragment.THREAD_TYPE_KEY, GameThreadType.LIVE);
                if (fragmentMap.get(0) != null) {
                    return fragmentMap.get(position);
                } else {
                    GameThreadFragment tab1 = GameThreadFragment.newInstance();
                    tab1.setArguments(bundle);
                    fragmentMap.put(0, tab1);
                    return tab1;
                }
            case BOX_SCORE_TAB:
                BoxScoreFragment tab2 = new BoxScoreFragment();
                tab2.setArguments(bundle);
                fragmentMap.put(1, tab2);
                return tab2;
            case POST_GAME_TAB:
                bundle.putSerializable(GameThreadFragment.THREAD_TYPE_KEY, GameThreadType.POST);
                if (fragmentMap.get(2) != null) {
                    return fragmentMap.get(2);
                } else {
                    GameThreadFragment tab3 = GameThreadFragment.newInstance();
                    tab3.setArguments(bundle);
                    fragmentMap.put(2, tab3);
                    return tab3;
                }
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return numOfTabs;
    }
}