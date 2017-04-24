package com.gmail.jorgegilcavazos.ballislife.features.games;

import com.gmail.jorgegilcavazos.ballislife.features.model.NbaGame;

import java.util.Calendar;
import java.util.List;

public interface GamesView {

    void setLoadingIndicator(boolean active);

    void setDateNavigatorText(String dateText);

    void hideGames();

    void showGames(List<NbaGame> games);

    void showGameDetails(NbaGame game, Calendar selectedDate);

    void updateScores(List<NbaGame> games);

    void setNoGamesIndicator(boolean active);

    void showSnackbar(boolean canReload);

    void dismissSnackbar();
}
