package com.gmail.jorgegilcavazos.ballislife.features.games;

import com.gmail.jorgegilcavazos.ballislife.features.model.GameV2;

import java.util.Calendar;
import java.util.List;

public interface GamesView {

    void setLoadingIndicator(boolean active);

    void setDateNavigatorText(String dateText);

    void hideGames();

    void showGames(List<GameV2> games);

    void showGameDetails(GameV2 game, Calendar selectedDate);

    void updateScores(List<GameV2> games);

    void setNoGamesIndicator(boolean active);

    void showNoNetSnackbar();

    void showErrorSnackbar();

    void dismissSnackbar();
}
