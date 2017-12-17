package com.gmail.jorgegilcavazos.ballislife.data.actions.games

import com.gmail.jorgegilcavazos.ballislife.data.actions.games.GamesAction.LoadGamesAction
import com.gmail.jorgegilcavazos.ballislife.data.actions.games.GamesAction.OpenGameAction
import com.gmail.jorgegilcavazos.ballislife.data.actions.games.GamesResult.LoadGamesResult
import com.gmail.jorgegilcavazos.ballislife.data.actions.games.GamesResult.OpenGameResult
import com.gmail.jorgegilcavazos.ballislife.data.repository.games.GamesRepository
import com.gmail.jorgegilcavazos.ballislife.util.schedulers.BaseSchedulerProvider
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import javax.inject.Inject

/**
 * Transforms [GamesAction]s into [GamesResult]s.
 */
class GamesActions @Inject constructor(
    private val gamesRepository: GamesRepository,
    private val schedulerProvider: BaseSchedulerProvider) {

  fun results(actions: Observable<GamesAction>): Observable<GamesResult> {
    val loadGamesTransformer = ObservableTransformer<LoadGamesAction, LoadGamesResult> {
      it.flatMap { action -> gamesRepository.loadGames(action.date, action.forceReload) }
    }

    val openGameTransformer = ObservableTransformer<OpenGameAction, OpenGameResult> {
      it.map { action -> OpenGameResult(action.game) }.observeOn(schedulerProvider.ui())
    }

    val actionTransformer = ObservableTransformer<GamesAction, GamesResult> {
      it.publish { shared ->
        Observable.merge(
            shared.ofType(LoadGamesAction::class.java).compose(loadGamesTransformer),
            shared.ofType(OpenGameAction::class.java).compose(openGameTransformer))
      }
    }

    return actions.compose(actionTransformer)
  }
}