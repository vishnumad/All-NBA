package com.gmail.jorgegilcavazos.ballislife.data.repository.games;

import com.gmail.jorgegilcavazos.ballislife.features.model.NbaGame;

import java.util.List;

import io.reactivex.Single;

/**
 * Provides and stores a list of {@link NbaGame}s for a specific date.
 */
public interface GamesRepository {

    void reset();

    Single<List<NbaGame>> getGames(String date);

    List<NbaGame> getCachedGames(String date);
}
