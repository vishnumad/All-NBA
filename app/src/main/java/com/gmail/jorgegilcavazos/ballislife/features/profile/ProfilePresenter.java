package com.gmail.jorgegilcavazos.ballislife.features.profile;

import android.content.SharedPreferences;

import com.gmail.jorgegilcavazos.ballislife.base.BasePresenter;
import com.gmail.jorgegilcavazos.ballislife.data.reddit.RedditAuthentication;
import com.gmail.jorgegilcavazos.ballislife.data.repository.profile.ProfileRepository;
import com.gmail.jorgegilcavazos.ballislife.util.exception.NotAuthenticatedException;
import com.gmail.jorgegilcavazos.ballislife.util.schedulers.BaseSchedulerProvider;

import net.dean.jraw.models.Contribution;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;

public class ProfilePresenter extends BasePresenter<ProfileView> {

    private CompositeDisposable disposables;
    private ProfileRepository profileRepository;
    private RedditAuthentication redditAuthentication;
    private SharedPreferences redditSharedPreferences;
    private BaseSchedulerProvider schedulerProvider;

    @Inject
    public ProfilePresenter(ProfileRepository profileRepository,
                            RedditAuthentication redditAuthentication,
                            @Named("redditSharedPreferences") SharedPreferences redditPreferences,
                            BaseSchedulerProvider schedulerProvider) {
        this.profileRepository = profileRepository;
        this.redditAuthentication = redditAuthentication;
        this.redditSharedPreferences = redditPreferences;
        this.schedulerProvider = schedulerProvider;

        disposables = new CompositeDisposable();
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

        disposables.clear();
        disposables.add(redditAuthentication.authenticate(redditSharedPreferences)
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

    public void stop() {
        if (disposables != null) {
            disposables.clear();
        }
    }
}
