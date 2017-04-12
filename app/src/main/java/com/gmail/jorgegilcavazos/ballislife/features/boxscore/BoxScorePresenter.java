package com.gmail.jorgegilcavazos.ballislife.features.boxscore;

import com.gmail.jorgegilcavazos.ballislife.features.model.BoxScoreValues;
import com.gmail.jorgegilcavazos.ballislife.network.API.NbaGamesService;
import com.gmail.jorgegilcavazos.ballislife.util.schedulers.BaseSchedulerProvider;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;

public class BoxScorePresenter {

    private BoxScoreView view;
    private NbaGamesService nbaService;
    private BaseSchedulerProvider schedulerProvider;
    private CompositeDisposable disposables;

    public BoxScorePresenter(BoxScoreView view, NbaGamesService nbaService,
                             BaseSchedulerProvider schedulerProvider) {
        this.view = view;
        this.nbaService = nbaService;
        this.schedulerProvider = schedulerProvider;
    }

    public void start() {
        disposables = new CompositeDisposable();
    }

    public void loadBoxScore(String gameId, final int teamSelected) {
        view.setLoadingIndicator(true);
        view.hideBoxScore();
        view.hideLoadMessage();
        disposables.clear();
        disposables.add(nbaService.boxScore(gameId)
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribeWith(new DisposableSingleObserver<BoxScoreValues>() {
                    @Override
                    public void onSuccess(BoxScoreValues boxScoreValues) {
                        if (isViewAttached()) {
                            if (boxScoreValues.getHls().getPstsg() != null) {
                                if (teamSelected == BoxScoreFragment.LOAD_AWAY) {
                                    view.showVisitorBoxScore(boxScoreValues);
                                } else {
                                    view.showHomeBoxScore(boxScoreValues);
                                }
                            } else {
                                view.showBoxScoreNotAvailableMessage();
                            }
                            view.setLoadingIndicator(false);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (isViewAttached()) {
                            view.showLoadingBoxScoreErrorMessage();
                            view.setLoadingIndicator(false);
                        }
                    }
                })
        );
    }

    public void stop() {
        view = null;
        if (disposables != null) {
            disposables.clear();
        }
    }

    private boolean isViewAttached() {
        return view != null;
    }
}
