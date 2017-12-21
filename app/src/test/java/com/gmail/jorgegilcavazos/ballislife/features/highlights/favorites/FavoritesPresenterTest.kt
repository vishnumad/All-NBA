package com.gmail.jorgegilcavazos.ballislife.features.highlights.favorites

import com.gmail.jorgegilcavazos.ballislife.data.repository.highlights.FavoritesRepository
import com.gmail.jorgegilcavazos.ballislife.features.model.Highlight
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.disposables.CompositeDisposable
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

/**
 * Tests for the [FavoritesPresenter].
 */
@RunWith(MockitoJUnitRunner::class)
class FavoritesPresenterTest {

  @Mock private lateinit var mockView: FavoritesView
  @Mock private lateinit var mockFavoritesRepository: FavoritesRepository

  private val favoriteClicks = PublishRelay.create<Highlight>()
  private val favoriteDeletions = PublishRelay.create<Highlight>()
  private val openHighlightEvents = PublishRelay.create<Highlight>()
  private val openSubmissionEvents = PublishRelay.create<Highlight>()
  private val shareHighlightEvents = PublishRelay.create<Highlight>()
  private val favorites = PublishRelay.create<Highlight>()
  private val newlyAddedFavorites = PublishRelay.create<Highlight>()

  private lateinit var presenter: FavoritesPresenter

  @Before
  fun setup() {
    MockitoAnnotations.initMocks(this)

    `when`(mockView.favoriteClicks()).thenReturn(favoriteClicks)
    `when`(mockView.favoriteDeletions()).thenReturn(favoriteDeletions)
    `when`(mockView.openHighlightEvents()).thenReturn(openHighlightEvents)
    `when`(mockView.shareHighlightEvents()).thenReturn(shareHighlightEvents)
    `when`(mockView.openSubmissionEvents()).thenReturn(openSubmissionEvents)
    `when`(mockFavoritesRepository.favorites()).thenReturn(favorites)
    `when`(mockFavoritesRepository.newlyAddedFavorites()).thenReturn(newlyAddedFavorites)

    presenter = FavoritesPresenter(mockFavoritesRepository, CompositeDisposable())
  }

  @Test
  fun showConfirmationDialogIfFavorite() {
    val highlight = Highlight()
    `when`(mockView.isPremium()).thenReturn(true)
    presenter.attachView(mockView)

    favoriteClicks.accept(highlight)

    verify(mockView).showRemoveFromFavoritesConfirmation(highlight)
  }

  @Test
  fun removeHighlightFromFavoritesOnDeletion() {
    val highlight = Highlight()
    `when`(mockView.isPremium()).thenReturn(true)
    presenter.attachView(mockView)

    favoriteDeletions.accept(highlight)

    verify(mockView).removeHighlight(highlight)
    verify(mockFavoritesRepository).removeFromFavorites(highlight)
  }

  @Test
  fun openStreamableOnOpenEvent() {
    val highlight = Highlight(url = "streamable.com/abcd")
    `when`(mockView.isPremium()).thenReturn(true)
    presenter.attachView(mockView)

    openHighlightEvents.accept(highlight)

    verify(mockView).openStreamable("abcd")
  }

  @Test
  fun openYoutubeOnOpenEvent() {
    val highlight = Highlight(url = "youtube.com?v=abcd")
    `when`(mockView.isPremium()).thenReturn(true)
    presenter.attachView(mockView)

    openHighlightEvents.accept(highlight)

    verify(mockView).openYoutubeVideo("abcd")
  }

  @Test
  fun showInvalidStreamableOnBadSource() {
    val highlight = Highlight(url = "streamable.com")
    `when`(mockView.isPremium()).thenReturn(true)
    presenter.attachView(mockView)

    openHighlightEvents.accept(highlight)

    verify(mockView).showErrorOpeningStreamable()
  }

  @Test
  fun showInvalidYoutubeOnBadSource() {
    val highlight = Highlight(url = "youtube.net")
    `when`(mockView.isPremium()).thenReturn(true)
    presenter.attachView(mockView)

    openHighlightEvents.accept(highlight)

    verify(mockView).showErrorOpeningYoutube()
  }

  @Test
  fun showInvalidSourceIfNotStreamableOrYoutube() {
    val highlight = Highlight(url = "instagram.com?v=50")
    `when`(mockView.isPremium()).thenReturn(true)
    presenter.attachView(mockView)

    openHighlightEvents.accept(highlight)

    verify(mockView).showUnknownSourceError()
  }

  @Test
  fun showSubmissionOnOpenEvent() {
    val highlight = Highlight()
    `when`(mockView.isPremium()).thenReturn(true)
    presenter.attachView(mockView)

    openSubmissionEvents.accept(highlight)

    verify(mockView).showSubmission(highlight)
  }

  @Test
  fun showShareDialogOnShareEvent() {
    val highlight = Highlight()
    `when`(mockView.isPremium()).thenReturn(true)
    presenter.attachView(mockView)

    shareHighlightEvents.accept(highlight)

    verify(mockView).showShareDialog(highlight)
  }

  @Test
  fun addHighlightOnRepositoryPush() {
    val highlight = Highlight()
    `when`(mockView.isPremium()).thenReturn(true)
    presenter.attachView(mockView)

    favorites.accept(highlight)

    verify(mockView).addHighlight(highlight, addToTop = false)
  }

  @Test
  fun addHighlightOnNewlyAddedPush() {
    val highlight = Highlight()
    `when`(mockView.isPremium()).thenReturn(true)
    presenter.attachView(mockView)

    newlyAddedFavorites.accept(highlight)

    verify(mockView).addHighlight(highlight, addToTop = true)
  }
}