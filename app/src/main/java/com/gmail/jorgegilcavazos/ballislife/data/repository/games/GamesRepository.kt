package com.gmail.jorgegilcavazos.ballislife.data.repository.games

import com.gmail.jorgegilcavazos.ballislife.features.games.GamesUiModel
import com.gmail.jorgegilcavazos.ballislife.features.model.GameV2
import com.gmail.jorgegilcavazos.ballislife.features.model.NbaGame
import io.reactivex.Observable
import java.util.*

/**
 * Provides and stores a list of [NbaGame]s for a specific date.
 */
interface GamesRepository {

  fun getGames(date: Calendar, forceReload: Boolean): Observable<List<GameV2>>

  fun models(date: Calendar, forceNetwork: Boolean): Observable<GamesUiModel>
}
