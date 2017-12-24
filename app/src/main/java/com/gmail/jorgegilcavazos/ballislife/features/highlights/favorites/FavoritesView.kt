package com.gmail.jorgegilcavazos.ballislife.features.highlights.favorites

import com.gmail.jorgegilcavazos.ballislife.features.model.Highlight
import com.gmail.jorgegilcavazos.ballislife.features.model.SwishCard
import io.reactivex.Observable

interface FavoritesView {

  fun addHighlight(highlight: Highlight, addToTop: Boolean)

  fun removeHighlight(highlight: Highlight)

  fun favoriteClicks(): Observable<Highlight>

  fun showRemoveFromFavoritesConfirmation(highlight: Highlight)

  fun favoriteDeletions(): Observable<Highlight>

  fun isPremium(): Boolean

  fun openHighlightEvents(): Observable<Highlight>

  fun shareHighlightEvents(): Observable<Highlight>

  fun openSubmissionEvents(): Observable<Highlight>

  fun openStreamable(shortCode: String)

  fun showErrorOpeningStreamable()

  fun openYoutubeVideo(videoId: String)

  fun showErrorOpeningYoutube()

  fun showUnknownSourceError()

  fun showSubmission(highlight: Highlight)

  fun showShareDialog(highlight: Highlight)

  fun swishCardExploreClicks() : Observable<SwishCard>

  fun swishCardGotItClicks(): Observable<SwishCard>

  fun dismissSwishCard(swishCard: SwishCard)

  fun openPremiumActivity()
}