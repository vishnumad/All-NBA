package com.gmail.jorgegilcavazos.ballislife.features.standings;

import com.gmail.jorgegilcavazos.ballislife.features.model.Standings;
import com.hannesdorfmann.mosby.mvp.MvpView;

public interface StandingsView {

    void setLoadingIndicator(boolean active);

    void showStandings(Standings standings);

    void hideStandings();

    void showSnackbar(boolean canReload);

    void dismissSnackbar();
}
