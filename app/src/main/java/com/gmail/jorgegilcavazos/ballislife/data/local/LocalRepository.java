package com.gmail.jorgegilcavazos.ballislife.data.local;

import com.gmail.jorgegilcavazos.ballislife.features.model.HighlightViewType;

public interface LocalRepository {

    void saveFavoritePostsViewType(int viewType);

    int getFavoritePostsViewType();

    void saveFavoriteHighlightViewType(HighlightViewType viewType);

    HighlightViewType getFavoriteHighlightViewType();

    void saveUsername(String username);

    String getUsername();

    String getStartupFragment();
}
