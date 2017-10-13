package com.gmail.jorgegilcavazos.ballislife.features.games;

import com.gmail.jorgegilcavazos.ballislife.features.model.GameV2;

import java.util.List;

public class GamesUiModel {

    private final boolean memoryInProgress;
    private final boolean memorySuccess;
    private final boolean networkInProgress;
    private final boolean networkSuccess;
    private final List<GameV2> games;

    public GamesUiModel(
            boolean memoryInProgress,
            boolean memorySuccess,
            boolean networkInProgress,
            boolean networkSuccess,
            List<GameV2> games) {
        this.memoryInProgress = memoryInProgress;
        this.memorySuccess = memorySuccess;
        this.networkInProgress = networkInProgress;
        this.networkSuccess = networkSuccess;
        this.games = games;
    }

    public static GamesUiModel memoryInProgress() {
        return new GamesUiModel(true, false, false, false, null);
    }

    public static GamesUiModel memorySuccess(List<GameV2> games) {
        return new GamesUiModel(false, true, false, false, games);
    }

    public static GamesUiModel networkInProgress() {
        return new GamesUiModel(false, false, true, false, null);
    }

    public static GamesUiModel networkSuccess(List<GameV2> games) {
        return new GamesUiModel(false, false, false, true, games);
    }

    public boolean isMemoryInProgress() {
        return memoryInProgress;
    }

    public boolean isMemorySuccess() {
        return memorySuccess;
    }

    public boolean isNetworkInProgress() {
        return networkInProgress;
    }

    public boolean isNetworkSuccess() {
        return networkSuccess;
    }

    public List<GameV2> getGames() {
        return games;
    }
}
