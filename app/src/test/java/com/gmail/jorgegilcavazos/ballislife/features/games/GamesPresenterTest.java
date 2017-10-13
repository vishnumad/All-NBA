package com.gmail.jorgegilcavazos.ballislife.features.games;

import com.gmail.jorgegilcavazos.ballislife.data.repository.games.GamesRepository;
import com.gmail.jorgegilcavazos.ballislife.features.model.GameV2;
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

import static org.mockito.ArgumentMatchers.any;
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

    private static GameV2 createGameV2() {
        return new GameV2("AT&T", "SAS", "San Antonio", "1545334321", "SAS", "Spurs", "110", "San" +
                " Antonio", "20171110", "", "", "", "", "", "", "", "", "", "", "", "", "",
                23894341, "");
    }

    @Before
    public void setup() {
        presenter = new GamesPresenter(mockRepository, new TrampolineSchedulerProvider());
        presenter.attachView(mockView);
    }

    @Test
    public void testLoadFirstAvailable_cacheEmptyWithGamesFromNetwork() {
        GameV2 game0 = createGameV2();
        GameV2 game1 = createGameV2();
        GameV2 game2 = createGameV2();
        when(mockRepository.getGames(any()))
                .thenReturn(Single.just(Arrays.asList(game0, game1, game2)));

        presenter.loadFirstAvailable(Calendar.getInstance());

        verify(mockRepository).getGames(any());
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
        when(mockRepository.getGames(any())).thenReturn(Single.just(Collections.emptyList()));

        presenter.loadFirstAvailable(Calendar.getInstance());

        verify(mockRepository).getGames(any());
        verify(mockView).setNoGamesIndicator(true);
        verify(mockView).setLoadingIndicator(false);
        verifyNoMoreInteractions(mockRepository);
    }

    @Test
    public void testLoadFirstAvailable_cacheEmptyWithNetworkUnavailable() {
        Single errorSingle = Single.error(new Exception());
        when(mockRepository.getGames(any())).thenReturn(errorSingle);

        presenter.loadFirstAvailable(Calendar.getInstance());

        verify(mockRepository).getGames(any());
        verify(mockView).hideGames();
        verify(mockView).setLoadingIndicator(true);
        verify(mockView).setLoadingIndicator(false);
        verify(mockView).showSnackbar(true);
        verifyNoMoreInteractions(mockRepository);
    }

    @Test
    public void onDestroy_hideSnackbar() {
        presenter.stop();

        verify(mockView).dismissSnackbar();
    }
}
