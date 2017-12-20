package com.gmail.jorgegilcavazos.ballislife.data.repository.highlights

import com.gmail.jorgegilcavazos.ballislife.features.model.Highlight
import io.reactivex.Completable
import io.reactivex.Observable

/**
 * Repository for the favorite highlights of a user.
 */
interface FavoritesRepository {

  fun newlyAddedFavorites(): Observable<Highlight>

  fun favorites(): Observable<Highlight>

  fun loadMore()

  fun saveToFavorites(highlight: Highlight): Completable

  fun removeFromFavorites(highlight: Highlight): Completable

}