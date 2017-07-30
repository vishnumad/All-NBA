package com.gmail.jorgegilcavazos.ballislife.features.gamethread;

import android.content.SharedPreferences;

import com.gmail.jorgegilcavazos.ballislife.data.reddit.RedditAuthentication;
import com.gmail.jorgegilcavazos.ballislife.data.service.GameThreadFinderService;
import com.gmail.jorgegilcavazos.ballislife.data.service.RedditGameThreadsService;
import com.gmail.jorgegilcavazos.ballislife.data.service.RedditService;
import com.gmail.jorgegilcavazos.ballislife.features.model.GameThreadSummary;
import com.gmail.jorgegilcavazos.ballislife.util.DateFormatUtil;
import com.gmail.jorgegilcavazos.ballislife.util.exception.NotLoggedInException;
import com.gmail.jorgegilcavazos.ballislife.util.exception.ReplyNotAvailableException;
import com.gmail.jorgegilcavazos.ballislife.util.exception.ReplyToThreadException;
import com.gmail.jorgegilcavazos.ballislife.util.exception.ThreadNotFoundException;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import net.dean.jraw.models.Comment;
import net.dean.jraw.models.CommentNode;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.VoteDirection;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.CompletableSource;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GameThreadPresenter {

    private static final String TAG = "GameThreadPresenter";

    private long gameDate;

    private GameThreadView view;
    private RedditService redditService;
    private RedditGameThreadsService gameThreadsService;
    private SharedPreferences preferences;
    private RedditAuthentication redditAuthentication;
    private CompositeDisposable disposables;

    public GameThreadPresenter(
            GameThreadView view,
            RedditService redditService,
            long gameDate,
            SharedPreferences preferences,
            RedditAuthentication redditAuthentication) {
        this.view = view;
        this.redditService = redditService;
        this.gameDate = gameDate;
        this.preferences = preferences;
        this.redditAuthentication = redditAuthentication;
    }

    public void start() {
        disposables = new CompositeDisposable();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://nba-app-ca681.firebaseio.com/")
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        gameThreadsService = retrofit.create(RedditGameThreadsService.class);
    }

    public void loadComments(final String type, final String homeTeamAbbr,
                             final String awayTeamAbbr, boolean stream) {

        view.setLoadingIndicator(true);
        view.hideComments();
        view.hideText();

        Observable<List<CommentNode>> observable = redditAuthentication.authenticate(preferences)
                .andThen(gameThreadsService.fetchGameThreads(
                        DateFormatUtil.getNoDashDateString(new Date(gameDate))))
                .flatMap(new Function<Map<String, GameThreadSummary>, SingleSource<String>>() {
                    @Override
                    public SingleSource<String> apply(Map<String, GameThreadSummary> threads) throws Exception {
                        List<GameThreadSummary> threadList = new ArrayList<>();
                        for (Map.Entry<String, GameThreadSummary> entry : threads.entrySet()) {
                            threadList.add(entry.getValue());
                        }
                        return GameThreadFinderService.findGameThreadInList(threadList, type,
                                homeTeamAbbr, awayTeamAbbr);
                    }
                })
                .flatMap(new Function<String, SingleSource<List<CommentNode>>>() {
                    @Override
                    public SingleSource<List<CommentNode>> apply(String threadId) throws Exception {
                        if (threadId.equals("")) {
                            return Single.error(new ThreadNotFoundException());
                        }
                        return redditService.getComments(redditAuthentication.getRedditClient(),
                                threadId, type);
                    }
                })
                .toObservable();

        if (stream) {
            observable = observable.repeatWhen(new Function<Observable<Object>, ObservableSource<?>>() {
                @Override
                public ObservableSource<?> apply(Observable<Object> objectObservable) throws Exception {
                    return objectObservable.delay(10, TimeUnit.SECONDS);
                }
            });
        }

        disposables.clear();
        disposables.add(observable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<List<CommentNode>>() {
                    @Override
                    public void onNext(List<CommentNode> commentNodes) {
                        view.setLoadingIndicator(false);
                        if (commentNodes.size() == 0) {
                            view.showNoCommentsText();
                        } else {
                            view.showComments(commentNodes);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        view.setLoadingIndicator(false);
                        if (e instanceof ThreadNotFoundException) {
                            view.showNoThreadText();
                        } else {
                            view.showFailedToLoadCommentsText();
                        }
                    }

                    @Override
                    public void onComplete() {
                    }
                })
        );
    }

    public void vote(final Comment comment, final VoteDirection voteDirection) {
        if (!redditAuthentication.isUserLoggedIn()) {
            view.showNotLoggedInToast();
            return;
        }

        disposables.add(redditAuthentication.authenticate(preferences)
                .andThen(redditAuthentication.checkUserLoggedIn())
                .flatMapCompletable(new Function<Boolean, CompletableSource>() {
                    @Override
                    public CompletableSource apply(Boolean loggedIn) throws Exception {
                        if (loggedIn) {
                            return redditService.voteComment(
                                    redditAuthentication.getRedditClient(),
                                    comment,
                                    voteDirection);
                        } else {
                            throw new NotLoggedInException();
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableCompletableObserver() {
                    @Override
                    public void onComplete() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                })
        );
    }

    public void save(final Comment comment) {
        if (!redditAuthentication.isUserLoggedIn()) {
            view.showNotLoggedInToast();
            return;
        }

        disposables.add(redditAuthentication.authenticate(preferences)
                .andThen(redditAuthentication.checkUserLoggedIn())
                .flatMapCompletable(new Function<Boolean, CompletableSource>() {
                    @Override
                    public CompletableSource apply(Boolean loggedIn) throws Exception {
                        if (loggedIn) {
                            return redditService.saveComment(
                                    redditAuthentication.getRedditClient(),
                                    comment);
                        } else {
                            throw new NotLoggedInException();
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableCompletableObserver() {
                    @Override
                    public void onComplete() {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }
                })
        );
    }

    public void unsave(final Comment comment) {
        if (!redditAuthentication.isUserLoggedIn()) {
            view.showNotLoggedInToast();
            return;
        }

        disposables.add(redditAuthentication.authenticate(preferences)
                .andThen(redditAuthentication.checkUserLoggedIn())
                .flatMapCompletable(new Function<Boolean, CompletableSource>() {
                    @Override
                    public CompletableSource apply(Boolean loggedIn) throws Exception {
                        if (loggedIn) {
                            return redditService.unsaveComment(
                                    redditAuthentication.getRedditClient(),
                                    comment);
                        } else {
                            throw new NotLoggedInException();
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableCompletableObserver() {
                    @Override
                    public void onComplete() {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }
                })
        );
    }

    public void reply(final int position, final Comment parentComment, final String text) {
        if (!redditAuthentication.isUserLoggedIn()) {
            view.showNotLoggedInToast();
            return;
        }

        view.showSavingToast();
        disposables.add(redditAuthentication.authenticate(preferences)
                .andThen(redditAuthentication.checkUserLoggedIn())
                .flatMap(new Function<Boolean, SingleSource<String>>() {
                    @Override
                    public SingleSource<String> apply(Boolean loggedIn) throws Exception {
                        if (loggedIn) {
                            return redditService.replyToComment(
                                    redditAuthentication.getRedditClient(), parentComment, text);
                        } else {
                            throw new NotLoggedInException();
                        }
                    }
                })
                // Comment is not immediately available after being posted in the next call
                // (probably a small delay from reddit's servers) so we need to wait for a bit
                // before fetching the posted comment.
                .delay(4, TimeUnit.SECONDS)
                .flatMap(new Function<String, SingleSource<CommentNode>>() {
                    @Override
                    public SingleSource<CommentNode> apply(String s) throws Exception {
                        return redditService.getComment(redditAuthentication.getRedditClient(),
                                parentComment.getSubmissionId().substring(3), s);
                    }
                })
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
                            } else {
                                view.showReplyErrorToast();
                            }
                        }
                    }
                })
        );
    }

    public void replyToThread(final String text, final String type, final String homeTeamAbbr,
                              final String awayTeamAbbr) {
        if (!redditAuthentication.isUserLoggedIn()) {
            view.showNotLoggedInToast();
            return;
        }

        view.showSavingToast();
        disposables.add(redditAuthentication.authenticate(preferences)
                .andThen(redditAuthentication.checkUserLoggedIn())
                .flatMap(new Function<Boolean, SingleSource<Map<String, GameThreadSummary>>>() {
                    @Override
                    public SingleSource<Map<String, GameThreadSummary>> apply(Boolean loggedIn)
                            throws Exception {
                        if (loggedIn) {
                            return gameThreadsService.fetchGameThreads(
                                    DateFormatUtil.getNoDashDateString(new Date(gameDate)));
                        } else {
                            throw new NotLoggedInException();
                        }
                    }
                })
                .flatMap(new Function<Map<String, GameThreadSummary>, SingleSource<String>>() {
                    @Override
                    public SingleSource<String> apply(Map<String, GameThreadSummary> threads) throws Exception {
                        List<GameThreadSummary> threadList = new ArrayList<>();
                        for (Map.Entry<String, GameThreadSummary> entry : threads.entrySet()) {
                            threadList.add(entry.getValue());
                        }
                        return GameThreadFinderService.findGameThreadInList(threadList, type,
                                homeTeamAbbr, awayTeamAbbr);
                    }
                })
                .flatMap(new Function<String, SingleSource<Submission>>() {
                    @Override
                    public SingleSource<Submission> apply(String threadId) throws Exception {
                        if (threadId.equals("")) {
                            return Single.error(new ThreadNotFoundException());
                        }
                        return redditService.getSubmission(redditAuthentication.getRedditClient(),
                                threadId, null);
                    }
                })
                .flatMap(new Function<Submission, SingleSource<CommentNode>>() {
                    @Override
                    public SingleSource<CommentNode> apply(final Submission submission) throws
                            Exception {
                        return redditService.replyToThread(redditAuthentication.getRedditClient(),
                                submission, text)
                                    // Comment is not immediately available after being posted in the next call
                                    // (probably a small delay from reddit's servers) so we need to wait for a bit
                                    // before fetching the posted comment.
                                    .delay(4, TimeUnit.SECONDS)
                                    .flatMap(new Function<String, SingleSource<CommentNode>>() {
                                        @Override
                                        public SingleSource<CommentNode> apply(String commentId)
                                                throws Exception {
                                            return redditService.getComment(
                                                    redditAuthentication.getRedditClient(),
                                                    submission.getId(),
                                                    commentId);
                                        }
                                    });
                    }
                })
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
                            } else if (e instanceof ReplyToThreadException){
                                view.showReplyToSubmissionFailedToast();
                            } else if (e instanceof ReplyNotAvailableException) {
                                view.showSavedToast();
                            }
                        }
                    }
                })
        );
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

    public void stop() {
        view = null;
        if (disposables != null) {
            disposables.clear();
        }
    }

    private boolean isViewAttached() {
        return view != null;
    }
}
