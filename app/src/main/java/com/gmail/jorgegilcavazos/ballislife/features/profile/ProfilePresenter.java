package com.gmail.jorgegilcavazos.ballislife.features.profile;

import com.gmail.jorgegilcavazos.ballislife.base.BasePresenter;
import com.gmail.jorgegilcavazos.ballislife.data.reddit.RedditAuthentication;
import com.gmail.jorgegilcavazos.ballislife.data.repository.profile.ProfileRepository;
import com.gmail.jorgegilcavazos.ballislife.util.exception.NotAuthenticatedException;
import com.gmail.jorgegilcavazos.ballislife.util.schedulers.BaseSchedulerProvider;

import net.dean.jraw.models.Comment;
import net.dean.jraw.models.Contribution;
import net.dean.jraw.models.PublicContribution;
import net.dean.jraw.paginators.Sorting;
import net.dean.jraw.paginators.TimePeriod;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.observers.DisposableSingleObserver;

public class ProfilePresenter extends BasePresenter<ProfileView> {

    private CompositeDisposable disposables;
    private ProfileRepository profileRepository;
    private RedditAuthentication redditAuthentication;
    private BaseSchedulerProvider schedulerProvider;

    @Inject
    public ProfilePresenter(ProfileRepository profileRepository,
                            RedditAuthentication redditAuthentication,
                            BaseSchedulerProvider schedulerProvider) {
        this.profileRepository = profileRepository;
        this.redditAuthentication = redditAuthentication;
        this.schedulerProvider = schedulerProvider;

        disposables = new CompositeDisposable();
    }

    public void setLimit(int limit) {
        profileRepository.setLimit(limit);
    }

    public void setSorting(Sorting sorting) {
        profileRepository.setSorting(sorting);
    }

    public void setTimePeriod(TimePeriod timePeriod) {
        profileRepository.setTimePeriod(timePeriod);
    }

    public void loadFirstAvailable() {
        List<Contribution> contributions = profileRepository.getCachedContributions();
        if (contributions.isEmpty()) {
            loadContributions(true /* reset */);
        } else {
            view.showContent(contributions, true /* clear */);
        }
    }

    public void loadContributions(final boolean reset) {
        if (reset) {
            view.setLoadingIndicator(true);
            view.resetScrollingState();
            profileRepository.reset();
        }

        view.dismissSnackbar();

        disposables.add(redditAuthentication.authenticate()
                .andThen(profileRepository.next())
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribeWith(new DisposableSingleObserver<List<Contribution>>() {
                    @Override
                    public void onSuccess(List<Contribution> contributions) {
                        if (contributions.isEmpty()) {
                            view.showNothingToShowSnackbar();
                            view.hideContent();
                        } else {
                            view.showContent(contributions, reset);
                        }

                        if (reset) {
                            view.scrollToTop();
                            view.setLoadingIndicator(false);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (e instanceof NotAuthenticatedException) {
                            view.showNotAuthenticatedToast();
                        } else {
                            view.showContributionsLoadingFailedSnackbar(reset);
                        }

                        if (reset) {
                            view.setLoadingIndicator(false);
                            view.hideContent();
                        }
                    }
                })
        );
    }

    public void observeContributionsClicks(Observable<PublicContribution> contributionObservable) {
        disposables.add(contributionObservable
                .subscribeOn(schedulerProvider.ui())
                .observeOn(schedulerProvider.ui())
                .subscribeWith(new DisposableObserver<Contribution>() {
                    @Override
                    public void onNext(Contribution contribution) {
                        if (contribution instanceof Comment) {
                            String submissionId = ((Comment) contribution).getSubmissionId()
                                    .substring(3);
                            view.openSubmissionAndScrollToComment(submissionId,
                                    contribution.getId());
                        } else {
                            view.openSubmission(contribution.getId());
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        view.showUnknownErrorToast(e);
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
