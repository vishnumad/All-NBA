package com.gmail.jorgegilcavazos.ballislife.features.games;

import com.gmail.jorgegilcavazos.ballislife.features.model.DayGames;
import com.gmail.jorgegilcavazos.ballislife.features.model.NbaGame;
import com.gmail.jorgegilcavazos.ballislife.data.API.NbaGamesService;
import com.gmail.jorgegilcavazos.ballislife.util.DateFormatUtil;
import com.gmail.jorgegilcavazos.ballislife.util.GameUtils;
import com.hannesdorfmann.mosby.mvp.MvpBasePresenter;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import java.util.Calendar;
import java.util.List;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GamesPresenter extends MvpBasePresenter<GamesView> {

    private List<NbaGame> gamesList;
    private Calendar selectedDate;

    private CompositeDisposable disposables;

    public GamesPresenter() {
        disposables = new CompositeDisposable();
        selectedDate = Calendar.getInstance();
    }

    @Override
    public void detachView(boolean retainInstance) {
        super.detachView(retainInstance);
        if (!retainInstance) {
            disposables.clear();
        }
    }

    public void loadGames() {
        loadDateNavigatorText();
        getView().setLoadingIndicator(true);
        getView().dismissSnackbar();
        getView().hideGames();
        getView().setNoGamesIndicator(false);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://nba-app-ca681.firebaseio.com/")
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        NbaGamesService gamesService = retrofit.create(NbaGamesService.class);

        Single<DayGames> dayGamesSingle = gamesService.getDayGames(
                DateFormatUtil.getNoDashDateString(selectedDate.getTime()));

        disposables.clear();
        disposables.add(dayGamesSingle
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<DayGames>() {
                    @Override
                    public void onSuccess(DayGames dayGames) {
                        getView().setLoadingIndicator(false);
                        if (dayGames.getNum_games() == 0) {
                            getView().setNoGamesIndicator(true);
                        } else {
                            if (dayGames.getGames() != null && dayGames.getGames().size() > 0) {
                                getView().showGames(dayGames.getGames());
                            } else {
                                getView().showSnackbar(true);
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        getView().setLoadingIndicator(false);
                        getView().showSnackbar(true);
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
        if (isViewAttached()) {
            getView().setDateNavigatorText(dateText);
        }
    }

    public void openGameDetails(NbaGame requestedGame) {
        if (isViewAttached()) {
            getView().showGameDetails(requestedGame, selectedDate);
        }
    }

    public void updateGames(String gameData) {
        if (DateFormatUtil.isDateToday(selectedDate.getTime())) {
            if (isViewAttached()) {
                getView().updateScores(GameUtils.getGamesListFromJson(gameData));
            }
        }
    }

    public void dismissSnackbar() {
        if (isViewAttached()) {
            getView().dismissSnackbar();
        }
    }

}
