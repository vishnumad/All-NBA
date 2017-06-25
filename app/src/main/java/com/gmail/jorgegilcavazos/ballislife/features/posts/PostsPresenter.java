package com.gmail.jorgegilcavazos.ballislife.features.posts;

import android.content.SharedPreferences;

import com.gmail.jorgegilcavazos.ballislife.base.BasePresenter;
import com.gmail.jorgegilcavazos.ballislife.data.API.RedditService;
import com.gmail.jorgegilcavazos.ballislife.data.RedditAuthentication;
import com.gmail.jorgegilcavazos.ballislife.data.local.LocalRepository;
import com.gmail.jorgegilcavazos.ballislife.data.repository.posts.PostsRepository;
import com.gmail.jorgegilcavazos.ballislife.features.application.BallIsLifeApplication;
import com.gmail.jorgegilcavazos.ballislife.features.model.SubscriberCount;
import com.gmail.jorgegilcavazos.ballislife.features.model.wrapper.CustomSubmission;
import com.gmail.jorgegilcavazos.ballislife.util.Constants;
import com.gmail.jorgegilcavazos.ballislife.util.Utilities;
import com.gmail.jorgegilcavazos.ballislife.util.exception.NotAuthenticatedException;
import com.gmail.jorgegilcavazos.ballislife.util.schedulers.BaseSchedulerProvider;

import net.dean.jraw.models.Submission;
import net.dean.jraw.models.VoteDirection;
import net.dean.jraw.paginators.Sorting;
import net.dean.jraw.paginators.TimePeriod;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.observers.DisposableSingleObserver;

public class PostsPresenter extends BasePresenter<PostsView> {

    private static final String TAG = "PostsPresenter";

    @Inject
    RedditService service;

    private LocalRepository localRepository;
    private PostsRepository postsRepository;
    private SharedPreferences preferences;
    private CompositeDisposable disposables;
    private BaseSchedulerProvider schedulerProvider;
    private String subreddit;

    public PostsPresenter(String subreddit,
                          LocalRepository localRepository,
                          PostsRepository postsRepository,
                          SharedPreferences preferences,
                          BaseSchedulerProvider schedulerProvider) {
        BallIsLifeApplication.getAppComponent().inject(this);
        this.subreddit = subreddit;
        this.localRepository = localRepository;
        this.postsRepository = postsRepository;
        this.preferences = preferences;
        this.schedulerProvider = schedulerProvider;

        disposables = new CompositeDisposable();
    }

    public void loadSubscriberCount() {
        disposables.add(RedditAuthentication.getInstance().authenticate(preferences)
                .andThen(service.getSubscriberCount(subreddit))
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
            view.showPosts(submissions, false /* clear */);
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

        disposables.add(RedditAuthentication.getInstance().authenticate(preferences)
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

    public void onVote(Submission submission, VoteDirection direction) {
        if (!RedditAuthentication.getInstance().isUserLoggedIn()) {
            view.showNotLoggedInToast();
            return;
        }

        disposables.add(RedditAuthentication.getInstance().authenticate(preferences)
                .andThen(service.voteSubmission(submission, direction))
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

    public void onSave(Submission submission, boolean saved) {
        if (!RedditAuthentication.getInstance().isUserLoggedIn()) {
            view.showNotLoggedInToast();
            return;
        }

        disposables.add(RedditAuthentication.getInstance().authenticate(preferences)
                .andThen(service.saveSubmission(submission, saved))
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

    public void stop() {
        if (disposables != null) {
            disposables.clear();
        }
    }
}
