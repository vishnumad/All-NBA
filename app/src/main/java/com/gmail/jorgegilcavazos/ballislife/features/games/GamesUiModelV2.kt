package com.gmail.jorgegilcavazos.ballislife.features.games

import com.gmail.jorgegilcavazos.ballislife.features.model.GameV2
import java.util.*

/**
 * Models used to represent the state of the Games UI.
 */
sealed class GamesUiModelV2 {
  /**
   * Starting state, no actions needed.
   */
  object Idle: GamesUiModelV2()

  /**
   * Games are being loaded from the cache.
   */
  object MemoryInProgress : GamesUiModelV2()

  /**
   * Games are being loaded from the internet.
   */
  object NetworkInProgress: GamesUiModelV2()

  /**
   * A list of games was successfully loaded.
   */
  data class Success(val games: List<GameV2>, val date: Calendar) : GamesUiModelV2()

  /**
   * The attempt to load games from the cache returned no games.
   */
  object NoCachedGames : GamesUiModelV2()

  /**
   * There are no games available to load.
   */
  object NoGames : GamesUiModelV2()

  /**
   * Something went wrong in the games screen.
   */
  data class Failure(val t: Throwable) : GamesUiModelV2()

  /**
   * The detail of a particular game must be shown.
   */
  data class OpenGameScreen(val game: GameV2) : GamesUiModelV2()
}