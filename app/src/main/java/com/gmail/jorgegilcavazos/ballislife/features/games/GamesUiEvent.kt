package com.gmail.jorgegilcavazos.ballislife.features.games

import java.util.*

sealed class GamesUiEvent {
  data class DateSelectedEvent(val date: Calendar) : GamesUiEvent()
}