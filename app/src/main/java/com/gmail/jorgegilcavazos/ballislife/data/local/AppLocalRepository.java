package com.gmail.jorgegilcavazos.ballislife.data.local;

import android.content.SharedPreferences;

import com.gmail.jorgegilcavazos.ballislife.features.application.BallIsLifeApplication;

import javax.inject.Inject;
import javax.inject.Named;

public class AppLocalRepository implements LocalRepository {

    @Inject
    @Named("localSharedPreferences")
    SharedPreferences sharedPreferences;

    public AppLocalRepository() {
        BallIsLifeApplication.getAppComponent().inject(this);
    }

    @Override
    public void saveFavoritePostsViewType(int viewType) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(LocalSharedPreferences.POSTS_VIEW_TYPE, viewType);
        editor.apply();
    }

    @Override
    public int getFavoritePostsViewType() {
        return sharedPreferences.getInt(LocalSharedPreferences.POSTS_VIEW_TYPE, -1);
    }

    @Override
    public void saveFavoriteHighlightViewType(int viewType) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(LocalSharedPreferences.HIGHLIGHTS_VIEW_TYPE, viewType);
        editor.apply();
    }

    @Override
    public int getFavoriteHighlightViewType() {
        return sharedPreferences.getInt(LocalSharedPreferences.HIGHLIGHTS_VIEW_TYPE, -1);
    }
}
