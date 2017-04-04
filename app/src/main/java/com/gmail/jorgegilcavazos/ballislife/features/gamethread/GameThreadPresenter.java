package com.gmail.jorgegilcavazos.ballislife.features.gamethread;

import android.util.Log;

import com.gmail.jorgegilcavazos.ballislife.features.model.GameThreadSummary;
import com.gmail.jorgegilcavazos.ballislife.network.API.GameThreadFinderService;
import com.gmail.jorgegilcavazos.ballislife.network.API.RedditGameThreadsService;
import com.gmail.jorgegilcavazos.ballislife.network.API.RedditService;
import com.gmail.jorgegilcavazos.ballislife.util.DateFormatUtil;
import com.hannesdorfmann.mosby.mvp.MvpBasePresenter;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import net.dean.jraw.models.Comment;
import net.dean.jraw.models.CommentNode;
import net.dean.jraw.models.VoteDirection;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GameThreadPresenter extends MvpBasePresenter<GameThreadView> {

    private long gameDate;

    private RedditService redditService;
    private CompositeDisposable disposables;

    public GameThreadPresenter(long gameDate) {
        redditService = new RedditService();
        disposables = new CompositeDisposable();
        this.gameDate = gameDate;
    }

    @Override
    public void detachView(boolean retainInstance) {
        super.detachView(retainInstance);
        if (!retainInstance) {
            disposables.clear();
        }
    }

    public void loadComments(final String type,
                             final String homeTeamAbbr, final String awayTeamAbbr) {
        getView().setLoadingIndicator(true);
        getView().hideComments();
        getView().dismissSnackbar();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://nba-app-ca681.firebaseio.com/")
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RedditGameThreadsService gameThreadsService = retrofit
                .create(RedditGameThreadsService.class);

        disposables.clear();
        disposables.add(gameThreadsService.fetchGameThreads(
                DateFormatUtil.getNoDashDateString(new Date(gameDate)))
                .flatMap(new Function<List<GameThreadSummary>, SingleSource<String>>() {
                    @Override
                    public SingleSource<String> apply(List<GameThreadSummary> threads) throws Exception {
                        return GameThreadFinderService.findGameThreadInList(threads, type,
                                homeTeamAbbr, awayTeamAbbr);
                    }
                })
                .flatMap(new Function<String, SingleSource<List<CommentNode>>>() {
                    @Override
                    public SingleSource<List<CommentNode>> apply(String threadId) throws Exception {
                        if (threadId.equals("")) {
                            List<CommentNode> list = new ArrayList<>();
                            return Single.just(list);
                        }
                        return redditService.getComments(threadId, type);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<List<CommentNode>>() {
                    @Override
                    public void onSuccess(List<CommentNode> commentNodes) {
                        getView().setLoadingIndicator(false);
                        if(commentNodes.size() == 0) {
                            getView().showSnackbar(true);
                        } else {
                            getView().showComments(commentNodes);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        getView().setLoadingIndicator(false);
                        getView().showSnackbar(true);
                    }
                })
        );
    }

    public void dismissSnackbar() {
        if (isViewAttached()) {
            getView().dismissSnackbar();
        }
    }

    public void vote(Comment comment, VoteDirection voteDirection) {
        redditService.voteComment(comment, voteDirection);
    }

    public void save(Comment comment) {
        redditService.saveComment(comment);
    }

    public void reply(final int position, final Comment parentComment, final String text) {
        disposables.clear();
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
                            getView().showReplySavedToast();
                            if (comment != null) {
                                getView().addComment(position + 1, comment);
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (isViewAttached()) {
                            if (e instanceof RedditService.ReplyNotAvailableException) {
                                getView().showReplySavedToast();
                            } else {
                                getView().showReplyErrorToast();
                            }
                            Log.d("Presenter", e.toString());
                        }
                    }
                })
        );
    }
}
