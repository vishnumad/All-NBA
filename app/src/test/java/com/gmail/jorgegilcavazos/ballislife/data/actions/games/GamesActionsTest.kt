package com.gmail.jorgegilcavazos.ballislife.data.actions.games

import com.gmail.jorgegilcavazos.ballislife.data.actions.games.GamesAction.LoadGamesAction
import com.gmail.jorgegilcavazos.ballislife.data.actions.games.GamesAction.OpenGameAction
import com.gmail.jorgegilcavazos.ballislife.data.repository.games.GamesRepository
import com.gmail.jorgegilcavazos.ballislife.features.model.GameV2
import com.gmail.jorgegilcavazos.ballislife.util.schedulers.TrampolineSchedulerProvider
import io.reactivex.Observable
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import java.util.*

/**
 * Tests for the [GamesActions].
 */
@RunWith(MockitoJUnitRunner::class)
class GamesActionsTest {

  @Mock private lateinit var mockGamesRepository: GamesRepository

  private lateinit var gamesActions: GamesActions

  @Before
  fun setup() {
    MockitoAnnotations.initMocks(this)

    gamesActions = GamesActions(mockGamesRepository,TrampolineSchedulerProvider())
  }

  @Test
  fun loadGamesActionResult() {
    val games = listOf(createGameV2(), createGameV2(), createGameV2())
    Mockito.`when`(mockGamesRepository.loadGames(anyObject(), ArgumentMatchers.anyBoolean()))
        .thenReturn(Observable.just(
            GamesResult.LoadGamesResult.MemoryInProgress,
            GamesResult.LoadGamesResult.Success(games, Calendar.getInstance())))

    val testObserver = gamesActions.results(
        Observable.just(LoadGamesAction(Calendar.getInstance(), false))).test()

    testObserver.assertValueCount(2)
    testObserver.assertValueAt(0, {it is GamesResult.LoadGamesResult.MemoryInProgress})
    testObserver.assertValueAt(1, {it is GamesResult.LoadGamesResult.Success})
  }

  @Test
  fun openGameActionResult() {
    val game = createGameV2()
    val testObserver = gamesActions.results(
        Observable.just(OpenGameAction(game))).test()

    testObserver.assertValueCount(1)
    testObserver.assertValueAt(0, {it is GamesResult.OpenGameResult})
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
        totalPeriods = "",
        broadcasters = mapOf())
  }

  private fun <T> anyObject(): T = Mockito.anyObject<T>()
}