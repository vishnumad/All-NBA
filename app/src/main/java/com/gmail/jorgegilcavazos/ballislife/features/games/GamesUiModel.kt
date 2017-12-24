package com.gmail.jorgegilcavazos.ballislife.features.games

import com.gmail.jorgegilcavazos.ballislife.features.model.GameV2

/**
 * Deprecated. Use [GamesUiModelV2] instead.
 */
class GamesUiModel(
    val isMemoryInProgress: Boolean,
    val isMemorySuccess: Boolean,
    val isNetworkInProgress: Boolean,
    val isNetworkSuccess: Boolean,
    val games: List<GameV2>) {

  companion object {
    fun memoryInProgress(): GamesUiModel =
        GamesUiModel(
            isMemoryInProgress = true,
            isMemorySuccess = false,
            isNetworkInProgress = false,
            isNetworkSuccess = false,
            games = emptyList())

    fun memorySuccess(games: List<GameV2>): GamesUiModel =
        GamesUiModel(
            isMemoryInProgress = false,
            isMemorySuccess = true,
            isNetworkInProgress = false,
            isNetworkSuccess = false,
            games = games)

    fun networkInProgress(): GamesUiModel =
        GamesUiModel(
            isMemoryInProgress = false,
            isMemorySuccess = false,
            isNetworkInProgress = true,
            isNetworkSuccess = false,
            games = emptyList())

    fun networkSuccess(games: List<GameV2>): GamesUiModel =
        GamesUiModel(
            isMemoryInProgress = false,
            isMemorySuccess = false,
            isNetworkInProgress = false,
            isNetworkSuccess = true,
            games = games)
  }
}
