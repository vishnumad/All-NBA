package com.gmail.jorgegilcavazos.ballislife.data.repository.games

import com.gmail.jorgegilcavazos.ballislife.data.service.NbaGamesService
import com.gmail.jorgegilcavazos.ballislife.features.model.GameV2
import com.gmail.jorgegilcavazos.ballislife.util.DateFormatUtil
import io.reactivex.Observable
import io.reactivex.Single
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of the [GamesRepository] interface.
 */
@Singleton
class GamesRepositoryImpl @Inject constructor(
    private val gamesService: NbaGamesService) : GamesRepository {

  private val gamesMap = HashMap<String, GameV2>()

  override fun getGames(date: Calendar, forceReload: Boolean): Observable<List<GameV2>> {
    val source: Observable<Map<String, GameV2>> = if (forceReload) {
      Timber.i("Requesting games directly from the network source")
      networkSource(date).toObservable()
    } else {
      Timber.i("Requesting games from anywhere")
      Single.concat(memorySource(date), networkSource(date)).toObservable()
    }

    return source
        .filter { it.isNotEmpty() }
        .concatMap { Observable.just(it.values.sortedBy { it.timeUtc }) }
  }

  private fun networkSource(date: Calendar): Single<Map<String, GameV2>> {
    return gamesService
        .getDayGames(
            "\"timeUtc\"",
            DateFormatUtil.getDateStartUtc(date),
            DateFormatUtil.getDateEndUtc(date))
        .doOnSuccess { gamesMap.putAll(it) }

  }

  private fun memorySource(date: Calendar): Single<Map<String, GameV2>> {
    return Single.just(gamesMap
        .filterValues {
          it.timeUtc > DateFormatUtil.getDateStartUtc(date)
              && it.timeUtc < DateFormatUtil.getDateEndUtc(date)
        })
  }
}
