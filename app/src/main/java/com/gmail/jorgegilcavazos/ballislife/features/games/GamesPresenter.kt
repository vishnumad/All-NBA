package com.gmail.jorgegilcavazos.ballislife.features.games

import com.gmail.jorgegilcavazos.ballislife.base.BasePresenter
import com.gmail.jorgegilcavazos.ballislife.data.repository.games.GamesRepository
import com.gmail.jorgegilcavazos.ballislife.features.model.GameV2
import com.gmail.jorgegilcavazos.ballislife.util.DateFormatUtil
import com.gmail.jorgegilcavazos.ballislife.util.GameUtils
import com.gmail.jorgegilcavazos.ballislife.util.NetworkUtils
import com.gmail.jorgegilcavazos.ballislife.util.schedulers.BaseSchedulerProvider
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import java.util.*
import javax.inject.Inject

class GamesPresenter @Inject constructor(
    private val gamesRepository: GamesRepository,
    private val schedulerProvider: BaseSchedulerProvider) : BasePresenter<GamesView>() {

  private val disposables = CompositeDisposable()

  fun onDateChanged() {
    view.hideGames()
  }

  fun loadModels(selectedDate: Calendar, forceNetwork: Boolean) {
    view.dismissSnackbar()
    loadDateNavigatorText(selectedDate)

    disposables.clear()
    disposables.add(gamesRepository.models(selectedDate, forceNetwork)
        .observeOn(schedulerProvider.ui(), true)
        .subscribeWith(object : DisposableObserver<GamesUiModel>() {
          override fun onNext(uiModel: GamesUiModel) {
            if (uiModel.isMemorySuccess && uiModel.games
                .isEmpty()) {
              view.setLoadingIndicator(true)
            }

            if (uiModel.isNetworkSuccess) {
              view.setLoadingIndicator(false)
            }

            if (!uiModel.games.isEmpty()) {
              view.showGames(uiModel.games)
            }

            view.setNoGamesIndicator(uiModel.isNetworkSuccess && uiModel
                .games
                .isEmpty())
          }

          override fun onError(e: Throwable) {
            view.setLoadingIndicator(false)
            if (NetworkUtils.isNetworkAvailable()) {
              view.showErrorSnackbar()
            } else {
              view.showNoNetSnackbar()
            }
          }

          override fun onComplete() {

          }
        }))
  }

  private fun loadDateNavigatorText(selectedDate: Calendar) {
    val dateText = DateFormatUtil.formatNavigatorDate(selectedDate.time)
    view.setDateNavigatorText(dateText)
  }

  fun openGameDetails(requestedGame: GameV2, selectedDate: Calendar) {
    view.showGameDetails(requestedGame, selectedDate)
  }

  fun updateGames(gameData: String, selectedDate: Calendar) {
    if (DateFormatUtil.isDateToday(selectedDate.time)) {
      view.updateScores(GameUtils.getGamesListFromJson(gameData))
    }
  }

  fun stop() {
    disposables.clear()
    view.dismissSnackbar()
  }

}
