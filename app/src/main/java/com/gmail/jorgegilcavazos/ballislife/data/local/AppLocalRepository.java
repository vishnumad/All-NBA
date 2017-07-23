package com.gmail.jorgegilcavazos.ballislife.data.local;

import android.content.SharedPreferences;

import com.gmail.jorgegilcavazos.ballislife.features.settings.SettingsFragment;
import com.gmail.jorgegilcavazos.ballislife.util.Constants;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
public class AppLocalRepository implements LocalRepository {
    SharedPreferences localSharedPreferences;
    SharedPreferences defaultSharedPreferences;

    @Inject
    public AppLocalRepository(
            @Named("localSharedPreferences") SharedPreferences localSharedPreferences,
            @Named("defaultSharedPreferences") SharedPreferences defaultSharedPreferences) {
        this.localSharedPreferences = localSharedPreferences;
        this.defaultSharedPreferences = defaultSharedPreferences;
    }

    @Override
    public void saveFavoritePostsViewType(int viewType) {
        SharedPreferences.Editor editor = localSharedPreferences.edit();
        editor.putInt(LocalSharedPreferences.POSTS_VIEW_TYPE, viewType);
        editor.apply();
    }

    @Override
    public int getFavoritePostsViewType() {
        return localSharedPreferences.getInt(LocalSharedPreferences.POSTS_VIEW_TYPE,
                Constants.POSTS_VIEW_WIDE_CARD);
    }

    @Override
    public void saveFavoriteHighlightViewType(int viewType) {
        SharedPreferences.Editor editor = localSharedPreferences.edit();
        editor.putInt(LocalSharedPreferences.HIGHLIGHTS_VIEW_TYPE, viewType);
        editor.apply();
    }

    @Override
    public int getFavoriteHighlightViewType() {
        return localSharedPreferences.getInt(LocalSharedPreferences.HIGHLIGHTS_VIEW_TYPE,
                Constants.HIGHLIGHTS_VIEW_SMALL);
    }

    @Override
    public void saveUsername(String username) {
        SharedPreferences.Editor editor = localSharedPreferences.edit();
        editor.putString(LocalSharedPreferences.USERNAME, username);
        editor.apply();
    }

    @Override
    public String getUsername() {
        return localSharedPreferences.getString(LocalSharedPreferences.USERNAME, null);
    }

    @Override
    public String getStartupFragment() {
        return defaultSharedPreferences.getString(SettingsFragment.KEY_STARTUP_FRAGMENT,
                SettingsFragment.STARTUP_FRAGMENT_GAMES);
    }
}
