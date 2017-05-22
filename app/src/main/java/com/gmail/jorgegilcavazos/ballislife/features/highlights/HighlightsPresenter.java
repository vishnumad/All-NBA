package com.gmail.jorgegilcavazos.ballislife.features.highlights;

import com.gmail.jorgegilcavazos.ballislife.base.BasePresenter;
import com.gmail.jorgegilcavazos.ballislife.data.HighlightsRepository;
import com.gmail.jorgegilcavazos.ballislife.features.model.Highlight;
import com.gmail.jorgegilcavazos.ballislife.util.Utilities;
import com.gmail.jorgegilcavazos.ballislife.util.schedulers.BaseSchedulerProvider;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.observers.DisposableSingleObserver;

public class HighlightsPresenter extends BasePresenter<HighlightsView> {

    private static final String TAG = "HighlightsPresenter";

    private HighlightsRepository highlightsRepository;
    private BaseSchedulerProvider schedulerProvider;
    private CompositeDisposable disposables;

    public HighlightsPresenter(HighlightsRepository highlightsRepository,
                               BaseSchedulerProvider schedulerProvider) {
        this.highlightsRepository = highlightsRepository;
        this.schedulerProvider = schedulerProvider;

        disposables = new CompositeDisposable();
    }

    public void loadHighlights(final boolean reset) {
        if (reset) {
            view.resetScrollState();
            view.setLoadingIndicator(true);
            highlightsRepository.reset();
        }
        disposables.add(highlightsRepository.next()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribeWith(new DisposableSingleObserver<List<Highlight>>() {
                    @Override
                    public void onSuccess(List<Highlight> highlights) {
                        if (highlights.isEmpty()) {
                            view.showNoHighlightsAvailable();
                        } else {
                            view.showHighlights(highlights, reset);
                        }

                        if (reset) {
                            view.setLoadingIndicator(false);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        view.showErrorLoadingHighlights();

                        if (reset) {
                            view.setLoadingIndicator(false);
                        }
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

    public void subscribeToHighlightsShare(Observable<Highlight> highlightShare) {
        disposables.add(highlightShare
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribeWith(new DisposableObserver<Highlight>() {
                    @Override
                    public void onNext(Highlight highlight) {
                        view.shareHighlight(highlight);
                    }

                    @Override
                    public void onError(Throwable e) {

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
