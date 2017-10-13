package com.gmail.jorgegilcavazos.ballislife.features.games;

import com.gmail.jorgegilcavazos.ballislife.base.BasePresenter;
import com.gmail.jorgegilcavazos.ballislife.data.repository.games.GamesRepository;
import com.gmail.jorgegilcavazos.ballislife.features.model.GameV2;
import com.gmail.jorgegilcavazos.ballislife.util.DateFormatUtil;
import com.gmail.jorgegilcavazos.ballislife.util.GameUtils;
import com.gmail.jorgegilcavazos.ballislife.util.NetworkUtils;
import com.gmail.jorgegilcavazos.ballislife.util.schedulers.BaseSchedulerProvider;

import java.util.Calendar;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;

public class GamesPresenter extends BasePresenter<GamesView> {

    private final GamesRepository gamesRepository;
    private final BaseSchedulerProvider schedulerProvider;
    private final CompositeDisposable disposables = new CompositeDisposable();

    @Inject
    public GamesPresenter(
            GamesRepository gamesRepository, BaseSchedulerProvider schedulerProvider) {
        this.gamesRepository = gamesRepository;
        this.schedulerProvider = schedulerProvider;
    }

    public void onDateChanged() {
        view.hideGames();
    }

    public void loadModels(Calendar selectedDate, boolean forceNetwork) {
        view.dismissSnackbar();
        loadDateNavigatorText(selectedDate);

        disposables.clear();
        disposables.add(gamesRepository.models(selectedDate, forceNetwork)
                                       .observeOn(schedulerProvider.ui(), true)
                                       .subscribeWith(new DisposableObserver<GamesUiModel>() {
                                           @Override
                                           public void onNext(GamesUiModel uiModel) {
                                               if (uiModel.isMemorySuccess() && uiModel.getGames()
                                                                                       .isEmpty()) {
                                                   view.setLoadingIndicator(true);
                                               }

                                               if (uiModel.isNetworkSuccess()) {
                                                   view.setLoadingIndicator(false);
                                               }

                                               if (uiModel.getGames() != null && !uiModel.getGames()
                                                                                         .isEmpty()) {
                                                   view.showGames(uiModel.getGames());
                                               }

                                               view.setNoGamesIndicator(uiModel.isNetworkSuccess
                                                       () && uiModel
                                                       .getGames()
                                                       .isEmpty());
                                           }

                                           @Override
                                           public void onError(Throwable e) {
                                               view.setLoadingIndicator(false);
                                               if (NetworkUtils.Companion.isNetworkAvailable()) {
                                                   view.showErrorSnackbar();
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
