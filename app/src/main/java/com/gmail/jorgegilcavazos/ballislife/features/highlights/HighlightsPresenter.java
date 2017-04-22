package com.gmail.jorgegilcavazos.ballislife.features.highlights;

import android.util.Log;

import com.gmail.jorgegilcavazos.ballislife.base.BasePresenter;
import com.gmail.jorgegilcavazos.ballislife.features.model.Highlight;
import com.gmail.jorgegilcavazos.ballislife.network.API.HighlightsService;
import com.gmail.jorgegilcavazos.ballislife.util.schedulers.BaseSchedulerProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.observers.DisposableSingleObserver;

public class HighlightsPresenter extends BasePresenter<HighlightsView> {

    private static final String TAG = "HighlightsPresenter";

    private HighlightsService highlightsService;
    private BaseSchedulerProvider schedulerProvider;
    private CompositeDisposable disposables;

    public HighlightsPresenter(HighlightsService highlightsService, BaseSchedulerProvider schedulerProvider) {
        this.highlightsService = highlightsService;
        this.schedulerProvider = schedulerProvider;

        disposables = new CompositeDisposable();
    }

    public void loadHighlights() {
        String orderBy = "\"$key\"";
        String startAt = "";
        String limitToLast = String.valueOf(5);

        view.setLoadingIndicator(true);
        disposables.add(highlightsService.getHighlights(orderBy, startAt, limitToLast)
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .doOnError(new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.e(TAG, throwable.toString());
                    }
                })
                .subscribeWith(new DisposableSingleObserver<Map<String, Highlight>>() {
                    @Override
                    public void onSuccess(Map<String, Highlight> highlights) {
                        if (highlights.isEmpty()) {
                            view.showNoHighlightsAvailable();
                        } else {
                            view.showHighlights(new ArrayList<>(highlights.values()));
                        }
                        view.setLoadingIndicator(false);
                    }

                    @Override
                    public void onError(Throwable e) {
                        view.showErrorLoadingHighlights();
                        view.setLoadingIndicator(false);
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
