package com.gmail.jorgegilcavazos.ballislife.features.games

import com.gmail.jorgegilcavazos.ballislife.base.BasePresenter
import com.gmail.jorgegilcavazos.ballislife.util.ErrorHandler
import com.gmail.jorgegilcavazos.ballislife.util.NetworkUtils
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.OnErrorNotImplementedException
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

class GamesPresenter @Inject constructor(
    private val gamesModelTransformer: GamesModelTransformer,
    private val disposables: CompositeDisposable,
    private val networkUtils: NetworkUtils,
    private val errorHandler: ErrorHandler) : BasePresenter<GamesView>() {

  override fun attachView(view: GamesView) {
    super.attachView(view)

    val uiEvents = Observable.merge(
        view.dateSelectionEvents(),
        view.loadGamesEvents(),
        view.refreshGamesEvents(),
        view.openGameEvents()
    )

    gamesModelTransformer.uiModels(uiEvents).subscribe({ model ->
      when (model) {
        is GamesUiModelV2.Idle -> {
          view.setNoGamesIndicator(false)
        }
        is GamesUiModelV2.MemoryInProgress -> {
          view.setDateNavigatorText()
          view.hideGames()
          view.setLoadingIndicator(false)
          view.setNoGamesIndicator(false)
          view.dismissSnackbar()
        }
        is GamesUiModelV2.NetworkInProgress -> {
          view.setDateNavigatorText()
          view.setNoGamesIndicator(false)
          view.dismissSnackbar()
        }
        is GamesUiModelV2.Success -> {
          if (view.getCurrentDateShown() == model.date) {
            view.showGames(model.games)
            view.setLoadingIndicator(false)
            view.setNoGamesIndicator(false)
          }
        }
        is GamesUiModelV2.NoCachedGames -> {
          view.setLoadingIndicator(true)
          view.setNoGamesIndicator(false)
        }
        is GamesUiModelV2.NoGames -> {
          view.setNoGamesIndicator(true)
          view.setLoadingIndicator(false)
        }
        is GamesUiModelV2.Failure -> {
          view.setLoadingIndicator(false)
          if (networkUtils.isNetworkAvailable()) {
            view.showErrorSnackbar(errorHandler.handleError(model.t))
          } else {
            view.showNoNetSnackbar()
          }
          view.setNoGamesIndicator(false)
        }
        is GamesUiModelV2.OpenGameScreen -> {
          view.showGameDetails(model.game)
        }
      }
    }, { t ->
      throw OnErrorNotImplementedException(t)
    }).addTo(disposables)
  }

  override fun detachView() {
    disposables.clear()
    view.dismissSnackbar()
    super.detachView()
  }

}
