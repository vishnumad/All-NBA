package com.gmail.jorgegilcavazos.ballislife.features.gamethread;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.gmail.jorgegilcavazos.ballislife.base.BasePresenter;
import com.gmail.jorgegilcavazos.ballislife.data.reddit.RedditAuthentication;
import com.gmail.jorgegilcavazos.ballislife.data.repository.submissions.SubmissionRepository;
import com.gmail.jorgegilcavazos.ballislife.data.service.RedditGameThreadsService;
import com.gmail.jorgegilcavazos.ballislife.data.service.RedditService;
import com.gmail.jorgegilcavazos.ballislife.features.common.ThreadAdapter;
import com.gmail.jorgegilcavazos.ballislife.features.model.GameThreadSummary;
import com.gmail.jorgegilcavazos.ballislife.features.model.ThreadItem;
import com.gmail.jorgegilcavazos.ballislife.util.DateFormatUtil;
import com.gmail.jorgegilcavazos.ballislife.util.RedditUtils;
import com.gmail.jorgegilcavazos.ballislife.util.exception.NotLoggedInException;
import com.gmail.jorgegilcavazos.ballislife.util.exception.ReplyNotAvailableException;
import com.gmail.jorgegilcavazos.ballislife.util.exception.ReplyToThreadException;
import com.gmail.jorgegilcavazos.ballislife.util.exception.ThreadNotFoundException;
import com.google.common.base.Optional;

import net.dean.jraw.models.Comment;
import net.dean.jraw.models.CommentNode;
import net.dean.jraw.models.CommentSort;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.VoteDirection;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Named;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class GameThreadPresenter extends BasePresenter<GameThreadView> {
    private long gameDate;

    private final RedditService redditService;
    private final RedditGameThreadsService gameThreadsService;
    private final SubmissionRepository submissionRepository;
    private final SharedPreferences preferences;
    private final RedditAuthentication redditAuthentication;
    private final CompositeDisposable disposables;

    @Inject
    public GameThreadPresenter(
            RedditService redditService,
            RedditGameThreadsService gameThreadsService,
            SubmissionRepository submissionRepository,
            @Named("redditSharedPreferences") SharedPreferences preferences,
            RedditAuthentication redditAuthentication,
            CompositeDisposable disposables) {
        this.redditService = redditService;
        this.gameThreadsService = gameThreadsService;
        this.submissionRepository = submissionRepository;
        this.preferences = preferences;
        this.redditAuthentication = redditAuthentication;
        this.disposables = disposables;
    }

    @Override
    public void attachView(@NonNull GameThreadView view) {
        super.attachView(view);
    }

    public void loadComments(
            final String type,
            final String homeTeamAbbr,
            final String awayTeamAbbr,
            boolean stream,
            boolean forceReload,
            long gameUtc) {

        view.setLoadingIndicator(true);
        view.hideComments();
        view.hideText();

        Observable<List<CommentNode>> observable = redditAuthentication.authenticate()
                .andThen(gameThreadsService.fetchGameThreads("\"created_utc\"",
                                                             DateFormatUtil.addHoursToTime(gameUtc,
                                                                                           -2),
                                                             DateFormatUtil.addHoursToTime(gameUtc,
                                                                                           5)))
                .map(threads -> {
                    List<GameThreadSummary> threadsList = new ArrayList<>();
                    threadsList.addAll(threads.values());
                    return RedditUtils.findGameThreadId(threadsList,
                                                        type,
                                                        homeTeamAbbr,
                                                        awayTeamAbbr);
                })
                .flatMap(threadId -> {
                    if (threadId.equals("")) {
                        return Single.error(new ThreadNotFoundException());
                    }
                    CommentSort sort;
                    switch (type) {
                        case RedditUtils.LIVE_GT_TYPE:
                            sort = CommentSort.NEW;
                            break;
                        case RedditUtils.POST_GT_TYPE:
                            sort = CommentSort.TOP;
                            break;
                        default:
                            throw new IllegalStateException("Thread type should be new or top");
                    }
                    return submissionRepository.getSubmission(threadId, sort, forceReload);
                })
                .flatMap((submissionWrapper) -> {
                    view.setSubmissionId(submissionWrapper.getId());
                    Iterable<CommentNode> iterable = submissionWrapper.getSubmission().getComments()
                            .walkTree();
                    List<CommentNode> commentNodes = new ArrayList<>();
                    for (CommentNode node : iterable) {
                        commentNodes.add(node);
                    }
                    return Single.just(commentNodes);
                })
                .toObservable();

        if (stream) {
            observable = observable.repeatWhen(object -> object.delay(10, TimeUnit.SECONDS));
        }

        disposables.clear();
        disposables.add(observable.subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribeWith(new DisposableObserver<List<CommentNode>>() {
                                    @Override
                                    public void onNext(List<CommentNode> commentNodes) {
                                        List<ThreadItem> items = new ArrayList<>();
                                        for (CommentNode node : commentNodes) {
                                            items.add(new ThreadItem(ThreadAdapter.TYPE_COMMENT,
                                                                     node,
                                                                     node.getDepth()));
                                        }
                                        view.setLoadingIndicator(false);
                                        if (commentNodes.size() == 0) {
                                            view.showNoCommentsText();
                                        } else {
                                            view.showComments(items);
                                        }
                                        view.showFab();
                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        view.setLoadingIndicator(false);
                                        if (e instanceof ThreadNotFoundException) {
                                            view.showNoThreadText();
                                            view.hideFab();
                                        } else {
                                            view.showFailedToLoadCommentsText();
                                        }
                                    }

                                    @Override
                                    public void onComplete() {
                                    }
                                }));
    }

    public void vote(final Comment comment, final VoteDirection voteDirection) {
        disposables.add(redditAuthentication.authenticate()
                                .andThen(redditAuthentication.checkUserLoggedIn())
                                .flatMapCompletable((loggedIn -> {
                                    if (loggedIn) {
                                        return redditService.voteComment(redditAuthentication
                                                                                 .getRedditClient(),
                                                                         comment,
                                                                         voteDirection);
                                    } else {
                                        throw new NotLoggedInException();
                                    }
                                }))
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribeWith(new DisposableCompletableObserver() {
                                    @Override
                                    public void onComplete() {

                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        if (e instanceof NotLoggedInException) {
                                            view.showNotLoggedInToast();
                                        }
                                    }
                                }));
    }

    public void save(final Comment comment) {
        disposables.add(redditAuthentication.authenticate()
                                .andThen(redditAuthentication.checkUserLoggedIn())
                                .flatMapCompletable((loggedIn -> {
                                    if (loggedIn) {
                                        return redditService.saveComment(redditAuthentication
                                                                                 .getRedditClient(),
                                                                         comment);
                                    } else {
                                        throw new NotLoggedInException();
                                    }
                                }))
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribeWith(new DisposableCompletableObserver() {
                                    @Override
                                    public void onComplete() {
                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        if (e instanceof NotLoggedInException) {
                                            view.showNotLoggedInToast();
                                        }
                                    }
                                }));
    }

    public void unsave(final Comment comment) {
        disposables.add(redditAuthentication.authenticate()
                                .andThen(redditAuthentication.checkUserLoggedIn())
                                .flatMapCompletable((loggedIn -> {
                                    if (loggedIn) {
                                        return redditService.unsaveComment(redditAuthentication
                                                                                   .getRedditClient(),
                                                                           comment);
                                    } else {
                                        throw new NotLoggedInException();
                                    }
                                }))
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribeWith(new DisposableCompletableObserver() {
                                    @Override
                                    public void onComplete() {
                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        if (e instanceof NotLoggedInException) {
                                            view.showNotLoggedInToast();
                                        }
                                    }
                                }));
    }

    public void reply(
            final int position,
            final String submissionId,
            final String commentFullName,
            final String text) {
        Optional<Submission> submission = submissionRepository.getCachedSubmission(submissionId);
        if (!submission.isPresent()) {
            throw new IllegalStateException("A cached submission should've been available.");
        }
        Optional<CommentNode> parent = submission.get()
                .getComments()
                .walkTree()
                .firstMatch(node -> node.getComment().getFullName().equals(commentFullName));
        if (!parent.isPresent()) {
            throw new IllegalStateException("Could not find comment to reply to.");
        }

        view.showSavingToast();
        disposables.add(redditAuthentication.authenticate()
                                .andThen(redditAuthentication.checkUserLoggedIn())
                                .flatMap((loggedIn -> {
                                    if (loggedIn) {
                                        return redditService.replyToComment(redditAuthentication
                                                                                    .getRedditClient(),
                                                                            parent.get()
                                                                                    .getComment(),
                                                                            text);
                                    } else {
                                        throw new NotLoggedInException();
                                    }
                                }))
                                // Comment is not immediately available after being posted in the
                                // next call
                                // (probably a small delay from reddit's servers) so we need to
                                // wait for a bit
                                // before fetching the posted comment.
                                .delay(4, TimeUnit.SECONDS)
                                .flatMap(commentId -> redditService.getComment
                                        (redditAuthentication.getRedditClient(),
                                                                               submissionId,
                                                                               commentId))
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribeOn(Schedulers.io())
                                .subscribeWith(new DisposableSingleObserver<CommentNode>() {
                                    @Override
                                    public void onSuccess(CommentNode comment) {
                                        if (isViewAttached()) {
                                            view.showReplySavedToast();
                                            if (comment != null) {
                                                view.addComment(position + 1, comment);
                                            }
                                        }
                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        if (isViewAttached()) {
                                            if (e instanceof ReplyNotAvailableException) {
                                                view.showReplySavedToast();
                                            } else if (e instanceof NotLoggedInException) {
                                                view.showNotLoggedInToast();
                                            } else {
                                                view.showReplyErrorToast();
                                            }
                                        }
                                    }
                                }));
    }

    public void replyToThread(final String text, final String submissionId) {
        Optional<Submission> submission = submissionRepository.getCachedSubmission(submissionId);
        if (!submission.isPresent()) {
            throw new IllegalStateException("A cached submission should've been available.");
        }

        view.showSavingToast();
        disposables.add(redditAuthentication.authenticate()
                                .andThen(redditAuthentication.checkUserLoggedIn())
                                .flatMapCompletable((loggedIn) -> {
                                    if (!loggedIn) {
                                        throw new NotLoggedInException();
                                    }
                                    return Completable.complete();
                                })
                                .andThen(redditService.replyToThread(redditAuthentication
                                                                             .getRedditClient(),
                                                                     submission.get(),
                                                                     text))
                                // Comment is not immediately available after being posted in the
                                // next call
                                // (probably a small delay from reddit's servers) so we need to
                                // wait for a bit
                                // before fetching the posted comment.º
                                .delay(4, TimeUnit.SECONDS)
                                .flatMap(commentId -> redditService.getComment
                                        (redditAuthentication.getRedditClient(),
                                                                               submissionId,
                                                                               commentId))
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribeWith(new DisposableSingleObserver<CommentNode>() {
                                    @Override
                                    public void onSuccess(CommentNode commentNode) {
                                        if (isViewAttached()) {
                                            view.showSavedToast();
                                            view.addComment(0, commentNode);
                                        }
                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        if (isViewAttached()) {
                                            if (e instanceof ThreadNotFoundException) {
                                                view.showNoThreadText();
                                            } else if (e instanceof ReplyToThreadException) {
                                                view.showReplyToSubmissionFailedToast();
                                            } else if (e instanceof ReplyNotAvailableException) {
                                                view.showSavedToast();
                                            } else if (e instanceof NotLoggedInException) {
                                                view.showNotLoggedInToast();
                                            }
                                        }
                                    }
                                }));
    }

    public void replyToCommentBtnClick(int position, Comment parentComment) {
        if (!redditAuthentication.isUserLoggedIn()) {
            view.showNotLoggedInToast();
            return;
        }

        view.openReplyToCommentActivity(position, parentComment);
    }

    public void replyToThreadBtnClick() {
        if (!redditAuthentication.isUserLoggedIn()) {
            view.showNotLoggedInToast();
            return;
        }

        view.openReplyToSubmissionActivity();
    }

    @Override
    public void detachView() {
        disposables.clear();
        super.detachView();
    }
}
