package com.gmail.jorgegilcavazos.ballislife.data.repository.boxscore

import android.support.annotation.VisibleForTesting
import com.gmail.jorgegilcavazos.ballislife.data.firebase.remoteconfig.RemoteConfig
import com.gmail.jorgegilcavazos.ballislife.data.service.NbaGamesService
import com.gmail.jorgegilcavazos.ballislife.data.service.NbaService
import com.gmail.jorgegilcavazos.ballislife.features.boxscore.BoxScoreUIModel
import com.gmail.jorgegilcavazos.ballislife.features.model.BoxScoreResponse
import com.gmail.jorgegilcavazos.ballislife.util.Constants
import com.gmail.jorgegilcavazos.ballislife.util.schedulers.BaseSchedulerProvider
import com.google.common.base.Optional
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class BoxScoreRepositoryImpl @Inject constructor(
		private val nbaGamesService: NbaGamesService,
		private val nbaService: NbaService,
		private val schedulerProvider: BaseSchedulerProvider,
    private val remoteConfig: RemoteConfig) : BoxScoreRepository {

	private val boxScoreMap = HashMap<String, BoxScoreResponse>()

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

	private fun memorySource(gameId: String): Single<Optional<BoxScoreResponse>> {
		return Single.just(Optional.fromNullable(boxScoreMap[gameId]))
	}

	private fun networkSource(gameId: String): Single<Optional<BoxScoreResponse>> {
    val source = if (remoteConfig.getBoolean(Constants.USE_SWISH_BACKEND_BOX_SCORE)) {
      nbaGamesService.boxScore(gameId)
    } else {
      nbaService.boxScoreNba(gameId)
    }

		return source
				.doOnSuccess { boxScoreMap[gameId] = it }
				.map { Optional.of(it) }
				.onErrorReturn { Optional.absent<BoxScoreResponse>() }
	}

	@VisibleForTesting
	fun saveBoxScoreInCache(gameId: String, boxScore: BoxScoreResponse) {
		boxScoreMap[gameId] = boxScore
	}
}