package com.gmail.jorgegilcavazos.ballislife.features.highlights;

import com.gmail.jorgegilcavazos.ballislife.data.HighlightsRepositoryImpl;
import com.gmail.jorgegilcavazos.ballislife.features.model.Highlight;
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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(MockitoJUnitRunner.class)
public class HighlightsPresenterTest {

    @Mock
    HighlightsView mockView;

    @Mock
    HighlightsRepositoryImpl mockHighlightsRepository;

    HighlightsPresenter presenter;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        presenter = new HighlightsPresenter(mockHighlightsRepository,
                new TrampolineSchedulerProvider());
        presenter.attachView(mockView);
    }

    @Test
    public void testLoadHighlights_hlsAvailable_shouldShowHighlights() {
        List<Highlight> highlightList = new ArrayList<>();
        highlightList.add(new Highlight("1", "Title 1", "url1", "url1"));
        highlightList.add(new Highlight("2", "Title 2", "url2", "url2"));
        highlightList.add(new Highlight("3", "Title 3", "url3", "url3"));
        Mockito.when(mockHighlightsRepository.next()).thenReturn(Single.just(highlightList));

        presenter.loadHighlights(true); // reset to get first batch.

        verify(mockView).setLoadingIndicator(true);
        verify(mockView).resetScrollState();
        verify(mockHighlightsRepository).reset();
        verify(mockHighlightsRepository).next();
        verify(mockView).showHighlights(highlightList, true);
        verify(mockView).setLoadingIndicator(false);
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
        verifyNoMoreInteractions(mockView);
        verifyNoMoreInteractions(mockHighlightsRepository);
    }

    @Test
    public void testLoadHighlights_errorLoading_shouldShowErrorMessage() {
        Single<List<Highlight>> errorSingle = Single.error(new Exception());
        Mockito.when(mockHighlightsRepository.next()).thenReturn(errorSingle);

        presenter.loadHighlights(true); // reset to get first batch.

        verify(mockView).setLoadingIndicator(true);
        verify(mockView).resetScrollState();
        verify(mockHighlightsRepository).reset();
        verify(mockHighlightsRepository).next();
        verify(mockView).showErrorLoadingHighlights();
        verify(mockView).setLoadingIndicator(false);
        verifyNoMoreInteractions(mockView);
        verifyNoMoreInteractions(mockHighlightsRepository);
    }

    @Test
    public void testLoadHighlights_loadSecondPage_showShowHighlights() {
        List<Highlight> highlightList = new ArrayList<>();
        highlightList.add(new Highlight("1", "Title 1", "url1", "url1"));
        highlightList.add(new Highlight("2", "Title 2", "url2", "url2"));
        highlightList.add(new Highlight("3", "Title 3", "url3", "url3"));
        Mockito.when(mockHighlightsRepository.next()).thenReturn(Single.just(highlightList));

        presenter.loadHighlights(false);

        verify(mockHighlightsRepository).next();
        verify(mockView).showHighlights(highlightList, false);
        verifyNoMoreInteractions(mockHighlightsRepository);
        verifyNoMoreInteractions(mockView);
    }

    @Test
    public void testSubscribeToHighlightsClick() {
        Highlight hl1 = new Highlight("1", "Title 1", "url1", "streamable.com/abcde");
        Highlight hl2 = new Highlight("2", "Title 2", "url2", "twitter.com/rkeyr");
        Highlight hl3 = new Highlight("3", "Title 3", "url3", "streamable.com/fghi");

        Observable<Highlight> highlightObservable = Observable.just(hl1, hl2, hl3);

        presenter.subscribeToHighlightsClick(highlightObservable);

        verify(mockView).openStreamable("abcde");
        verify(mockView).showErrorOpeningStreamable();
        verify(mockView).openStreamable("fghi");
        verifyNoMoreInteractions(mockView);
    }

}
