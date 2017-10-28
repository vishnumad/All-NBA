package com.gmail.jorgegilcavazos.ballislife.data.local;

import com.gmail.jorgegilcavazos.ballislife.features.model.HighlightViewType;
import com.gmail.jorgegilcavazos.ballislife.features.model.SwishTheme;

public interface LocalRepository {

    void saveFavoritePostsViewType(int viewType);

    int getFavoritePostsViewType();

    void saveFavoriteHighlightViewType(HighlightViewType viewType);

    HighlightViewType getFavoriteHighlightViewType();

    void saveUsername(String username);

    String getUsername();

    String getStartupFragment();

    boolean getOpenYouTubeInApp();

    void saveAppTheme(SwishTheme theme);

    SwishTheme getAppTheme();

    boolean shouldShowWhatsNew();

    void setShouldShowWhatsNew(boolean showWhatsNew);
}
