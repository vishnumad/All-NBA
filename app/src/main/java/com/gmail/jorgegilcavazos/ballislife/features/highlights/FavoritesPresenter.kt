package com.gmail.jorgegilcavazos.ballislife.features.highlights

import com.gmail.jorgegilcavazos.ballislife.base.BasePresenter
import com.gmail.jorgegilcavazos.ballislife.data.repository.highlights.FavoritesRepository
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

/**
 * Presenter for the favorite highlights screen.
 */
class FavoritesPresenter @Inject constructor(
    private val favoritesRepository: FavoritesRepository,
    private val disposables: CompositeDisposable
) : BasePresenter<FavoritesView>() {

  override fun attachView(view: FavoritesView) {
    super.attachView(view)

    if (!view.isPremium()) {
      return
    }

    view.favoriteClicks()
        .subscribe { view.showRemoveFromFavoritesConfirmation(it) }
        .addTo(disposables)

    view.favoriteDeletions()
        .subscribe {
          view.removeHighlight(it)
          favoritesRepository.removeFromFavorites(it)
              .subscribe({
                // Save successful
              }, { _ ->
                // Save unsuccessful
              }).addTo(disposables)
        }
        .addTo(disposables)

    favoritesRepository.favorites()
        .subscribe { view.addHighlight(it, false) }
        .addTo(disposables)

    favoritesRepository.newlyAddedFavorites()
        .subscribe { view.addHighlight(it, true) }
        .addTo(disposables)
  }

  fun loadMore() {
    favoritesRepository.loadMore()
  }

  override fun detachView() {
    disposables.clear()
    super.detachView()
  }
}