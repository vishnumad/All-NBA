package com.gmail.jorgegilcavazos.ballislife.features.gamethread;

import android.util.Log;

import com.gmail.jorgegilcavazos.ballislife.features.model.GameThreadSummary;
import com.gmail.jorgegilcavazos.ballislife.network.API.GameThreadFinderService;
import com.gmail.jorgegilcavazos.ballislife.network.API.RedditGameThreadsService;
import com.gmail.jorgegilcavazos.ballislife.network.API.RedditService;
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

import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GameThreadPresenter {

    private long gameDate;

    private GameThreadView view;
    private RedditService redditService;
    private RedditGameThreadsService gameThreadsService;
    private CompositeDisposable disposables;

    public GameThreadPresenter(GameThreadView view, RedditService redditService, long gameDate) {
        this.view = view;
        this.redditService = redditService;
        this.gameDate = gameDate;
    }

    public void start() {
        redditService = new RedditService();
        disposables = new CompositeDisposable();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://nba-app-ca681.firebaseio.com/")
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        gameThreadsService = retrofit.create(RedditGameThreadsService.class);
    }

    public void loadComments(final String type, final String homeTeamAbbr,
                             final String awayTeamAbbr) {

        view.setLoadingIndicator(true);
        view.hideComments();
        view.hideText();

        disposables.add(gameThreadsService.fetchGameThreads(
                DateFormatUtil.getNoDashDateString(new Date(gameDate)))
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
                        return redditService.getComments(threadId, type);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<List<CommentNode>>() {
                    @Override
                    public void onSuccess(List<CommentNode> commentNodes) {
                        if (isViewAttached()) {
                            view.setLoadingIndicator(false);
                            if (commentNodes.size() == 0) {
                                view.showNoCommentsText();
                            } else {
                                view.showComments(commentNodes);
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (isViewAttached()) {
                            view.setLoadingIndicator(false);
                            if (e instanceof ThreadNotFoundException) {
                                view.showNoThreadText();
                            } else {
                                view.showFailedToLoadCommentsText();
                            }
                        }
                    }
                })
        );
    }

    public void vote(Comment comment, VoteDirection voteDirection) {
        disposables.add(redditService.voteComment(comment, voteDirection)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableCompletableObserver() {
                    @Override
                    public void onComplete() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        if (isViewAttached()) {
                            if (e instanceof NotLoggedInException) {
                                view.showNotLoggedInToast();
                            }
                        }
                    }
                })
        );
    }

    public void save(Comment comment) {
        view.showSavingToast();
        disposables.add(redditService.saveComment(comment)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableCompletableObserver() {
                    @Override
                    public void onComplete() {
                        if (isViewAttached()) {
                            view.showSavedToast();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (isViewAttached()) {
                            if (e instanceof NotLoggedInException) {
                                view.showNotLoggedInToast();
                            } else {
                                view.showFailedToSaveToast();
                            }
                        }
                    }
                })
        );
    }

    public void reply(final int position, final Comment parentComment, final String text) {
        view.showSavingToast();
        disposables.add(redditService.replyToComment(parentComment, text)
                .flatMap(new Function<String, SingleSource<CommentNode>>() {
                    @Override
                    public SingleSource<CommentNode> apply(String s) throws Exception {
                        return redditService.getComment(parentComment.getSubmissionId().substring(3), s);
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
        view.showSavingToast();
        disposables.add(gameThreadsService.fetchGameThreads(
                DateFormatUtil.getNoDashDateString(new Date(gameDate)))
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
                        return redditService.getSubmission(threadId, null);
                    }
                })
                .flatMap(new Function<Submission, SingleSource<CommentNode>>() {
                    @Override
                    public SingleSource<CommentNode> apply(final Submission submission) throws Exception {
                        return redditService.replyToThread(submission, text)
                                    .flatMap(new Function<String, SingleSource<CommentNode>>() {
                                        @Override
                                        public SingleSource<CommentNode> apply(String commentId) throws Exception {
                                            return redditService.getComment(submission.getId(), commentId);
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
                            } else if (e instanceof NotLoggedInException) {
                                view.showNotLoggedInToast();
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
