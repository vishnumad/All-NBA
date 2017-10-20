package com.gmail.jorgegilcavazos.ballislife.data.repository.gamethreads

import com.gmail.jorgegilcavazos.ballislife.data.repository.submissions.SubmissionRepository
import com.gmail.jorgegilcavazos.ballislife.data.service.RedditGameThreadsService
import com.gmail.jorgegilcavazos.ballislife.features.gamethread.GameThreadsUIModel
import com.gmail.jorgegilcavazos.ballislife.features.model.GameThreadSummary
import com.gmail.jorgegilcavazos.ballislife.features.model.GameThreadType
import com.gmail.jorgegilcavazos.ballislife.features.model.GameThreadType.LIVE
import com.gmail.jorgegilcavazos.ballislife.features.model.GameThreadType.POST
import com.gmail.jorgegilcavazos.ballislife.features.model.SubmissionWrapper
import com.gmail.jorgegilcavazos.ballislife.util.DateFormatUtil
import com.gmail.jorgegilcavazos.ballislife.util.RedditUtils
import com.gmail.jorgegilcavazos.ballislife.util.TeamName
import com.gmail.jorgegilcavazos.ballislife.util.schedulers.BaseSchedulerProvider
import io.reactivex.Observable
import io.reactivex.Single
import net.dean.jraw.models.CommentSort
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameThreadsRepositoryImpl @Inject constructor(
    private val threadsService: RedditGameThreadsService,
    private val submissionRepository: SubmissionRepository,
    private val schedulerProvider: BaseSchedulerProvider) : GameThreadsRepository {

  override fun gameThreads(home: String, visitor: String, gameTimeUtc: Long, type: GameThreadType)
      : Observable<GameThreadsUIModel> {
    return threadsNetworkSource(gameTimeUtc).toObservable()
        .flatMap {
          val gameThreads = it.values.filter { isThreadForGame(it.title, home, visitor, type) }
          if (gameThreads.isEmpty()) {
            Observable.just(GameThreadsUIModel.notFound())
          } else {
            val sort = when (type) {
              LIVE -> CommentSort.NEW
              POST -> CommentSort.TOP
            }

            val submissionObservables = gameThreads
                .map { submissionRepository.getSubmission(it.id, sort, true).toObservable() }
                .toList()

            // Return found model of first non deleted thread or not found if there aren't any.
            Observable.merge(submissionObservables)
                .filter { !RedditUtils.isRemovedOrDeleted(it.submission!!) }
                .first(SubmissionWrapper("", null, "", ""))
                .flatMapObservable {
                  if (it.id.isEmpty()) {
                    Observable.just(GameThreadsUIModel.notFound())
                  } else {
                    Observable.just(GameThreadsUIModel.found(it.submission!!))
                  }
                }
          }
        }
        .subscribeOn(schedulerProvider.io())
        .observeOn(schedulerProvider.ui())
        .startWith(GameThreadsUIModel.inProgress())
  }

  private fun threadsNetworkSource(gameTimeUtc: Long): Single<Map<String, GameThreadSummary>> {
    return threadsService.fetchGameThreads(
        "\"created_utc\"",
        DateFormatUtil.addHoursToTime(gameTimeUtc, -2),
        DateFormatUtil.addHoursToTime(gameTimeUtc, 5))
  }

  private fun isThreadForGame(title: String, home: String, visitor: String, type: GameThreadType)
      : Boolean {
    val homeFullName = TeamName.values().find { it.toString() == home }
    val visitorFullName = TeamName.values().find { it.toString() == visitor }

    if (homeFullName == null) return false
    if (visitorFullName == null) return false

    return when (type) {
      LIVE -> {
        val upperTitle = title.toUpperCase()
        upperTitle.contains("GAME THREAD") && !upperTitle.contains("POST")
            && titleContainsTeam(upperTitle, homeFullName.teamName)
            && titleContainsTeam(upperTitle, visitorFullName.teamName)
      }
      POST -> {
        val upperTitle = title.toUpperCase()
        (upperTitle.contains("POST GAME THREAD") || upperTitle.contains("POST-GAME THREAD"))
            && titleContainsTeam(upperTitle, homeFullName.teamName)
            && titleContainsTeam(upperTitle, visitorFullName.teamName)
      }
    }
  }

  private fun titleContainsTeam(upperTitle: String, teamFullName: String): Boolean {
    val upperFullName = teamFullName.toUpperCase() // Ex. "SAN ANTONIO SPURS".
    val upperTeamName = upperFullName.substring(upperFullName.lastIndexOf(" ") + 1) // Ex. "SPURS".
    return upperTitle.contains(upperTeamName)
  }
}