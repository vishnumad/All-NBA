package com.gmail.jorgegilcavazos.ballislife.features.highlights;

import android.util.Log;

import com.gmail.jorgegilcavazos.ballislife.base.BasePresenter;
import com.gmail.jorgegilcavazos.ballislife.features.model.Highlight;
import com.gmail.jorgegilcavazos.ballislife.network.API.HighlightsService;
import com.gmail.jorgegilcavazos.ballislife.util.Utilities;
import com.gmail.jorgegilcavazos.ballislife.util.schedulers.BaseSchedulerProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.observers.DisposableSingleObserver;

public class HighlightsPresenter extends BasePresenter<HighlightsView> {

    private static final String TAG = "HighlightsPresenter";

    private HighlightsService highlightsService;
    private BaseSchedulerProvider schedulerProvider;
    private CompositeDisposable disposables;
    private String lastLoadedHLKey;

    public HighlightsPresenter(HighlightsService highlightsService,
                               BaseSchedulerProvider schedulerProvider) {
        this.highlightsService = highlightsService;
        this.schedulerProvider = schedulerProvider;

        disposables = new CompositeDisposable();
    }

    public void loadHighlights() {
        lastLoadedHLKey = "";
        view.resetScrollState();

        // This params query the 20 most recently posted streamables.
        String orderBy = "\"$key\"";
        String startAt = "";
        String endAt = null;
        String limitToLast = String.valueOf(20);

        view.setLoadingIndicator(true);
        disposables.add(highlightsService.getHighlights(orderBy, startAt, endAt, limitToLast)
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .doOnError(new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.e(TAG, throwable.toString());
                        Log.e(TAG, throwable.getLocalizedMessage());
                    }
                })
                .subscribeWith(new DisposableSingleObserver<Map<String, Highlight>>() {
                    @Override
                    public void onSuccess(Map<String, Highlight> highlights) {
                        if (highlights.isEmpty()) {
                            view.showNoHighlightsAvailable();
                        } else {
                            // Save last key for pagination.
                            for (Map.Entry<String, Highlight> entry : highlights.entrySet()) {
                                lastLoadedHLKey = entry.getKey();
                                break;
                            }
                            List<Highlight> highlightList = new ArrayList<>(highlights.values());
                            Collections.reverse(highlightList);
                            view.showHighlights(highlightList, true);
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

    public void loadMoreHighlights() {
        // This params query the 26 most recently posted streamables, ending on the last saved key.
        String orderBy = "\"$key\"";
        String startAt = null;
        String endAt = "\"" + lastLoadedHLKey + "\"";
        String limitToLast = String.valueOf(21);

        disposables.add(highlightsService.getHighlights(orderBy, startAt, endAt, limitToLast)
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .doOnError(new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.e(TAG, throwable.toString());
                        Log.e(TAG, throwable.getLocalizedMessage());
                    }
                })
                .subscribeWith(new DisposableSingleObserver<Map<String, Highlight>>() {
                    @Override
                    public void onSuccess(Map<String, Highlight> highlights) {
                        if (highlights.isEmpty()) {
                            view.showNoHighlightsAvailable();
                        } else {
                            // Save last key for pagination.
                            for (Map.Entry<String, Highlight> entry : highlights.entrySet()) {
                                lastLoadedHLKey = entry.getKey();
                                break;
                            }
                            List<Highlight> highlightList = new ArrayList<>(highlights.values());

                            // Last one from this list is already shown in the recycler view.
                            highlightList.remove(highlightList.size() - 1);

                            Collections.reverse(highlightList);
                            view.showHighlights(highlightList, false);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        view.showErrorLoadingHighlights();
                    }
                })
        );
    }

    public void subscribeToHighlightsClick(Observable<Highlight> highlightsClick) {
        disposables.add(highlightsClick
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribeWith(new DisposableObserver<Highlight>() {
                    @Override
                    public void onNext(Highlight hl) {
                        String shortCode = Utilities.getStreamableShortcodeFromUrl(hl.getUrl());
                        if (shortCode != null) {
                            view.openStreamable(shortCode);
                        } else {
                            view.showErrorOpeningStreamable();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        view.showErrorOpeningStreamable();
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
