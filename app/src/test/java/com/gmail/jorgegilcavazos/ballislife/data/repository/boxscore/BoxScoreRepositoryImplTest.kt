package com.gmail.jorgegilcavazos.ballislife.data.repository.boxscore

import com.gmail.jorgegilcavazos.ballislife.data.service.NbaGamesService
import com.gmail.jorgegilcavazos.ballislife.features.model.BoxScoreTeam
import com.gmail.jorgegilcavazos.ballislife.features.model.BoxScoreValues
import com.gmail.jorgegilcavazos.ballislife.util.schedulers.TrampolineSchedulerProvider
import io.reactivex.Single
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class BoxScoreRepositoryImplTest {

	@Mock private lateinit var mockNbaGamesService: NbaGamesService

	private lateinit var repository: BoxScoreRepositoryImpl

	@Before
	fun setup() {
		MockitoAnnotations.initMocks(this)

		repository = BoxScoreRepositoryImpl(mockNbaGamesService, TrampolineSchedulerProvider())
	}

	@Test
	fun loadBoxScoreCacheAvailableDontForceNetwork() {
		`when`(mockNbaGamesService.boxScore("GAME_ID"))
				.thenReturn(Single.error(Exception()))
		val boxScore = BoxScoreValues(BoxScoreTeam(listOf(), 0), BoxScoreTeam(listOf(), 0))
		repository.saveBoxScoreInCache("GAME_ID", boxScore)

		val testObserver = repository.boxScore("GAME_ID", false).test()

		testObserver.assertValueCount(2)
		testObserver.assertValueAt(0, { it.inProgress })
		testObserver.assertValueAt(1, { it.success && it.boxScoreValues == boxScore })
	}

	@Test
	fun loadBoxScoreCacheNotAvailableDontForceNetwork() {
		val boxScore = BoxScoreValues(BoxScoreTeam(listOf(), 0), BoxScoreTeam(listOf(), 0))
		`when`(mockNbaGamesService.boxScore("GAME_ID")).thenReturn(Single.just(boxScore))

		val testObserver = repository.boxScore("GAME_ID", false).test()

		testObserver.assertValueCount(3)
		testObserver.assertValueAt(0, { it.inProgress })
		testObserver.assertValueAt(1, { it.inProgress })
		testObserver.assertValueAt(2, { it.success && it.boxScoreValues == boxScore })
	}

	@Test
	fun loadBoxScoreForceNetworkSuccess() {
		val boxScore = BoxScoreValues(BoxScoreTeam(listOf(), 0), BoxScoreTeam(listOf(), 0))
		`when`(mockNbaGamesService.boxScore("GAME_ID")).thenReturn(Single.just(boxScore))

		val testObserver = repository.boxScore("GAME_ID", true).test()

		testObserver.assertValueCount(2)
		testObserver.assertValueAt(0, { it.inProgress })
		testObserver.assertValueAt(1, { it.success && it.boxScoreValues == boxScore })
	}

	@Test
	fun loadBoxScoreForceNetworkNotAvailable() {
		`when`(mockNbaGamesService.boxScore("GAME_ID")).thenReturn(Single.error(Exception()))

		val testObserver = repository.boxScore("GAME_ID", true).test()

		testObserver.assertValueCount(2)
		testObserver.assertValueAt(0, { it.inProgress })
		testObserver.assertValueAt(1, { it.notAvailable })
	}
}