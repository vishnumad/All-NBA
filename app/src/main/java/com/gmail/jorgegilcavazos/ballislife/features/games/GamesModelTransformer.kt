package com.gmail.jorgegilcavazos.ballislife.features.games

import com.gmail.jorgegilcavazos.ballislife.data.actions.games.GamesAction
import com.gmail.jorgegilcavazos.ballislife.data.actions.games.GamesActions
import com.gmail.jorgegilcavazos.ballislife.data.actions.games.GamesResult
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import javax.inject.Inject

/**
 * Helper class used to model the state of the Games screen based on a set of UI events.
 */
class GamesModelTransformer @Inject constructor(private val gamesActions: GamesActions) {

  /**
   * Turns a stream of [GamesUiEvent]s into a stream of [GamesUiModelV2]s representing the state
   * of the games view.
   */
  fun uiModels(uiEvents: Observable<GamesUiEvent>): Observable<GamesUiModelV2> {

    // Transforms ui events into actions.
    val actions = ObservableTransformer<GamesUiEvent, GamesAction> {
      it.publish { shared ->
        Observable.merge(
            shared.ofType(GamesUiEvent.LoadGamesEvent::class.java)
                .compose { it.map { event -> GamesAction.LoadGamesAction(event.date, false) } },
            shared.ofType(GamesUiEvent.RefreshGamesEvent::class.java)
                .compose { it.map { event -> GamesAction.LoadGamesAction(event.date, true) } },
            shared.ofType(GamesUiEvent.OpenGameEvent::class.java)
                .compose { it.map { event -> GamesAction.OpenGameAction(event.game) } }
        )
      }
    }

    return gamesActions
        .results(uiEvents.compose(actions)) // Obtain the results of the events turned into actions.
        .scan(GamesUiModelV2.Idle as GamesUiModelV2, { _, result ->
          // Map the results of the actions to a model representing the games screen state.
          when (result) {
            is GamesResult.LoadGamesResult.MemoryInProgress -> GamesUiModelV2.MemoryInProgress
            is GamesResult.LoadGamesResult.NetworkInProgress -> GamesUiModelV2.NetworkInProgress
            is GamesResult.LoadGamesResult.Success -> {
              GamesUiModelV2.Success(result.games, result.date)
            }
            is GamesResult.LoadGamesResult.NoCachedGames -> GamesUiModelV2.NoCachedGames
            is GamesResult.LoadGamesResult.NoGames -> GamesUiModelV2.NoGames
            is GamesResult.LoadGamesResult.Failure -> GamesUiModelV2.Failure(result.t)
            is GamesResult.OpenGameResult -> GamesUiModelV2.OpenGameScreen(result.game)
          }
        })
  }
}