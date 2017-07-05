package com.gmail.jorgegilcavazos.ballislife.features.games;

import com.gmail.jorgegilcavazos.ballislife.base.BasePresenter;
import com.gmail.jorgegilcavazos.ballislife.data.repository.games.GamesRepository;
import com.gmail.jorgegilcavazos.ballislife.features.model.NbaGame;
import com.gmail.jorgegilcavazos.ballislife.util.DateFormatUtil;
import com.gmail.jorgegilcavazos.ballislife.util.GameUtils;
import com.gmail.jorgegilcavazos.ballislife.util.schedulers.BaseSchedulerProvider;

import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;

public class GamesPresenter extends BasePresenter<GamesView> {

    GamesRepository gamesRepository;
    BaseSchedulerProvider schedulerProvider;
    private CompositeDisposable disposables;

    @Inject
    public GamesPresenter(GamesRepository gamesRepository,
                          BaseSchedulerProvider schedulerProvider) {
        this.gamesRepository = gamesRepository;
        this.schedulerProvider = schedulerProvider;
        disposables = new CompositeDisposable();
    }

    public void loadFirstAvailable(Calendar selectedDate) {
        List<NbaGame> nbaGames = gamesRepository
                .getCachedGames(DateFormatUtil.getNoDashDateString(selectedDate.getTime()));
        if (nbaGames == null || nbaGames.isEmpty()) {
            loadGames(selectedDate);
        } else {
            loadDateNavigatorText(selectedDate);
            view.setNoGamesIndicator(false);
            view.showGames(nbaGames);
            view.dismissSnackbar();
        }
    }

    public void loadGames(Calendar selectedDate) {
        view.dismissSnackbar();
        view.setNoGamesIndicator(false);
        view.setLoadingIndicator(true);
        view.hideGames();

        loadDateNavigatorText(selectedDate);

        disposables.clear();
        disposables.add(gamesRepository.getGames(DateFormatUtil
                .getNoDashDateString(selectedDate.getTime()))
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribeWith(new DisposableSingleObserver<List<NbaGame>>() {
                    @Override
                    public void onSuccess(List<NbaGame> games) {
                        if (games.isEmpty()) {
                            view.setNoGamesIndicator(true);
                        } else {
                            view.showGames(games);
                        }
                        view.setLoadingIndicator(false);
                    }

                    @Override
                    public void onError(Throwable e) {
                        view.setLoadingIndicator(false);
                        view.showSnackbar(true);
                    }
                })
        );
    }

    public void loadDateNavigatorText(Calendar selectedDate) {
        String dateText = DateFormatUtil.formatNavigatorDate(selectedDate.getTime());
        view.setDateNavigatorText(dateText);
    }

    public void openGameDetails(NbaGame requestedGame, Calendar selectedDate) {
        view.showGameDetails(requestedGame, selectedDate);
    }

    public void updateGames(String gameData, Calendar selectedDate) {
        if (DateFormatUtil.isDateToday(selectedDate.getTime())) {
            view.updateScores(GameUtils.getGamesListFromJson(gameData));
        }
    }

    public void stop() {
        if (disposables != null) {
            disposables.clear();
        }
    }

}
