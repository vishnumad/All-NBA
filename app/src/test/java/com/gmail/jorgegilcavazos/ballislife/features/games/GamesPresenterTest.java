package com.gmail.jorgegilcavazos.ballislife.features.games;

import com.gmail.jorgegilcavazos.ballislife.data.repository.games.GamesRepository;
import com.gmail.jorgegilcavazos.ballislife.features.model.NbaGame;
import com.gmail.jorgegilcavazos.ballislife.util.DateFormatUtil;
import com.gmail.jorgegilcavazos.ballislife.util.schedulers.TrampolineSchedulerProvider;
import com.google.common.collect.ImmutableList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;

import io.reactivex.Single;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GamesPresenterTest {

    @Mock
    GamesView mockView;

    @Mock
    GamesRepository mockRepository;

    GamesPresenter presenter;

    @Before
    public void setup() {
        presenter = new GamesPresenter(mockRepository, new TrampolineSchedulerProvider());
        presenter.attachView(mockView);
    }

    @Test
    public void testLoadFirstAvailable_cacheAvailable() {
        NbaGame game0 = new NbaGame();
        NbaGame game1 = new NbaGame();
        NbaGame game2 = new NbaGame();
        Calendar calendar = Calendar.getInstance();
        String stringDate = DateFormatUtil.getNoDashDateString(calendar.getTime());
        when(mockRepository.getCachedGames(stringDate)).thenReturn(ImmutableList.of(game0, game1,
                game2));

        presenter.loadFirstAvailable(calendar);

        verify(mockRepository).getCachedGames(stringDate);
        verify(mockView).setDateNavigatorText(anyString());
        verify(mockView).setNoGamesIndicator(false);
        verify(mockView).showGames(ImmutableList.of(game0, game1, game2));
        verify(mockView).dismissSnackbar();
        verifyNoMoreInteractions(mockRepository);
        verifyNoMoreInteractions(mockView);
    }

    @Test
    public void testLoadFirstAvailable_cacheEmptyWithGamesFromNetwork() {
        NbaGame game0 = new NbaGame();
        NbaGame game1 = new NbaGame();
        NbaGame game2 = new NbaGame();
        when(mockRepository.getCachedGames(anyString())).thenReturn(null);
        when(mockRepository.getGames(anyString()))
                .thenReturn(Single.just(Arrays.asList(game0, game1, game2)));

        presenter.loadFirstAvailable(Calendar.getInstance());

        verify(mockRepository).getCachedGames(anyString());
        verify(mockRepository).getGames(anyString());
        verify(mockView).showGames(ImmutableList.of(game0, game1, game2));
        verify(mockView).dismissSnackbar();
        verify(mockView).setLoadingIndicator(true);
        verify(mockView).setNoGamesIndicator(false);
        verify(mockView).hideGames();
        verify(mockView).setDateNavigatorText(anyString());
        verify(mockView).setLoadingIndicator(false);
        verifyNoMoreInteractions(mockRepository);
        verifyNoMoreInteractions(mockView);
    }

    @Test
    public void testLoadFirstAvailable_cacheEmptyWithNoGamesFromNetwork() {
        when(mockRepository.getCachedGames(anyString())).thenReturn(null);
        when(mockRepository.getGames(anyString()))
                .thenReturn(Single.just(Collections.<NbaGame>emptyList()));

        presenter.loadFirstAvailable(Calendar.getInstance());

        verify(mockRepository).getCachedGames(anyString());
        verify(mockRepository).getGames(anyString());
        verify(mockView).setNoGamesIndicator(true);
        verify(mockView).setLoadingIndicator(false);
        verifyNoMoreInteractions(mockRepository);
    }

    @Test
    public void testLoadFirstAvailable_cacheEmptyWithNetworkUnavailable() {
        Single errorSingle = Single.error(new Exception());
        when(mockRepository.getCachedGames(anyString())).thenReturn(null);
        when(mockRepository.getGames(anyString())).thenReturn(errorSingle);

        presenter.loadFirstAvailable(Calendar.getInstance());

        verify(mockRepository).getCachedGames(anyString());
        verify(mockRepository).getGames(anyString());
        verify(mockView).hideGames();
        verify(mockView).setLoadingIndicator(true);
        verify(mockView).setLoadingIndicator(false);
        verify(mockView).showSnackbar(true);
        verifyNoMoreInteractions(mockRepository);
    }
}
