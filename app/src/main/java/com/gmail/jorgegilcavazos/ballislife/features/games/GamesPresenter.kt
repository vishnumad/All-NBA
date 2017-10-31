package com.gmail.jorgegilcavazos.ballislife.features.games

import com.gmail.jorgegilcavazos.ballislife.base.BasePresenter
import com.gmail.jorgegilcavazos.ballislife.data.repository.games.GamesRepository
import com.gmail.jorgegilcavazos.ballislife.util.DateFormatUtil
import com.gmail.jorgegilcavazos.ballislife.util.ErrorHandler
import com.gmail.jorgegilcavazos.ballislife.util.NetworkUtils
import com.gmail.jorgegilcavazos.ballislife.util.schedulers.BaseSchedulerProvider
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.rxkotlin.addTo
import java.util.*
import javax.inject.Inject

class GamesPresenter @Inject constructor(
    private val gamesRepository: GamesRepository,
    private val schedulerProvider: BaseSchedulerProvider,
    private val disposables: CompositeDisposable,
    private val gamesDisposable: CompositeDisposable,
    private val networkUtils: NetworkUtils,
    private val errorHandler: ErrorHandler) : BasePresenter<GamesView>() {

  private val calendar = Calendar.getInstance()

  override fun attachView(view: GamesView) {
    super.attachView(view)

    view.prevDayClicks()
        .subscribe { _ ->
          calendar.add(Calendar.DAY_OF_YEAR, -1)
          view.hideGames()
          loadGames()
        }
        .addTo(disposables)

    view.nextDayClicks()
        .subscribe { _ ->
          calendar.add(Calendar.DAY_OF_YEAR, 1)
          view.hideGames()
          loadGames()
        }
        .addTo(disposables)

    view.gameClicks()
        .subscribe { view.showGameDetails(it, calendar.time.time) }
        .addTo(disposables)
  }

  fun loadGames(forceNetwork: Boolean = false) {
    view.dismissSnackbar()
    loadDateNavigatorText(calendar)

    gamesDisposable.clear()
    gamesRepository.games(calendar, forceNetwork)
        .observeOn(schedulerProvider.ui(), true)
        .subscribeWith(object : DisposableObserver<GamesUiModel>() {
          override fun onNext(uiModel: GamesUiModel) {
            if (uiModel.isMemorySuccess && uiModel.games.isEmpty()) {
              view.setLoadingIndicator(true)
            }

            if (uiModel.isNetworkSuccess) {
              view.setLoadingIndicator(false)
            }

            if (!uiModel.games.isEmpty()) {
              view.showGames(uiModel.games)
            }

            view.setNoGamesIndicator(uiModel.isNetworkSuccess && uiModel.games.isEmpty())
          }

          override fun onError(e: Throwable) {
            view.setLoadingIndicator(false)
            if (networkUtils.isNetworkAvailable()) {
              view.showErrorSnackbar(errorHandler.handleError(e))
            } else {
              view.showNoNetSnackbar()
            }
          }

          override fun onComplete() {

          }
        })
        .addTo(gamesDisposable)
  }

  private fun loadDateNavigatorText(selectedDate: Calendar) {
    val dateText = DateFormatUtil.formatNavigatorDate(selectedDate.time)
    view.setDateNavigatorText(dateText)
  }

  fun getSelectedDate(): Long = calendar.time.time

  fun setSelectedDate(millis: Long) {
    calendar.timeInMillis = millis
  }

  override fun detachView() {
    disposables.clear()
    view.dismissSnackbar()
    super.detachView()
  }

}
