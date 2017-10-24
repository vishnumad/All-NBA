package com.gmail.jorgegilcavazos.ballislife.features.highlights;

import com.gmail.jorgegilcavazos.ballislife.data.local.LocalRepository;
import com.gmail.jorgegilcavazos.ballislife.data.repository.highlights.HighlightsRepositoryImpl;
import com.gmail.jorgegilcavazos.ballislife.features.model.Highlight;
import com.gmail.jorgegilcavazos.ballislife.util.ErrorHandler;
import com.gmail.jorgegilcavazos.ballislife.util.NetworkUtils;
import com.gmail.jorgegilcavazos.ballislife.util.schedulers.TrampolineSchedulerProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HighlightsPresenterTest {

    @Mock
    HighlightsView mockView;
    @Mock
    HighlightsRepositoryImpl mockHighlightsRepository;
    @Mock
    LocalRepository mockLocalRepository;
    @Mock NetworkUtils mockNetworkUtils;
    @Mock ErrorHandler mockErrorHandler;

    HighlightsPresenter presenter;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        presenter = new HighlightsPresenter(mockHighlightsRepository, mockLocalRepository,
                                            new TrampolineSchedulerProvider(),
                                            mockNetworkUtils,
                                            mockErrorHandler);
        presenter.attachView(mockView);
    }

    @Test
    public void testLoadHighlights_hlsAvailable_shouldShowHighlights() {
        List<Highlight> highlightList = new ArrayList<>();
        highlightList.add(new Highlight("1", "Title 1", "url1", "url1", "", 0));
        highlightList.add(new Highlight("2", "Title 2", "url2", "url2", "", 0));
        highlightList.add(new Highlight("3", "Title 3", "url3", "url3", "", 0));
        Mockito.when(mockHighlightsRepository.next()).thenReturn(Single.just(highlightList));

        presenter.loadHighlights(true); // reset to get first batch.

        verify(mockView).setLoadingIndicator(true);
        verify(mockView).resetScrollState();
        verify(mockHighlightsRepository).reset();
        verify(mockHighlightsRepository).next();
        verify(mockView).showHighlights(highlightList, true);
        verify(mockView).setLoadingIndicator(false);
        verify(mockView).hideSnackbar();
        verifyNoMoreInteractions(mockView);
        verifyNoMoreInteractions(mockHighlightsRepository);
    }


    @Test
    public void testLoadHighlights_hlsEmpty_shouldShowNoHighlightsMessage() {
        List<Highlight> highlightList = new ArrayList<>();
        Mockito.when(mockHighlightsRepository.next()).thenReturn(Single.just(highlightList));

        presenter.loadHighlights(true); // reset to get first batch.

        verify(mockView).setLoadingIndicator(true);
        verify(mockView).resetScrollState();
        verify(mockHighlightsRepository).reset();
        verify(mockHighlightsRepository).next();
        verify(mockView).showNoHighlightsAvailable();
        verify(mockView).setLoadingIndicator(false);
        verify(mockView).hideSnackbar();
        verifyNoMoreInteractions(mockView);
        verifyNoMoreInteractions(mockHighlightsRepository);
    }

    @Test
    public void testLoadHighlights_errorLoadingWithNetAvailable_shouldShowErrorMessage() {
        Single<List<Highlight>> errorSingle = Single.error(new Exception());
        when(mockHighlightsRepository.next()).thenReturn(errorSingle);
        when(mockNetworkUtils.isNetworkAvailable()).thenReturn(true);
        when(mockErrorHandler.handleError(any())).thenReturn(404);

        presenter.loadHighlights(true); // reset to get first batch.

        verify(mockView).setLoadingIndicator(true);
        verify(mockView).resetScrollState();
        verify(mockHighlightsRepository).reset();
        verify(mockHighlightsRepository).next();
        verify(mockView).showErrorLoadingHighlights(404);
        verify(mockView).setLoadingIndicator(false);
        verifyNoMoreInteractions(mockView);
        verifyNoMoreInteractions(mockHighlightsRepository);
    }

    @Test
    public void testLoadHighlights_errorLoadingWithNoNetAvailable_shouldShowErrorMessage() {
        Single<List<Highlight>> errorSingle = Single.error(new Exception());
        when(mockHighlightsRepository.next()).thenReturn(errorSingle);
        when(mockNetworkUtils.isNetworkAvailable()).thenReturn(false);

        presenter.loadHighlights(true); // reset to get first batch.

        verify(mockView).setLoadingIndicator(true);
        verify(mockView).resetScrollState();
        verify(mockHighlightsRepository).reset();
        verify(mockHighlightsRepository).next();
        verify(mockView).showNoNetAvailable();
        verify(mockView).setLoadingIndicator(false);
        verifyNoMoreInteractions(mockView);
        verifyNoMoreInteractions(mockHighlightsRepository);
    }

    @Test
    public void testLoadHighlights_loadSecondPage_showShowHighlights() {
        List<Highlight> highlightList = new ArrayList<>();
        highlightList.add(new Highlight("1", "Title 1", "url1", "url1", "", 0));
        highlightList.add(new Highlight("2", "Title 2", "url2", "url2", "", 0));
        highlightList.add(new Highlight("3", "Title 3", "url3", "url3", "", 0));
        Mockito.when(mockHighlightsRepository.next()).thenReturn(Single.just(highlightList));

        presenter.loadHighlights(false);

        verify(mockHighlightsRepository).next();
        verify(mockView).showHighlights(highlightList, false);
        verify(mockView).hideSnackbar();
        verifyNoMoreInteractions(mockHighlightsRepository);
        verifyNoMoreInteractions(mockView);
    }

    @Test
    public void testSubscribeToHighlightsClick() {
        Highlight hl1 = new Highlight("1", "Title 1", "", "", "streamable.com/abcde", 0);
        Highlight hl2 = new Highlight("2", "Title 2", "", "", "twitter.com/rkeyr", 0);
        Highlight hl3 = new Highlight("3", "Title 3", "", "", "streamable.com/fghi", 0);
        Highlight hl4 = new Highlight("3", "Title 3", "", "", "youtube.com?v=poiuy", 0);
        Highlight hl5 = new Highlight("3", "Title 3", "", "", "youtu.be/xyz", 0);

        Observable<Highlight> highlightObservable = Observable.just(hl1, hl2, hl3, hl4, hl5);

        presenter.subscribeToHighlightsClick(highlightObservable);

        verify(mockView).openStreamable("abcde");
        verify(mockView).showUnknownSourceError();
        verify(mockView).openStreamable("fghi");
        verify(mockView).openYoutubeVideo("poiuy");
        verify(mockView).openYoutubeVideo("xyz");
        verifyNoMoreInteractions(mockView);
    }

    @Test
    public void onDestroyHideSnackbar() {
        presenter.stop();

        verify(mockView).hideSnackbar();
    }
}
