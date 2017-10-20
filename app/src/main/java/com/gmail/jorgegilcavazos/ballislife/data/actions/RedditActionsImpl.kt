package com.gmail.jorgegilcavazos.ballislife.data.actions

import com.gmail.jorgegilcavazos.ballislife.data.actions.models.ReplyUIModel
import com.gmail.jorgegilcavazos.ballislife.data.actions.models.SaveUIModel
import com.gmail.jorgegilcavazos.ballislife.data.actions.models.VoteUIModel
import com.gmail.jorgegilcavazos.ballislife.data.reddit.RedditAuthentication
import com.gmail.jorgegilcavazos.ballislife.data.repository.comments.ContributionRepository
import com.gmail.jorgegilcavazos.ballislife.data.service.RedditService
import com.gmail.jorgegilcavazos.ballislife.util.schedulers.BaseSchedulerProvider
import io.reactivex.Observable
import net.dean.jraw.models.Comment
import net.dean.jraw.models.VoteDirection
import javax.inject.Inject

class RedditActionsImpl @Inject constructor(
    private val redditAuthentication: RedditAuthentication,
    private val redditService: RedditService,
    private val contributionRepository: ContributionRepository,
    private val schedulerProvider: BaseSchedulerProvider) : RedditActions {


  override fun saveComment(comment: Comment): Observable<SaveUIModel> {
    return redditAuthentication.authenticate()
        .andThen(redditAuthentication.checkUserLoggedIn())
        .toObservable()
        .flatMap {
          if (it) {
            redditService.saveComment(redditAuthentication.redditClient, comment)
                .andThen(Observable.just(SaveUIModel.success()))
          } else {
            Observable.just(SaveUIModel.notLoggedIn())
          }
        }
        .subscribeOn(schedulerProvider.io())
        .observeOn(schedulerProvider.ui())
        .startWith(SaveUIModel.inProgress())
  }

  override fun unsaveComment(comment: Comment): Observable<SaveUIModel> {
    return redditAuthentication.authenticate()
        .andThen(redditAuthentication.checkUserLoggedIn())
        .toObservable()
        .flatMap {
          if (it) {
            redditService.unsaveComment(redditAuthentication.redditClient, comment)
                .andThen(Observable.just(SaveUIModel.success()))
          } else {
            Observable.just(SaveUIModel.notLoggedIn())
          }
        }
        .subscribeOn(schedulerProvider.io())
        .observeOn(schedulerProvider.ui())
        .startWith(SaveUIModel.inProgress())
  }

  override fun voteComment(comment: Comment, voteDirection: VoteDirection)
      : Observable<VoteUIModel> {
    return redditAuthentication.authenticate()
        .andThen(redditAuthentication.checkUserLoggedIn())
        .toObservable()
        .flatMap {
          if (it) {
            redditService.voteComment(redditAuthentication.redditClient, comment, voteDirection)
                .andThen(Observable.just(VoteUIModel.success()))
          } else {
            Observable.just(VoteUIModel.notLoggedIn())
          }
        }
        .subscribeOn(schedulerProvider.io())
        .observeOn(schedulerProvider.ui())
        .startWith(VoteUIModel.inProgress())
  }

  override fun replyToComment(parentFullname: String, response: String)
      : Observable<ReplyUIModel> {
    return redditAuthentication.authenticate()
        .andThen(redditAuthentication.checkUserLoggedIn())
        .toObservable()
        .flatMap {
          if (it) {
            val parent = contributionRepository.getComment(parentFullname)
            if (parent != null) {
              redditService.replyToComment(redditAuthentication.redditClient, parent, response)
                  .flatMapObservable { Observable.just(ReplyUIModel.success()) }
            } else {
              Observable.just(ReplyUIModel.parentNotFound())
            }
          } else {
            Observable.just(ReplyUIModel.notLoggedIn())
          }
        }
        .subscribeOn(schedulerProvider.io())
        .observeOn(schedulerProvider.ui())
        .startWith(ReplyUIModel.inProgress())
  }

  override fun replyToSubmission(submissionId: String, response: String): Observable<ReplyUIModel> {
    return redditAuthentication.authenticate()
        .andThen(redditAuthentication.checkUserLoggedIn())
        .toObservable()
        .flatMap {
          if (it) {
            val submission = contributionRepository.getSubmission(submissionId)
            if (submission != null) {
              redditService.replyToThread(redditAuthentication.redditClient, submission, response)
                  .flatMapObservable { Observable.just(ReplyUIModel.success()) }
            } else {
              Observable.just(ReplyUIModel.parentNotFound())
            }
          } else {
            Observable.just(ReplyUIModel.notLoggedIn())
          }
        }
        .subscribeOn(schedulerProvider.io())
        .observeOn(schedulerProvider.ui())
        .startWith(ReplyUIModel.inProgress())
  }
}