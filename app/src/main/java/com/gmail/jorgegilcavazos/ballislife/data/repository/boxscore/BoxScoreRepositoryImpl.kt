package com.gmail.jorgegilcavazos.ballislife.data.repository.boxscore

import android.support.annotation.VisibleForTesting
import com.gmail.jorgegilcavazos.ballislife.data.service.NbaGamesService
import com.gmail.jorgegilcavazos.ballislife.features.boxscore.BoxScoreUIModel
import com.gmail.jorgegilcavazos.ballislife.features.model.BoxScoreValues
import com.gmail.jorgegilcavazos.ballislife.util.schedulers.BaseSchedulerProvider
import com.google.common.base.Optional
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BoxScoreRepositoryImpl @Inject constructor(
		private val nbaGamesService: NbaGamesService,
		private val schedulerProvider: BaseSchedulerProvider) : BoxScoreRepository {

	private val boxScoreMap = HashMap<String, BoxScoreValues>()

	override fun boxScore(gameId: String, forceNetwork: Boolean): Observable<BoxScoreUIModel> {
		val network = networkSource(gameId)
				.toObservable()
				.concatMap {
					if (it.isPresent) {
						Observable.just(BoxScoreUIModel.success(it.get()))
					} else {
						Observable.just(BoxScoreUIModel.notAvailable())
					}
				}
				.subscribeOn(schedulerProvider.io())
				.observeOn(schedulerProvider.ui())
				.startWith(BoxScoreUIModel.inProgress())

		val memory = memorySource(gameId)
				.toObservable()
				.concatMap {
					if (it.isPresent) {
						Observable.just(BoxScoreUIModel.success(it.get()))
					} else {
						network
					}
				}
				.subscribeOn(schedulerProvider.io())
				.observeOn(schedulerProvider.ui())
				.startWith(BoxScoreUIModel.inProgress())

		return if (forceNetwork) {
			network
		} else {
			memory
		}
	}

	private fun memorySource(gameId: String): Single<Optional<BoxScoreValues>> {
		return Single.just(Optional.fromNullable(boxScoreMap[gameId]))
	}

	private fun networkSource(gameId: String): Single<Optional<BoxScoreValues>> {
		return nbaGamesService.boxScore(gameId)
				.doOnSuccess { boxScoreMap[gameId] = it }
				.map { Optional.of(it) }
				.onErrorReturn { Optional.absent<BoxScoreValues>() }
	}

	@VisibleForTesting
	fun saveBoxScoreInCache(gameId: String, boxScore: BoxScoreValues) {
		boxScoreMap[gameId] = boxScore
	}
}