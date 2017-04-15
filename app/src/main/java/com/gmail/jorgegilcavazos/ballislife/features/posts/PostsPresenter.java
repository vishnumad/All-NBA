package com.gmail.jorgegilcavazos.ballislife.features.posts;

import android.content.SharedPreferences;

import com.gmail.jorgegilcavazos.ballislife.base.BasePresenter;
import com.gmail.jorgegilcavazos.ballislife.features.model.SubscriberCount;
import com.gmail.jorgegilcavazos.ballislife.features.model.wrapper.CustomSubmission;
import com.gmail.jorgegilcavazos.ballislife.network.API.RedditService;
import com.gmail.jorgegilcavazos.ballislife.network.RedditAuthentication;
import com.gmail.jorgegilcavazos.ballislife.util.exception.NotAuthenticatedException;
import com.gmail.jorgegilcavazos.ballislife.util.schedulers.BaseSchedulerProvider;

import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.VoteDirection;
import net.dean.jraw.paginators.Sorting;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.observers.DisposableSingleObserver;

public class PostsPresenter extends BasePresenter<PostsView> {

    private RedditService service;
    private SharedPreferences preferences;
    private CompositeDisposable disposables;
    private BaseSchedulerProvider schedulerProvider;

    public PostsPresenter(RedditService service, SharedPreferences preferences,
                          BaseSchedulerProvider schedulerProvider) {
        this.service = service;
        this.preferences = preferences;
        this.schedulerProvider = schedulerProvider;

        disposables = new CompositeDisposable();
    }

    public void loadSubscriberCount() {
        disposables.add(RedditAuthentication.getInstance().authenticate(preferences)
                .andThen(service.getSubscriberCount("nba"))
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

    public void loadPosts() {
        view.setLoadingIndicator(true);
        view.dismissSnackbar();
        disposables.add(RedditAuthentication.getInstance().authenticate(preferences)
                .andThen(service.getSubmissionListing("nba", 25, Sorting.HOT))
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribeWith(new DisposableSingleObserver<Listing<Submission>>() {
                    @Override
                    public void onSuccess(Listing<Submission> submissions) {
                        List<CustomSubmission> customSubmissions = new ArrayList<>();
                        for (Submission submission : submissions) {
                            customSubmissions.add(new CustomSubmission(submission,
                                    submission.getVote(), submission.isSaved()));
                        }

                        if (isViewAttached()) {
                            view.showPosts(customSubmissions);
                            view.setLoadingIndicator(false);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (isViewAttached()) {
                            if (e instanceof NotAuthenticatedException) {
                                view.showNotAuthenticatedToast();
                                // TODO: do something about it. Re-start auth?
                            } else {
                                view.showPostsLoadingFailedSnackbar();
                            }
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

    public void stop() {
        if (disposables != null) {
            disposables.clear();
        }
    }
}
