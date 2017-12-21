package com.gmail.jorgegilcavazos.ballislife.features.highlights.favorites

import com.gmail.jorgegilcavazos.ballislife.features.model.Highlight
import io.reactivex.Observable

interface FavoritesView {

  fun addHighlight(highlight: Highlight, addToTop: Boolean)

  fun removeHighlight(highlight: Highlight)

  fun favoriteClicks(): Observable<Highlight>

  fun showRemoveFromFavoritesConfirmation(highlight: Highlight)

  fun favoriteDeletions(): Observable<Highlight>

  fun isPremium(): Boolean
}