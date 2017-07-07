package com.gmail.jorgegilcavazos.ballislife.data.local;

public interface LocalRepository {

    void saveFavoritePostsViewType(int viewType);

    int getFavoritePostsViewType();

    void saveFavoriteHighlightViewType(int viewType);

    int getFavoriteHighlightViewType();

    void saveUsername(String username);

    String getUsername();

    String getStartupFragment();
}
