package com.gmail.jorgegilcavazos.ballislife.features.games

import com.gmail.jorgegilcavazos.ballislife.features.model.GameV2
import java.util.*

sealed class GamesUiEvent {
  data class DateSelectedEvent(val date: Calendar) : GamesUiEvent()
  data class LoadGamesEvent(val date: Calendar) : GamesUiEvent()
  data class RefreshGamesEvent(val date: Calendar) : GamesUiEvent()
  data class OpenGameEvent(val game: GameV2) : GamesUiEvent()
}