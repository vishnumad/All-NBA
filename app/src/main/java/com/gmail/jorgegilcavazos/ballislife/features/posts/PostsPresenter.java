package com.gmail.jorgegilcavazos.ballislife.features.posts;

import android.content.SharedPreferences;

import com.gmail.jorgegilcavazos.ballislife.base.BasePresenter;
import com.gmail.jorgegilcavazos.ballislife.data.local.LocalRepository;
import com.gmail.jorgegilcavazos.ballislife.features.model.SubscriberCount;
import com.gmail.jorgegilcavazos.ballislife.features.model.wrapper.CustomSubmission;
import com.gmail.jorgegilcavazos.ballislife.data.API.RedditService;
import com.gmail.jorgegilcavazos.ballislife.data.RedditAuthentication;
import com.gmail.jorgegilcavazos.ballislife.util.Constants;
import com.gmail.jorgegilcavazos.ballislife.util.Utilities;
import com.gmail.jorgegilcavazos.ballislife.util.exception.NotAuthenticatedException;
import com.gmail.jorgegilcavazos.ballislife.util.schedulers.BaseSchedulerProvider;

import net.dean.jraw.RedditClient;
import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.VoteDirection;
import net.dean.jraw.paginators.Sorting;
import net.dean.jraw.paginators.SubredditPaginator;
import net.dean.jraw.paginators.TimePeriod;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.observers.DisposableSingleObserver;

public class PostsPresenter extends BasePresenter<PostsView> {

    private LocalRepository localRepository;
    private RedditService service;
    private SharedPreferences preferences;
    private CompositeDisposable disposables;
    private BaseSchedulerProvider schedulerProvider;
    private SubredditPaginator paginator;
    private String subreddit;

    public PostsPresenter(String subreddit, LocalRepository localRepository, RedditService service, SharedPreferences preferences,
                          BaseSchedulerProvider schedulerProvider) {
        this.subreddit = subreddit;
        this.localRepository = localRepository;
        this.service = service;
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

    public void loadPosts(Sorting sorting, TimePeriod timePeriod) {
        view.setLoadingIndicator(true);
        view.dismissSnackbar();

        RedditClient redditClient = RedditAuthentication.getInstance()
                .getRedditClient();
        paginator = new SubredditPaginator(redditClient, subreddit);
        paginator.setLimit(25);
        paginator.setSorting(sorting);

        if (sorting == Sorting.TOP) {
            paginator.setTimePeriod(timePeriod);
        }

        disposables.add(RedditAuthentication.getInstance().authenticate(preferences)
                .andThen(service.getSubmissionListing(paginator))
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribeWith(new DisposableSingleObserver<Listing<Submission>>() {
                    @Override
                    public void onSuccess(Listing<Submission> submissions) {
                        if (submissions.isEmpty()) {
                            view.showNothingToShowToast();
                            return;
                        }

                        List<CustomSubmission> customSubmissions = new ArrayList<>();
                        for (Submission submission : submissions) {
                            customSubmissions.add(new CustomSubmission(submission,
                                    submission.getVote(), submission.isSaved()));
                        }

                        view.showPosts(customSubmissions);
                        view.setLoadingIndicator(false);
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (e instanceof NotAuthenticatedException) {
                            view.showNotAuthenticatedToast();
                        } else {
                            view.showPostsLoadingFailedSnackbar(PostsFragment.TYPE_FIRST_LOAD);
                        }
                        view.setLoadingIndicator(false);
                    }
                })
        );
    }

    public void loadMorePosts() {
        if (paginator == null) {
            return;
        }

        disposables.add(RedditAuthentication.getInstance().authenticate(preferences)
                .andThen(service.getSubmissionListing(paginator))
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribeWith(new DisposableSingleObserver<Listing<Submission>>() {
                    @Override
                    public void onSuccess(Listing<Submission> submissions) {
                        if (submissions.isEmpty()) {
                            view.showNothingToShowToast();
                            return;
                        }

                        List<CustomSubmission> customSubmissions = new ArrayList<>();
                        for (Submission submission : submissions) {
                            customSubmissions.add(new CustomSubmission(submission,
                                    submission.getVote(), submission.isSaved()));
                        }
                        view.addPosts(customSubmissions);
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (e instanceof NotAuthenticatedException) {
                            view.showNotAuthenticatedToast();
                        } else {
                            view.showPostsLoadingFailedSnackbar(PostsFragment.TYPE_LOAD_MORE);
                        }
                        view.setLoadingFailed(true);
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
