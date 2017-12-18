package com.gmail.jorgegilcavazos.ballislife.data.repository.games

import android.annotation.SuppressLint
import android.support.annotation.VisibleForTesting
import com.gmail.jorgegilcavazos.ballislife.data.actions.games.GamesResult
import com.gmail.jorgegilcavazos.ballislife.data.actions.games.GamesResult.LoadGamesResult
import com.gmail.jorgegilcavazos.ballislife.data.service.NbaGamesService
import com.gmail.jorgegilcavazos.ballislife.features.games.GamesUiModel
import com.gmail.jorgegilcavazos.ballislife.features.model.GameV2
import com.gmail.jorgegilcavazos.ballislife.util.DateFormatUtil
import com.gmail.jorgegilcavazos.ballislife.util.schedulers.BaseSchedulerProvider
import io.reactivex.Observable
import io.reactivex.Single
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of the [GamesRepository] interface.
 */
@Singleton
class GamesRepositoryImpl @Inject constructor(
    private val gamesService: NbaGamesService,
    private val schedulerProvider: BaseSchedulerProvider) : GamesRepository {

  private val gamesMap = ConcurrentHashMap<String, GameV2>()

  override fun games(date: Calendar, forceNetwork: Boolean): Observable<GamesUiModel> {
    val network = networkSource(date).toObservable()
        .concatMap {
          if (it.isEmpty()) {
            Observable.just(GamesUiModel.networkSuccess(emptyList()))
          } else {
            Observable.just(GamesUiModel.networkSuccess(it.values.sortedBy { it.id }))
          }
        }
        .subscribeOn(schedulerProvider.io())
        .observeOn(schedulerProvider.ui())
        .startWith(GamesUiModel.networkInProgress())

    val memory = memorySource(date).toObservable()
        .concatMap {
          if (it.isEmpty()) {
            Observable.just(GamesUiModel.memorySuccess(emptyList()))
          } else {
            Observable.just(GamesUiModel.memorySuccess(it.values.sortedBy { it.id }))
          }
        }
        .subscribeOn(schedulerProvider.io())
        .observeOn(schedulerProvider.ui())
        .startWith(GamesUiModel.memoryInProgress())

    if (forceNetwork) {
      return network
    }

    return Observable.concat(memory, network)
  }

  override fun loadGames(
      date: Calendar,
      forceNetwork: Boolean): Observable<GamesResult.LoadGamesResult> {
    val network = networkSource(date).toObservable()
        .concatMap {
          if (it.isEmpty()) {
            Observable.just(LoadGamesResult.NoGames)
          } else {
            Observable.just(LoadGamesResult.Success(it.values.sortedBy { it.id }, date))
          }
        }
        .onErrorReturn { LoadGamesResult.Failure(it) }
        .subscribeOn(schedulerProvider.io())
        .observeOn(schedulerProvider.ui())
        .startWith(LoadGamesResult.NetworkInProgress)

    val memory = memorySource(date).toObservable()
        .concatMap {
          if (it.isNotEmpty()) {
            Observable.just(LoadGamesResult.Success(it.values.sortedBy { it.id }, date))
          } else {
            Observable.just(LoadGamesResult.NoCachedGames)
          }
        }
        .onErrorReturn { LoadGamesResult.Failure(it) }
        .subscribeOn(schedulerProvider.io())
        .observeOn(schedulerProvider.ui())
        .startWith(LoadGamesResult.MemoryInProgress)

    if (forceNetwork) {
      return network
    }

    return Observable.concat(memory, network)
  }

  private fun networkSource(date: Calendar): Single<Map<String, GameV2>> {
    return gamesService
        .getDayGames(
            "\"timeUtc\"",
            DateFormatUtil.getDateStartUtc(date),
            DateFormatUtil.getDateEndUtc(date))
        .doOnSuccess { gamesMap.putAll(it) }

  }

  @SuppressLint("VisibleForTests")
  private fun memorySource(date: Calendar): Single<Map<String, GameV2>> {
    return Single.just(gamesMap
        .filterValues {
          it.timeUtc >= DateFormatUtil.getDateStartUtc(date)
              && it.timeUtc < DateFormatUtil.getDateEndUtc(date)
        })
        .doOnSuccess { saveGamesInCache(it) }
  }

  @VisibleForTesting
  fun saveGamesInCache(gamesMap: Map<String, GameV2>, clear: Boolean = false) {
    if (clear) {
      this.gamesMap.clear()
    }
    this.gamesMap.putAll(gamesMap)
  }
}
