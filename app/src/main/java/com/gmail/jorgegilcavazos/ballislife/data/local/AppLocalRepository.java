package com.gmail.jorgegilcavazos.ballislife.data.local;

import android.content.SharedPreferences;

public class AppLocalRepository implements LocalRepository {

    private SharedPreferences sharedPreferences;

    public AppLocalRepository(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
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
