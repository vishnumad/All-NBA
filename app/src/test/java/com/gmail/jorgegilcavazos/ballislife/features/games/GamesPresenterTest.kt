package com.gmail.jorgegilcavazos.ballislife.features.games

import com.gmail.jorgegilcavazos.ballislife.data.repository.games.GamesRepository
import com.gmail.jorgegilcavazos.ballislife.features.model.GameV2
import com.gmail.jorgegilcavazos.ballislife.util.NetworkUtils
import com.gmail.jorgegilcavazos.ballislife.util.schedulers.TrampolineSchedulerProvider
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import java.util.*

@RunWith(MockitoJUnitRunner::class)
class GamesPresenterTest {

  @Mock private lateinit var mockView: GamesView
  @Mock private lateinit var mockRepository: GamesRepository
  @Mock private lateinit var mockNetworkUtils: NetworkUtils

  private val prevDayClicks = PublishSubject.create<Any>()
  private val nextDayClicks = PublishSubject.create<Any>()
  private val gameClicks = PublishSubject.create<GameV2>()

  private lateinit var presenter: GamesPresenter

  @Before
  fun setup() {
    `when`(mockView.prevDayClicks()).thenReturn(prevDayClicks)
    `when`(mockView.nextDayClicks()).thenReturn(nextDayClicks)
    `when`(mockView.gameClicks()).thenReturn(gameClicks)
    `when`(mockRepository.games(anyObject(), anyBoolean())).thenReturn(Observable.empty())

    presenter = GamesPresenter(mockRepository, TrampolineSchedulerProvider(), CompositeDisposable(),
        mockNetworkUtils)
    presenter.attachView(mockView)
  }

  @Test
  fun hideGamesOnDayNavigation() {
    prevDayClicks.onNext(Object())
    nextDayClicks.onNext(Object())

    verify(mockView, times(2)).hideGames()
  }

  @Test
  fun setNavigatorDateOnPreviousNav() {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.YEAR, 2017)
    calendar.set(Calendar.MONTH, Calendar.OCTOBER)
    calendar.set(Calendar.DAY_OF_MONTH, 11)
    presenter.setSelectedDate(calendar.timeInMillis)

    prevDayClicks.onNext(Object())

    verify(mockView).setDateNavigatorText("Tuesday, October 10")
  }

  @Test
  fun setNavigatorDateOnNextNav() {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.YEAR, 2017)
    calendar.set(Calendar.MONTH, Calendar.OCTOBER)
    calendar.set(Calendar.DAY_OF_MONTH, 11)
    presenter.setSelectedDate(calendar.timeInMillis)

    nextDayClicks.onNext(Object())

    verify(mockView).setDateNavigatorText("Thursday, October 12")
  }

  @Test
  fun openGameDetailOnClick() {
    val game = createGameV2()

    gameClicks.onNext(game)

    verify(mockView).showGameDetails(game, presenter.getSelectedDate())
  }

  @Test
  fun showLoadingIndicatorIfEmptyMemoryResult() {
    `when`(mockRepository.games(anyObject(), ArgumentMatchers.anyBoolean()))
        .thenReturn(Observable.just(GamesUiModel.memorySuccess(listOf())))

    presenter.loadGames(false)

    verify(mockView).setLoadingIndicator(true)
    verify(mockView).setNoGamesIndicator(false)
  }

  @Test
  fun hideLoadingIndicatorIfNetworkResult() {
    `when`(mockRepository.games(anyObject(), ArgumentMatchers.anyBoolean()))
        .thenReturn(Observable.just(GamesUiModel.networkSuccess(listOf(createGameV2()))))

    presenter.loadGames(false)

    verify(mockView).setLoadingIndicator(false)
    verify(mockView).setNoGamesIndicator(false)
  }

  @Test
  fun showGamesIfResultContainsGames() {
    val games = listOf(createGameV2())
    `when`(mockRepository.games(anyObject(), ArgumentMatchers.anyBoolean()))
        .thenReturn(Observable.just(GamesUiModel.networkSuccess(games)))

    presenter.loadGames(false)

    verify(mockView).setNoGamesIndicator(false)
    verify(mockView).showGames(games)
  }

  @Test
  fun showNoGamesIndicatorIfNetworkResultEmpty() {
    `when`(mockRepository.games(anyObject(), ArgumentMatchers.anyBoolean()))
        .thenReturn(Observable.just(GamesUiModel.networkSuccess(listOf())))

    presenter.loadGames(false)

    verify(mockView).setNoGamesIndicator(true)
  }

  @Test
  fun hideLoadingIndicatorOnError() {
    `when`(mockRepository.games(anyObject(), ArgumentMatchers.anyBoolean()))
        .thenReturn(Observable.error(Exception()))

    presenter.loadGames(false)

    verify(mockView).setLoadingIndicator(false)
  }

  @Test
  fun showNoNetworkSnackbarIfNetUnavailable() {
    `when`(mockNetworkUtils.isNetworkAvailable()).thenReturn(false)
    `when`(mockRepository.games(anyObject(), ArgumentMatchers.anyBoolean()))
        .thenReturn(Observable.error(Exception()))

    presenter.loadGames(false)

    verify(mockView).showNoNetSnackbar()
  }

  @Test
  fun showErrorSnackbarIfErrorAndNetAvailable() {
    `when`(mockNetworkUtils.isNetworkAvailable()).thenReturn(true)
    `when`(mockRepository.games(anyObject(), ArgumentMatchers.anyBoolean()))
        .thenReturn(Observable.error(Exception()))

    presenter.loadGames(false)

    verify(mockView).showErrorSnackbar()
  }

  @Test
  fun dismissSnackbarOnDetach() {
    presenter.detachView()

    verify(mockView).dismissSnackbar()
  }

  private fun createGameV2(): GameV2 {
    return GameV2(
        arena = "AT&T",
        awayTeamAbbr = "SAS",
        awayTeamCity = "San Antonio",
        awayTeamId = "1545334321",
        awayTeamKey = "SAS",
        awayTeamNickname = "Spurs",
        awayTeamScore = "110",
        city = "San Antonio",
        date = "20171110",
        gameClock = "",
        gameStatus = "",
        homeTeamAbbr = "",
        homeTeamCity = "",
        homeTeamId = "",
        homeTeamKey = "",
        homeTeamNickname = "",
        homeTeamScore = "",
        id = "",
        periodName = "",
        periodStatus = "",
        periodValue = "",
        time = "",
        timeUtc = 23894341,
        totalPeriods = "")
  }

  private fun <T> anyObject(): T {
    return Mockito.anyObject<T>()
  }
}
