package com.gmail.jorgegilcavazos.ballislife.features.games

import com.gmail.jorgegilcavazos.ballislife.features.model.GameV2

class GamesUiModel(
    val isMemoryInProgress: Boolean,
    val isMemorySuccess: Boolean,
    val isNetworkInProgress: Boolean,
    val isNetworkSuccess: Boolean,
    val games: List<GameV2>) {

  companion object {
    fun memoryInProgress(): GamesUiModel {
      return GamesUiModel(true, false, false, false, emptyList())
    }

    fun memorySuccess(games: List<GameV2>): GamesUiModel {
      return GamesUiModel(false, true, false, false, games)
    }

    fun networkInProgress(): GamesUiModel {
      return GamesUiModel(false, false, true, false, emptyList())
    }

    fun networkSuccess(games: List<GameV2>): GamesUiModel {
      return GamesUiModel(false, false, false, true, games)
    }
  }
}
