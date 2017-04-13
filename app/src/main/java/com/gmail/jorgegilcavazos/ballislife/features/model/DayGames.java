package com.gmail.jorgegilcavazos.ballislife.features.model;

import java.util.List;

public class DayGames {

    private List<NbaGame> games;
    private int num_games;

    public DayGames(List<NbaGame> games, int num_games) {
        this.games = games;
        this.num_games = num_games;
    }

    public List<NbaGame> getGames() {
        return games;
    }

    public void setGames(List<NbaGame> games) {
        this.games = games;
    }

    public int getNum_games() {
        return num_games;
    }

    public void setNum_games(int num_games) {
        this.num_games = num_games;
    }
}
