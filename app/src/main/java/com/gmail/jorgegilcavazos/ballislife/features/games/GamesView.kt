package com.gmail.jorgegilcavazos.ballislife.features.games

import com.gmail.jorgegilcavazos.ballislife.features.games.GamesUiEvent.*
import com.gmail.jorgegilcavazos.ballislife.features.model.GameV2
import io.reactivex.Observable
import java.util.*

interface GamesView {

  fun openGameEvents(): Observable<OpenGameEvent>

  fun dateSelectionEvents(): Observable<DateSelectedEvent>

  fun loadGamesEvents(): Observable<LoadGamesEvent>

  fun refreshGamesEvents(): Observable<RefreshGamesEvent>

  fun setLoadingIndicator(active: Boolean)

  fun setDateNavigatorText()

  fun hideGames()

  fun showGames(games: List<GameV2>)

  fun showGameDetails(game: GameV2)

  fun setNoGamesIndicator(active: Boolean)

  fun showNoNetSnackbar()

  fun showErrorSnackbar(code: Int)

  fun dismissSnackbar()

  fun getCurrentDateShown(): Calendar
}
