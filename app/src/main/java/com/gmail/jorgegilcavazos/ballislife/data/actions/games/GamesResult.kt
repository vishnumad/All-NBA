package com.gmail.jorgegilcavazos.ballislife.data.actions.games

import com.gmail.jorgegilcavazos.ballislife.features.model.GameV2
import java.util.*

/**
 * Possible results of a [GamesAction].
 */
sealed class GamesResult {

  /**
   * Result of a [GamesAction.LoadGamesAction].
   */
  sealed class LoadGamesResult : GamesResult() {
    object MemoryInProgress : LoadGamesResult()
    object NetworkInProgress : LoadGamesResult()
    data class Success(val games: List<GameV2>, val date: Calendar) : LoadGamesResult()
    object NoCachedGames : LoadGamesResult()
    object NoGames : LoadGamesResult()
    data class Failure(val t: Throwable) : LoadGamesResult()
  }

  /**
   * Result of an [GamesAction.OpenGameAction].
   */
  data class OpenGameResult(val game: GameV2) : GamesResult()
}