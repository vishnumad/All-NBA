package com.gmail.jorgegilcavazos.ballislife.features.games;

import com.gmail.jorgegilcavazos.ballislife.base.BasePresenter;
import com.gmail.jorgegilcavazos.ballislife.features.model.DayGames;
import com.gmail.jorgegilcavazos.ballislife.features.model.NbaGame;
import com.gmail.jorgegilcavazos.ballislife.data.API.NbaGamesService;
import com.gmail.jorgegilcavazos.ballislife.util.DateFormatUtil;
import com.gmail.jorgegilcavazos.ballislife.util.GameUtils;
import com.gmail.jorgegilcavazos.ballislife.util.schedulers.BaseSchedulerProvider;

import java.util.Calendar;
import java.util.List;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class GamesPresenter extends BasePresenter<GamesView> {

    private NbaGamesService nbaGamesService;
    private BaseSchedulerProvider schedulerProvider;
    private List<NbaGame> gamesList;
    private Calendar selectedDate;
    private CompositeDisposable disposables;

    public GamesPresenter(NbaGamesService nbaGamesService, BaseSchedulerProvider schedulerProvider) {
        this.nbaGamesService = nbaGamesService;
        this.schedulerProvider = schedulerProvider;

        disposables = new CompositeDisposable();
        selectedDate = Calendar.getInstance();
    }

    public void loadGames() {
        loadDateNavigatorText();
        view.setLoadingIndicator(true);
        view.dismissSnackbar();
        view.hideGames();
        view.setNoGamesIndicator(false);

        Single<DayGames> dayGamesSingle = nbaGamesService.getDayGames(
                DateFormatUtil.getNoDashDateString(selectedDate.getTime()));

        disposables.clear();
        disposables.add(dayGamesSingle
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<DayGames>() {
                    @Override
                    public void onSuccess(DayGames dayGames) {
                        view.setLoadingIndicator(false);
                        if (dayGames.getNum_games() == 0) {
                            view.setNoGamesIndicator(true);
                        } else {
                            if (dayGames.getGames() != null && dayGames.getGames().size() > 0) {
                                view.showGames(dayGames.getGames());
                            } else {
                                view.showSnackbar(true);
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        view.setLoadingIndicator(false);
                        view.showSnackbar(true);
                    }
                })
        );
    }

    public void addOrSubstractDay(int delta) {
        selectedDate.add(Calendar.DAY_OF_YEAR, delta);
        loadDateNavigatorText();
        loadGames();
    }

    public void loadDateNavigatorText() {
        String dateText = DateFormatUtil.formatNavigatorDate(selectedDate.getTime());
        view.setDateNavigatorText(dateText);
    }

    public void openGameDetails(NbaGame requestedGame) {
        view.showGameDetails(requestedGame, selectedDate);
    }

    public void updateGames(String gameData) {
        if (DateFormatUtil.isDateToday(selectedDate.getTime())) {
            view.updateScores(GameUtils.getGamesListFromJson(gameData));
        }
    }

    public void dismissSnackbar() {
        view.dismissSnackbar();
    }

    public void stop() {
        if (disposables != null) {
            disposables.clear();
        }
    }

}
