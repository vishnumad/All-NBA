package com.gmail.jorgegilcavazos.ballislife.features.boxscore;

import com.gmail.jorgegilcavazos.ballislife.features.model.BoxScoreValues;

public interface BoxScoreView {

    void showVisitorBoxScore(BoxScoreValues values);

    void showHomeBoxScore(BoxScoreValues values);

    void setLoadingIndicator(boolean active);

    void hideBoxScore();

    void showLoadingBoxScoreErrorMessage();

    void showBoxScoreNotAvailableMessage();

    void hideLoadMessage();
}
