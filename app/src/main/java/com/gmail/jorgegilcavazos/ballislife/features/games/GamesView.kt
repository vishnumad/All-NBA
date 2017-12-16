package com.gmail.jorgegilcavazos.ballislife.features.games

import com.gmail.jorgegilcavazos.ballislife.features.games.GamesUiEvent.DateSelectedEvent
import com.gmail.jorgegilcavazos.ballislife.features.model.GameV2
import io.reactivex.Observable

interface GamesView {

  fun prevDayClicks(): Observable<Any>

  fun nextDayClicks(): Observable<Any>

  fun gameClicks(): Observable<GameV2>

  fun dateSelectedUiEvents(): Observable<DateSelectedEvent>

  fun setLoadingIndicator(active: Boolean)

  fun setDateNavigatorText(dateText: String)

  fun hideGames()

  fun showGames(games: List<GameV2>)

  fun showGameDetails(game: GameV2, selectedDate: Long)

  fun setNoGamesIndicator(active: Boolean)

  fun showNoNetSnackbar()

  fun showErrorSnackbar(code: Int)

  fun dismissSnackbar()
}
