package com.gmail.jorgegilcavazos.ballislife.data.repository.games

import com.gmail.jorgegilcavazos.ballislife.data.actions.games.GamesResult
import com.gmail.jorgegilcavazos.ballislife.features.games.GamesUiModel
import com.gmail.jorgegilcavazos.ballislife.features.model.NbaGame
import io.reactivex.Observable
import java.util.*

/**
 * Provides and stores a list of [NbaGame]s for a specific date.
 */
interface GamesRepository {

  fun games(date: Calendar, forceNetwork: Boolean): Observable<GamesUiModel>

  fun loadGames(date: Calendar, forceNetwork: Boolean): Observable<GamesResult.LoadGamesResult>
}
