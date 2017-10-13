package com.gmail.jorgegilcavazos.ballislife.features.games;

import com.gmail.jorgegilcavazos.ballislife.base.BasePresenter;
import com.gmail.jorgegilcavazos.ballislife.data.repository.games.GamesRepository;
import com.gmail.jorgegilcavazos.ballislife.features.model.GameV2;
import com.gmail.jorgegilcavazos.ballislife.util.DateFormatUtil;
import com.gmail.jorgegilcavazos.ballislife.util.GameUtils;
import com.gmail.jorgegilcavazos.ballislife.util.NetworkUtils;
import com.gmail.jorgegilcavazos.ballislife.util.schedulers.BaseSchedulerProvider;
import com.google.firebase.crash.FirebaseCrash;

import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;

public class GamesPresenter extends BasePresenter<GamesView> {

    private final GamesRepository gamesRepository;
    private final BaseSchedulerProvider schedulerProvider;
    private final CompositeDisposable disposables = new CompositeDisposable();

    @Inject
    public GamesPresenter(GamesRepository gamesRepository,
                          BaseSchedulerProvider schedulerProvider) {
        this.gamesRepository = gamesRepository;
        this.schedulerProvider = schedulerProvider;
    }

    public void loadGames(Calendar selectedDate, boolean forceReload) {
        view.dismissSnackbar();
        view.setNoGamesIndicator(false);
        view.setLoadingIndicator(true);
        view.hideGames();

        loadDateNavigatorText(selectedDate);

        disposables.clear();
        disposables.add(gamesRepository.getGames(selectedDate, forceReload)
                                .subscribeOn(schedulerProvider.io())
                                .observeOn(schedulerProvider.ui(), true)
                                .subscribeWith(new DisposableObserver<List<GameV2>>() {
                                    @Override
                                    public void onNext(List<GameV2> games) {
                                        view.showGames(games);
                                        view.setLoadingIndicator(false);
                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        view.setLoadingIndicator(false);
                                        if (NetworkUtils.Companion.isNetworkAvailable()) {
                                            view.showErrorSnackbar();
                                            FirebaseCrash.log("Error getting games...");
                                            FirebaseCrash.report(e);
                                        } else {
                                            view.showNoNetSnackbar();
                                        }
                                    }

                                    @Override
                                    public void onComplete() {

                                    }
                                }));
    }

    public void loadDateNavigatorText(Calendar selectedDate) {
        String dateText = DateFormatUtil.formatNavigatorDate(selectedDate.getTime());
        view.setDateNavigatorText(dateText);
    }

    public void openGameDetails(GameV2 requestedGame, Calendar selectedDate) {
        view.showGameDetails(requestedGame, selectedDate);
    }

    public void updateGames(String gameData, Calendar selectedDate) {
        if (DateFormatUtil.isDateToday(selectedDate.getTime())) {
            view.updateScores(GameUtils.getGamesListFromJson(gameData));
        }
    }

    public void stop() {
        disposables.clear();
        view.dismissSnackbar();
    }

}
