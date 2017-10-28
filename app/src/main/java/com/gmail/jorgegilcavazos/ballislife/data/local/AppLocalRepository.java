package com.gmail.jorgegilcavazos.ballislife.data.local;

import android.content.SharedPreferences;

import com.gmail.jorgegilcavazos.ballislife.features.model.HighlightViewType;
import com.gmail.jorgegilcavazos.ballislife.features.model.SwishTheme;
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
    public void saveFavoriteHighlightViewType(HighlightViewType viewType) {
        SharedPreferences.Editor editor = localSharedPreferences.edit();
        editor.putInt(LocalSharedPreferences.HIGHLIGHTS_VIEW_TYPE, viewType.getValue());
        editor.apply();
    }

    @Override
    public HighlightViewType getFavoriteHighlightViewType() {
        int value = localSharedPreferences.getInt(LocalSharedPreferences.HIGHLIGHTS_VIEW_TYPE,
                                                  HighlightViewType.SMALL.getValue());
        for (HighlightViewType viewType : HighlightViewType.values()) {
            if (viewType.getValue() == value) {
                return viewType;
            }
        }
        saveFavoriteHighlightViewType(HighlightViewType.SMALL);
        return HighlightViewType.SMALL;
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

    @Override
    public boolean getOpenYouTubeInApp() {
        return defaultSharedPreferences.getBoolean(SettingsFragment.KEY_YOUTUBE_IN_APP, true);
    }

    @Override
    public void saveAppTheme(SwishTheme theme) {
        SharedPreferences.Editor editor = localSharedPreferences.edit();
        editor.putInt(LocalSharedPreferences.SWISH_THEME, theme.getValue());
        editor.apply();
    }

    @Override
    public SwishTheme getAppTheme() {
        int value = localSharedPreferences.getInt(LocalSharedPreferences.SWISH_THEME, -1);

        if (SwishTheme.LIGHT.getValue() == value) {
            return SwishTheme.LIGHT;
        } else if (SwishTheme.DARK.getValue() == value) {
            return SwishTheme.DARK;
        } else {
            return SwishTheme.LIGHT;
        }
    }

    @Override
    public boolean shouldShowWhatsNew() {
        return localSharedPreferences.getBoolean(LocalSharedPreferences.SHOW_WHATS_NEW, true);
    }

    @Override
    public void setShouldShowWhatsNew(boolean showWhatsNew) {
        SharedPreferences.Editor editor = localSharedPreferences.edit();
        editor.putBoolean(LocalSharedPreferences.SHOW_WHATS_NEW, showWhatsNew);
        editor.apply();
    }
}
