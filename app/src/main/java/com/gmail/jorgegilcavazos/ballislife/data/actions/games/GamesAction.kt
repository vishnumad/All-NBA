package com.gmail.jorgegilcavazos.ballislife.data.actions.games

import com.gmail.jorgegilcavazos.ballislife.features.model.GameV2
import java.util.*

/**
 * Actions related to the loading of NBA Games.
 */
sealed class GamesAction {
  /**
   * Action used to load a list of games of a given date.
   */
  data class LoadGamesAction(val date: Calendar, val forceReload: Boolean) : GamesAction()

  /**
   * Action used to open the detail of a particular game.
   */
  data class OpenGameAction(val game: GameV2) : GamesAction()
}