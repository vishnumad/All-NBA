package com.gmail.jorgegilcavazos.ballislife.features.gamethread

import com.gmail.jorgegilcavazos.ballislife.data.actions.RedditActions
import com.gmail.jorgegilcavazos.ballislife.data.actions.models.ReplyUIModel
import com.gmail.jorgegilcavazos.ballislife.data.actions.models.SaveUIModel
import com.gmail.jorgegilcavazos.ballislife.data.actions.models.VoteUIModel
import com.gmail.jorgegilcavazos.ballislife.data.repository.comments.ContributionRepository
import com.gmail.jorgegilcavazos.ballislife.data.repository.gamethreads.GameThreadsRepository
import com.gmail.jorgegilcavazos.ballislife.features.model.GameThreadType
import com.gmail.jorgegilcavazos.ballislife.util.CrashReporter
import com.gmail.jorgegilcavazos.ballislife.util.schedulers.TrampolineSchedulerProvider
import com.google.common.collect.FluentIterable
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import net.dean.jraw.models.Comment
import net.dean.jraw.models.CommentNode
import net.dean.jraw.models.Submission
import net.dean.jraw.models.VoteDirection
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

class GameThreadPresenterV2Test {

  companion object {
    val THREAD_TYPE = GameThreadType.LIVE
    val HOME = "SAS"
    val VISITOR = "MIL"
    val GAME_TIME_UTC = 10000L
    val SUBMISSION_ID = "82fh"
    val PARENT_FULLNAME = "9n1uid3"
    val RESPONSE = "This is a reply!"

    val commentSaves: PublishSubject<Comment> = PublishSubject.create()
    val commentUnsaves: PublishSubject<Comment> = PublishSubject.create()
    val upvotes: PublishSubject<Comment> = PublishSubject.create()
    val downvotes: PublishSubject<Comment> = PublishSubject.create()
    val novotes: PublishSubject<Comment> = PublishSubject.create()
    val replies: PublishSubject<Comment> = PublishSubject.create()
    val submissionReplies: PublishSubject<Any> = PublishSubject.create()
    val streamChanges: PublishSubject<Boolean> = PublishSubject.create()
  }

  @Mock private lateinit var mockView: GameThreadView
  @Mock private lateinit var mockGameThreadsRepository: GameThreadsRepository
  @Mock private lateinit var mockRedditActions: RedditActions
  @Mock private lateinit var mockContributionsRepository: ContributionRepository
  @Mock private lateinit var threadsDisposable: CompositeDisposable
  @Mock private lateinit var disposable: CompositeDisposable
  @Mock private lateinit var mockCrashReporter: CrashReporter

  private lateinit var presenter: GameThreadPresenterV2

  @Before
  fun setUp() {
    MockitoAnnotations.initMocks(this)

    `when`(mockView.getThreadType()).thenReturn(THREAD_TYPE)
    `when`(mockView.getHome()).thenReturn(HOME)
    `when`(mockView.getVisitor()).thenReturn(VISITOR)
    `when`(mockView.getGameTimeUtc()).thenReturn(GAME_TIME_UTC)
    `when`(mockView.commentSaves()).thenReturn(commentSaves)
    `when`(mockView.commentUnsaves()).thenReturn(commentUnsaves)
    `when`(mockView.upvotes()).thenReturn(upvotes)
    `when`(mockView.downvotes()).thenReturn(downvotes)
    `when`(mockView.novotes()).thenReturn(novotes)
    `when`(mockView.replies()).thenReturn(replies)
    `when`(mockView.submissionReplies()).thenReturn(submissionReplies)
    `when`(mockView.streamChanges()).thenReturn(streamChanges)

    presenter = GameThreadPresenterV2(
        mockGameThreadsRepository,
        mockRedditActions,
        mockContributionsRepository,
        TrampolineSchedulerProvider(),
        threadsDisposable,
        disposable,
        mockCrashReporter)
    presenter.attachView(mockView)
  }

  @Test
  fun clearSubscriptionOnDetach() {
    presenter.detachView()

    verify(disposable).clear()
    verify(threadsDisposable).clear()
  }

  @Test
  fun saveCommentInProgress() {
    val mockComment = Mockito.mock(Comment::class.java)
    `when`(mockRedditActions.saveComment(mockComment))
        .thenReturn(Observable.just(SaveUIModel.inProgress()))

    commentSaves.onNext(mockComment)

    verify(mockRedditActions).saveComment(mockComment)
    verify(mockView).showSavingToast()
    verify(mockView, times(0)).showNotLoggedInToast()
    verify(mockView, times(0)).showSavedToast()
  }

  @Test
  fun saveCommentSuccess() {
    val mockComment = Mockito.mock(Comment::class.java)
    `when`(mockRedditActions.saveComment(mockComment))
        .thenReturn(Observable.just(SaveUIModel.success()))

    commentSaves.onNext(mockComment)

    verify(mockRedditActions).saveComment(mockComment)
    verify(mockView).showSavedToast()
    verify(mockView, times(0)).showNotLoggedInToast()
    verify(mockView, times(0)).showSavingToast()
  }

  @Test
  fun saveCommentNotLoggedIn() {
    val mockComment = Mockito.mock(Comment::class.java)
    `when`(mockRedditActions.saveComment(mockComment))
        .thenReturn(Observable.just(SaveUIModel.notLoggedIn()))

    commentSaves.onNext(mockComment)

    verify(mockRedditActions).saveComment(mockComment)
    verify(mockView).showNotLoggedInToast()
    verify(mockView, times(0)).showSavingToast()
    verify(mockView, times(0)).showSavedToast()
  }

  @Test
  fun unsaveCommentInProgress() {
    val mockComment = Mockito.mock(Comment::class.java)
    `when`(mockRedditActions.unsaveComment(mockComment))
        .thenReturn(Observable.just(SaveUIModel.inProgress()))

    commentUnsaves.onNext(mockComment)

    verify(mockRedditActions).unsaveComment(mockComment)
    verify(mockView).showUnsavingToast()
    verify(mockView, times(0)).showNotLoggedInToast()
    verify(mockView, times(0)).showUnsavedToast()
  }

  @Test
  fun unsaveCommentSuccess() {
    val mockComment = Mockito.mock(Comment::class.java)
    `when`(mockRedditActions.unsaveComment(mockComment))
        .thenReturn(Observable.just(SaveUIModel.success()))

    commentUnsaves.onNext(mockComment)

    verify(mockRedditActions).unsaveComment(mockComment)
    verify(mockView).showUnsavedToast()
    verify(mockView, times(0)).showNotLoggedInToast()
    verify(mockView, times(0)).showUnsavingToast()
  }

  @Test
  fun unsaveCommentNotLoggedIn() {
    val mockComment = Mockito.mock(Comment::class.java)
    `when`(mockRedditActions.unsaveComment(mockComment))
        .thenReturn(Observable.just(SaveUIModel.notLoggedIn()))

    commentUnsaves.onNext(mockComment)

    verify(mockRedditActions).unsaveComment(mockComment)
    verify(mockView).showNotLoggedInToast()
    verify(mockView, times(0)).showUnsavingToast()
    verify(mockView, times(0)).showUnsavedToast()
  }

  @Test
  fun upvote() {
    val mockComment = Mockito.mock(Comment::class.java)
    `when`(mockRedditActions.voteComment(mockComment, VoteDirection.UPVOTE))
        .thenReturn(Observable.just(VoteUIModel.success()))

    upvotes.onNext(mockComment)

    verify(mockRedditActions).voteComment(mockComment, VoteDirection.UPVOTE)
  }

  @Test
  fun downvote() {
    val mockComment = Mockito.mock(Comment::class.java)
    `when`(mockRedditActions.voteComment(mockComment, VoteDirection.DOWNVOTE))
        .thenReturn(Observable.just(VoteUIModel.success()))

    downvotes.onNext(mockComment)

    verify(mockRedditActions).voteComment(mockComment, VoteDirection.DOWNVOTE)
  }

  @Test
  fun novote() {
    val mockComment = Mockito.mock(Comment::class.java)
    `when`(mockRedditActions.voteComment(mockComment, VoteDirection.NO_VOTE))
        .thenReturn(Observable.just(VoteUIModel.success()))

    novotes.onNext(mockComment)

    verify(mockRedditActions).voteComment(mockComment, VoteDirection.NO_VOTE)
  }

  @Test
  fun openReplyActivityOnCommentReply() {
    val mockComment = Mockito.mock(Comment::class.java)

    replies.onNext(mockComment)

    verify(mockContributionsRepository).saveComment(mockComment)
    verify(mockView).openReplyToCommentActivity(mockComment)
  }

  @Test
  fun openReplyActivityOnSubmissionReply() {
    val mockSubmission = Mockito.mock(Submission::class.java)
    `when`(mockSubmission.id).thenReturn(SUBMISSION_ID)

    presenter.setCurrentSubmission(mockSubmission)
    submissionReplies.onNext(Any())

    verify(mockContributionsRepository).saveSubmission(mockSubmission)
    verify(mockView).openReplyToSubmissionActivity(SUBMISSION_ID)
  }

  @Test
  fun loadGameThreadInProgress() {
    `when`(mockGameThreadsRepository.gameThreads(HOME, VISITOR, GAME_TIME_UTC, THREAD_TYPE))
        .thenReturn(Observable.just(GameThreadsUIModel.inProgress()))

    presenter.loadGameThread()

    verify(mockGameThreadsRepository).gameThreads(HOME, VISITOR, GAME_TIME_UTC, THREAD_TYPE)
    verify(mockView).setLoadingIndicator(true)
    verify(mockView).hideErrorLoadingText()
    verify(mockView).hideNoCommentsText()
    verify(mockView).hideFab()
    verify(mockView).hideNoThreadText()
  }

  @Test
  fun loadGameThreadNotFound() {
    `when`(mockGameThreadsRepository.gameThreads(HOME, VISITOR, GAME_TIME_UTC, THREAD_TYPE))
        .thenReturn(Observable.just(GameThreadsUIModel.notFound()))

    presenter.loadGameThread()

    verify(mockView).setLoadingIndicator(false)
    verify(mockView).hideErrorLoadingText()
    verify(mockView).hideNoCommentsText()
    verify(mockView).hideFab()
    verify(mockView).showNoThreadText()
  }

  @Test
  fun loadGameThreadFoundWithComments() {
    val mockCommentNode1 = Mockito.mock(CommentNode::class.java)
    `when`(mockCommentNode1.depth).thenReturn(0)
    val mockCommentNode2 = Mockito.mock(CommentNode::class.java)
    `when`(mockCommentNode2.depth).thenReturn(1)
    val mockSubmission = Mockito.mock(Submission::class.java)
    `when`(mockSubmission.comments).thenReturn(mockCommentNode1)
    `when`(mockCommentNode1.walkTree())
        .thenReturn(FluentIterable.of(mockCommentNode1, mockCommentNode2))
    `when`(mockGameThreadsRepository.gameThreads(HOME, VISITOR, GAME_TIME_UTC, THREAD_TYPE))
        .thenReturn(Observable.just(GameThreadsUIModel.found(mockSubmission)))

    presenter.loadGameThread()

    verify(mockView).setLoadingIndicator(false)
    verify(mockView).hideErrorLoadingText()
    verify(mockView).hideNoCommentsText()
    verify(mockView).showFab()
    verify(mockView).showComments(ArgumentMatchers.anyList())
    verify(mockView).hideNoThreadText()
  }

  @Test
  fun loadGameThreadFoundWithNoComments() {
    val mockCommentNode1 = Mockito.mock(CommentNode::class.java)
    val mockSubmission = Mockito.mock(Submission::class.java)
    `when`(mockSubmission.comments).thenReturn(mockCommentNode1)
    `when`(mockCommentNode1.walkTree()).thenReturn(FluentIterable.of())
    `when`(mockGameThreadsRepository.gameThreads(HOME, VISITOR, GAME_TIME_UTC, THREAD_TYPE))
        .thenReturn(Observable.just(GameThreadsUIModel.found(mockSubmission)))

    presenter.loadGameThread()

    verify(mockView).setLoadingIndicator(false)
    verify(mockView).hideErrorLoadingText()
    verify(mockView).hideNoCommentsText()
    verify(mockView).showFab()
    verify(mockView).showNoCommentsText()
    verify(mockView).hideComments()
    verify(mockView).hideNoThreadText()
  }

  @Test
  fun loadGameThreadError() {
    val e = Exception()
    `when`(mockGameThreadsRepository.gameThreads(HOME, VISITOR, GAME_TIME_UTC, THREAD_TYPE))
        .thenReturn(Observable.error(e))

    presenter.loadGameThread()

    verify(mockView).setLoadingIndicator(false)
    verify(mockView).showErrorLoadingText()
    verify(mockCrashReporter).report(e)
  }

  @Test
  fun loadGameThreadInProgressWhileStreaming() {
    `when`(mockGameThreadsRepository.gameThreads(HOME, VISITOR, GAME_TIME_UTC, THREAD_TYPE))
        .thenReturn(Observable.just(GameThreadsUIModel.inProgress()))

    presenter.setShouldStream(true)
    presenter.loadGameThread()

    verify(mockGameThreadsRepository).gameThreads(HOME, VISITOR, GAME_TIME_UTC, THREAD_TYPE)
    verify(mockView).setLoadingIndicator(false)
    verify(mockView).hideErrorLoadingText()
    verify(mockView).hideNoCommentsText()
    verify(mockView, times(0)).hideFab()
    verify(mockView).hideNoThreadText()
  }

  @Test
  fun replyToCommentInProgress() {
    `when`(mockRedditActions.replyToComment(PARENT_FULLNAME, RESPONSE))
        .thenReturn(Observable.just(ReplyUIModel.inProgress()))

    presenter.replyToComment(PARENT_FULLNAME, RESPONSE)

    verify(mockRedditActions).replyToComment(PARENT_FULLNAME, RESPONSE)
    verify(mockView).showSubmittingCommentToast()
  }

  @Test
  fun replyToCommentParentNotFound() {
    `when`(mockRedditActions.replyToComment(PARENT_FULLNAME, RESPONSE))
        .thenReturn(Observable.just(ReplyUIModel.parentNotFound()))

    presenter.replyToComment(PARENT_FULLNAME, RESPONSE)

    verify(mockView).showMissingParentToast()
  }

  @Test
  fun replyToCommentNotLoggedIn() {
    `when`(mockRedditActions.replyToComment(PARENT_FULLNAME, RESPONSE))
        .thenReturn(Observable.just(ReplyUIModel.notLoggedIn()))

    presenter.replyToComment(PARENT_FULLNAME, RESPONSE)

    verify(mockView).showNotLoggedInToast()
  }

  @Test
  fun replyToCommentSuccess() {
    `when`(mockRedditActions.replyToComment(PARENT_FULLNAME, RESPONSE))
        .thenReturn(Observable.just(ReplyUIModel.success()))

    presenter.replyToComment(PARENT_FULLNAME, RESPONSE)

    verify(mockView).showSubmittedCommentToast()
  }

  @Test
  fun replyToSubmissionInProgress() {
    `when`(mockRedditActions.replyToSubmission(SUBMISSION_ID, RESPONSE))
        .thenReturn(Observable.just(ReplyUIModel.inProgress()))

    presenter.replyToSubmission(SUBMISSION_ID, RESPONSE)

    verify(mockRedditActions).replyToSubmission(SUBMISSION_ID, RESPONSE)
    verify(mockView).showSubmittingCommentToast()
  }

  @Test
  fun replyToSubmissionParentNotFound() {
    `when`(mockRedditActions.replyToSubmission(SUBMISSION_ID, RESPONSE))
        .thenReturn(Observable.just(ReplyUIModel.parentNotFound()))

    presenter.replyToSubmission(SUBMISSION_ID, RESPONSE)

    verify(mockView).showMissingSubmissionToast()
  }

  @Test
  fun replyToSubmissionNotLoggedIn() {
    `when`(mockRedditActions.replyToSubmission(SUBMISSION_ID, RESPONSE))
        .thenReturn(Observable.just(ReplyUIModel.notLoggedIn()))

    presenter.replyToSubmission(SUBMISSION_ID, RESPONSE)

    verify(mockView).showNotLoggedInToast()
  }

  @Test
  fun replyToSubmissionSuccess() {
    `when`(mockRedditActions.replyToSubmission(SUBMISSION_ID, RESPONSE))
        .thenReturn(Observable.just(ReplyUIModel.success()))

    presenter.replyToSubmission(SUBMISSION_ID, RESPONSE)

    verify(mockView).showSubmittedCommentToast()
  }

  @Test
  fun turnOnStreamingWithPremiumPurchased() {
    `when`(mockView.isPremiumPurchased()).thenReturn(true)

    streamChanges.onNext(true)

    verify(mockView).isPremiumPurchased()
    verify(mockView, times(0)).setStreamSwitch(false)
    verify(mockView, times(0)).purchasePremium()
  }

  @Test
  fun turnOnStreamingWithoutPremiumPurchased() {
    `when`(mockView.isPremiumPurchased()).thenReturn(false)

    streamChanges.onNext(true)

    verify(mockView).isPremiumPurchased()
    verify(mockView).setStreamSwitch(false)
    verify(mockView).purchasePremium()
  }
}