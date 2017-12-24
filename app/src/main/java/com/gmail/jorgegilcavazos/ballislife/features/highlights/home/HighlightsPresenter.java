package com.gmail.jorgegilcavazos.ballislife.features.highlights.home;

import android.support.annotation.NonNull;

import com.gmail.jorgegilcavazos.ballislife.base.BasePresenter;
import com.gmail.jorgegilcavazos.ballislife.data.local.LocalRepository;
import com.gmail.jorgegilcavazos.ballislife.data.repository.highlights.FavoritesRepository;
import com.gmail.jorgegilcavazos.ballislife.data.repository.highlights.HighlightsRepository;
import com.gmail.jorgegilcavazos.ballislife.features.model.Highlight;
import com.gmail.jorgegilcavazos.ballislife.features.model.HighlightViewType;
import com.gmail.jorgegilcavazos.ballislife.features.model.SwishCard;
import com.gmail.jorgegilcavazos.ballislife.util.ErrorHandler;
import com.gmail.jorgegilcavazos.ballislife.util.NetworkUtils;
import com.gmail.jorgegilcavazos.ballislife.util.Utilities;
import com.gmail.jorgegilcavazos.ballislife.util.schedulers.BaseSchedulerProvider;

import java.util.List;
import java.util.NoSuchElementException;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.observers.DisposableSingleObserver;

public class HighlightsPresenter extends BasePresenter<HighlightsView> {

    private final HighlightsRepository highlightsRepository;
    private final FavoritesRepository favoritesRepository;
    private final LocalRepository localRepository;
    private final BaseSchedulerProvider schedulerProvider;
    private final CompositeDisposable disposables;
    private final NetworkUtils networkUtils;
    private final ErrorHandler errorHandler;

    @Inject
    public HighlightsPresenter(
            HighlightsRepository highlightsRepository,
            FavoritesRepository favoritesRepository,
            LocalRepository localRepository,
            BaseSchedulerProvider schedulerProvider,
            NetworkUtils networkUtils,
            ErrorHandler errorHandler) {
        this.highlightsRepository = highlightsRepository;
        this.favoritesRepository = favoritesRepository;
        this.localRepository = localRepository;
        this.schedulerProvider = schedulerProvider;
        this.networkUtils = networkUtils;
        this.errorHandler = errorHandler;

        disposables = new CompositeDisposable();
    }

    @Override
    public void attachView(@NonNull HighlightsView view) {
        super.attachView(view);

        disposables.add(view.explorePremiumClicks()
                .filter(swishCard -> swishCard == SwishCard.HIGHLIGHT_SORTING)
                .subscribe(swishCard -> {
                    localRepository.markSwishCardSeen(SwishCard.HIGHLIGHT_SORTING);
                    view.dismissSwishCard(swishCard);
                    view.openPremiumActivity();
                }));

        disposables.add(view.gotItClicks()
                .filter(swishCard -> swishCard == SwishCard.HIGHLIGHT_SORTING)
                .subscribe(swishCard -> {
                    localRepository.markSwishCardSeen(SwishCard.HIGHLIGHT_SORTING);
                    view.dismissSwishCard(swishCard);
                }));
    }

    @Override
    public void detachView() {
        disposables.clear();
        view.hideSnackbar();
        super.detachView();
    }

    public void setItemsToLoad(int itemsToLoad) {
        highlightsRepository.setItemsToLoad(itemsToLoad);
    }

    public void loadFirstAvailable() {
        List<Highlight> highlights = highlightsRepository.getCachedHighlights();
        if (highlights.isEmpty() || highlightsRepository.getSorting() != view.getSorting()) {
            loadHighlights(true);
        } else {
            view.showHighlights(highlights, true);
        }
    }

    public void loadHighlights(final boolean reset) {
        if (reset) {
            view.resetScrollState();
            view.setLoadingIndicator(true);
            view.hideHighlights();
            highlightsRepository.reset(view.getSorting());
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
                        view.hideSnackbar();
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (!networkUtils.isNetworkAvailable()) {
                            view.showNoNetAvailable();
                        } else if (e instanceof NoSuchElementException) {
                            view.showNoHighlightsAvailable();
                        } else {
                            view.showErrorLoadingHighlights(errorHandler.handleError(e));
                        }

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
                        if (hl.getUrl().contains("streamable")) {
                            String shortCode = Utilities.getStreamableShortcodeFromUrl(hl.getUrl());
                            if (shortCode != null) {
                                view.openStreamable(shortCode);
                            } else {
                                view.showErrorOpeningStreamable();
                            }
                        } else if (hl.getUrl().contains("youtube") || hl.getUrl().contains("youtu" +
                                ".be")) {
                            String videoId = Utilities.getYoutubeVideoIdFromUrl(hl.getUrl());
                            if (videoId == null) {
                                view.showErrorOpeningYoutube();
                            } else {
                                view.openYoutubeVideo(videoId);
                            }
                        } else {
                            view.showUnknownSourceError();
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

                    public void onComplete() {

                    }
                })
        );
    }

    public void subscribeToSubmissionClick(Observable<Highlight> highlightShare) {
        disposables.add(highlightShare
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribeWith(new DisposableObserver<Highlight>() {
                    @Override
                    public void onNext(Highlight highlight) {
                        view.onSubmissionClick(highlight);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    public void onComplete() {

                    }
                })
        );
    }

    public void subscribeToFavoriteClick(Observable<Highlight> highlights) {
        disposables.add(highlights
                .subscribe(highlight -> {
                    view.showAddingToFavoritesMsg();
                    addToFavorites(highlight);
                }));
    }

    public void onViewTypeSelected(HighlightViewType viewType) {
        localRepository.saveFavoriteHighlightViewType(viewType);
        view.changeViewType(viewType);
    }

    private void addToFavorites(Highlight highlight) {
        disposables.add(favoritesRepository
                .saveToFavorites(highlight)
                .subscribeWith(new DisposableCompletableObserver() {
                    @Override
                    public void onComplete() {
                        view.showAddedToFavoritesMsg();
                    }

                    @Override
                    public void onError(Throwable e) {
                        view.showAddToFavoritesFailed();
                    }
                })
        );
    }

}
