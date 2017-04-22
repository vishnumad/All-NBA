package com.gmail.jorgegilcavazos.ballislife.features.highlights;

import com.gmail.jorgegilcavazos.ballislife.features.model.Highlight;
import com.gmail.jorgegilcavazos.ballislife.network.API.HighlightsService;
import com.gmail.jorgegilcavazos.ballislife.util.schedulers.TrampolineSchedulerProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Single;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(MockitoJUnitRunner.class)
public class HighlightsPresenterTest {

    @Mock
    HighlightsView mockView;

    @Mock
    HighlightsService mockHighlightsService;

    HighlightsPresenter presenter;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        presenter = new HighlightsPresenter(mockHighlightsService,
                new TrampolineSchedulerProvider());
        presenter.attachView(mockView);
    }

    @Test
    public void testLoadHighlights_hlsAvailable_shouldShowHighlights() {
        List<Highlight> highlights = new ArrayList<>();
        highlights.add(new Highlight("1", "Title 1", "url1"));
        highlights.add(new Highlight("2", "Title 2", "url2"));
        highlights.add(new Highlight("3", "Title 3", "url3"));

        Mockito.when(mockHighlightsService.getHighlights(anyString(), anyString(), anyString()))
                .thenReturn(Single.just(highlights));

        presenter.loadHighlights();

        verify(mockView).setLoadingIndicator(true);
        verify(mockView).showHighlights(highlights);
        verify(mockView).setLoadingIndicator(false);
        verify(mockHighlightsService).getHighlights(anyString(), anyString(), anyString());
        verifyNoMoreInteractions(mockView);
        verifyNoMoreInteractions(mockHighlightsService);

    }

    @Test
    public void testLoadHighlights_hlsEmpty_shouldShowNoHighlightsMessage() {
        List<Highlight> highlights = new ArrayList<>();

        Mockito.when(mockHighlightsService.getHighlights(anyString(), anyString(), anyString()))
                .thenReturn(Single.just(highlights));

        presenter.loadHighlights();

        verify(mockView).setLoadingIndicator(true);
        verify(mockView).showNoHighlightsAvailable();
        verify(mockView).setLoadingIndicator(false);
        verify(mockHighlightsService).getHighlights(anyString(), anyString(), anyString());
        verifyNoMoreInteractions(mockView);
        verifyNoMoreInteractions(mockHighlightsService);

    }

    @Test
    public void testLoadHighlights_errorLoading_shouldShowErrorMessage() {
        Single<List<Highlight>> errorSingle = Single.error(new Exception());
        Mockito.when(mockHighlightsService.getHighlights(anyString(), anyString(), anyString()))
                .thenReturn(errorSingle);

        presenter.loadHighlights();

        verify(mockView).setLoadingIndicator(true);
        verify(mockView).showErrorLoadingHighlights();
        verify(mockView).setLoadingIndicator(false);
        verify(mockHighlightsService).getHighlights(anyString(), anyString(), anyString());
        verifyNoMoreInteractions(mockView);
        verifyNoMoreInteractions(mockHighlightsService);

    }

}
