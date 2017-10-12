package com.gmail.jorgegilcavazos.ballislife.data.repository.games;

import com.gmail.jorgegilcavazos.ballislife.features.model.GameV2;
import com.gmail.jorgegilcavazos.ballislife.features.model.NbaGame;

import java.util.Calendar;
import java.util.List;

import io.reactivex.Single;

/**
 * Provides and stores a list of {@link NbaGame}s for a specific date.
 */
public interface GamesRepository {

    Single<List<GameV2>> getGames(Calendar date);
}
