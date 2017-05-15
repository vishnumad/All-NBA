package com.gmail.jorgegilcavazos.ballislife.features.profile;

import android.content.SharedPreferences;

import com.gmail.jorgegilcavazos.ballislife.base.BasePresenter;
import com.gmail.jorgegilcavazos.ballislife.data.API.RedditService;
import com.gmail.jorgegilcavazos.ballislife.data.RedditAuthentication;
import com.gmail.jorgegilcavazos.ballislife.util.schedulers.BaseSchedulerProvider;

import net.dean.jraw.models.Contribution;
import net.dean.jraw.models.Listing;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;

public class ProfilePresenter extends BasePresenter<ProfileView> {

    private CompositeDisposable disposables;
    private SharedPreferences preferences;
    private RedditService redditService;
    private BaseSchedulerProvider schedulerProvider;

    public ProfilePresenter(RedditService redditService, SharedPreferences preferences,
                            CompositeDisposable disposables, BaseSchedulerProvider schedulerProvider) {
        this.redditService = redditService;
        this.preferences = preferences;
        this.disposables = disposables;
        this.schedulerProvider = schedulerProvider;

    }

    public void loadUserDetails() {
        view.setLoadingIndicator(true);
        view.dismissSnackbar();
        view.hideContent();

        disposables.clear();
        disposables.add(RedditAuthentication.getInstance().authenticate(preferences)
                .andThen(redditService.getUserContributions())
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribeWith(new DisposableObserver<Listing<Contribution>>() {
                    @Override
                    public void onNext(Listing<Contribution> contributions) {
                        view.setLoadingIndicator(false);
                        view.showContent(contributions);
                    }

                    @Override
                    public void onError(Throwable e) {
                        view.setLoadingIndicator(false);
                        view.showSnackbar(true);
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
