package com.gmail.jorgegilcavazos.ballislife.features.posts;

import android.content.SharedPreferences;

import com.gmail.jorgegilcavazos.ballislife.base.BasePresenter;
import com.gmail.jorgegilcavazos.ballislife.data.local.LocalRepository;
import com.gmail.jorgegilcavazos.ballislife.data.reddit.RedditAuthentication;
import com.gmail.jorgegilcavazos.ballislife.data.repository.posts.PostsRepository;
import com.gmail.jorgegilcavazos.ballislife.data.service.RedditService;
import com.gmail.jorgegilcavazos.ballislife.features.model.SubscriberCount;
import com.gmail.jorgegilcavazos.ballislife.features.model.wrapper.CustomSubmission;
import com.gmail.jorgegilcavazos.ballislife.util.Constants;
import com.gmail.jorgegilcavazos.ballislife.util.Utilities;
import com.gmail.jorgegilcavazos.ballislife.util.exception.NotAuthenticatedException;
import com.gmail.jorgegilcavazos.ballislife.util.exception.NotLoggedInException;
import com.gmail.jorgegilcavazos.ballislife.util.schedulers.BaseSchedulerProvider;

import net.dean.jraw.models.Submission;
import net.dean.jraw.models.VoteDirection;
import net.dean.jraw.paginators.Sorting;
import net.dean.jraw.paginators.TimePeriod;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import io.reactivex.CompletableSource;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.observers.DisposableSingleObserver;

public class PostsPresenter extends BasePresenter<PostsView> {

    private static final String TAG = "PostsPresenter";

    private RedditAuthentication redditAuthentication;
    private LocalRepository localRepository;
    private SharedPreferences redditPrefs;
    private PostsRepository postsRepository;
    private RedditService service;
    private BaseSchedulerProvider schedulerProvider;

    private CompositeDisposable disposables;
    private String subreddit;

    @Inject
    public PostsPresenter(
            RedditAuthentication redditAuthentication,
            LocalRepository localRepository,
            @Named("redditSharedPreferences") SharedPreferences redditPrefs,
            PostsRepository postsRepository,
            RedditService redditService,
            BaseSchedulerProvider schedulerProvider) {
        this.redditAuthentication = redditAuthentication;
        this.localRepository = localRepository;
        this.redditPrefs = redditPrefs;
        this.postsRepository = postsRepository;
        this.service = redditService;
        this.schedulerProvider = schedulerProvider;

        disposables = new CompositeDisposable();
    }

    public void setSubreddit(String subreddit) {
        this.subreddit = subreddit;
    }

    public void loadSubscriberCount() {
        disposables.add(redditAuthentication.authenticate(redditPrefs)
                .andThen(service.getSubscriberCount(redditAuthentication.getRedditClient(),
                        subreddit))
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribeWith(new DisposableSingleObserver<SubscriberCount>() {
                    @Override
                    public void onSuccess(SubscriberCount subscriberCount) {
                        if (isViewAttached()) {
                            view.showSubscribers(subscriberCount);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                })
        );
    }

    public void loadFirstAvailable(Sorting sorting, TimePeriod timePeriod) {
        List<CustomSubmission> submissions = postsRepository.getCachedSubmissions();
        if (submissions.isEmpty()) {
            resetLoaderFromStartWithParams(sorting, timePeriod);
            loadPosts(true /* reset */);
        } else {
            view.showPosts(submissions, true /* clear */);
        }
    }

    /**
     * Must be called before loadPosts(reset = true).
     */
    public void resetLoaderFromStartWithParams(Sorting sorting, TimePeriod timePeriod) {
        postsRepository.reset(sorting, timePeriod, subreddit);
    }

    public void loadPosts(final boolean reset) {
        if (reset) {
            view.resetScrollState();
            view.setLoadingIndicator(true);
        }

        view.dismissSnackbar();

        disposables.add(redditAuthentication.authenticate(redditPrefs)
                .andThen(postsRepository.next())
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribeWith(new DisposableSingleObserver<List<CustomSubmission>>() {
                    @Override
                    public void onSuccess(List<CustomSubmission> submissions) {
                        if (submissions.isEmpty()) {
                            view.showNothingToShowToast();
                            return;
                        }

                        view.showPosts(submissions, reset);
                        if (reset) {
                            view.scrollToTop();
                        }

                        if (reset) {
                            view.setLoadingIndicator(false);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (e instanceof NotAuthenticatedException) {
                            view.showNotAuthenticatedToast();
                        } else {
                            view.showPostsLoadingFailedSnackbar(reset);
                        }

                        if (reset) {
                            view.setLoadingIndicator(false);
                        }
                    }
                })
        );
    }

    public void onVote(final Submission submission, final VoteDirection direction) {
        if (!redditAuthentication.isUserLoggedIn()) {
            view.showNotLoggedInToast();
            return;
        }

        disposables.add(redditAuthentication.authenticate(redditPrefs)
                .andThen(redditAuthentication.checkUserLoggedIn())
                .flatMapCompletable(new Function<Boolean, CompletableSource>() {
                    @Override
                    public CompletableSource apply(Boolean loggedIn) throws Exception {
                        if (loggedIn) {
                            return service.voteSubmission(
                                    redditAuthentication.getRedditClient(),
                                    submission,
                                    direction);
                        } else {
                            throw new NotLoggedInException();
                        }
                    }
                })
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
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

    public void onSave(final Submission submission, final boolean saved) {
        if (!redditAuthentication.isUserLoggedIn()) {
            view.showNotLoggedInToast();
            return;
        }

        disposables.add(redditAuthentication.authenticate(redditPrefs)
                .andThen(redditAuthentication.checkUserLoggedIn())
                .flatMapCompletable(new Function<Boolean, CompletableSource>() {
                    @Override
                    public CompletableSource apply(Boolean loggedIn) throws Exception {
                        if (loggedIn) {
                            return service.saveSubmission(
                                    redditAuthentication.getRedditClient(),
                                    submission,
                                    saved);
                        } else {
                            throw new NotLoggedInException();
                        }
                    }
                })
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
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

    public void onContentClick(String url) {
        if (url != null) {
            if (url.contains(Constants.STREAMABLE_DOMAIN)) {
                String shortCode = Utilities.getStreamableShortcodeFromUrl(url);
                if (shortCode != null) {
                    view.openStreamable(shortCode);
                } else {
                    view.openContentTab(url);
                }
            } else {
                view.openContentTab(url);
            }
        } else {
            view.showContentUnavailableToast();
        }
    }

    public void onViewTypeSelected(int viewType) {
        localRepository.saveFavoritePostsViewType(viewType);
        view.changeViewType(viewType);
    }

    public void subscribeToSubmissionShare(Observable<Submission> shareObservable) {
        disposables.add(shareObservable
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribeWith(new DisposableObserver<Submission>() {
                    @Override
                    public void onNext(Submission submission) {
                        if (submission.isSelfPost()) {
                            view.share(Constants.HTTPS + Constants.REDDIT_DOMAIN +
                                    submission.getPermalink());
                        } else {
                            view.share(submission.getUrl());
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        view.showUnknownErrorToast();
                    }

                    @Override
                    public void onComplete() {

                    }
                })
        );
    }

    public void stop() {
        if (disposables != null) {
            disposables.clear();
        }
    }
}
