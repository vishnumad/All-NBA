package com.gmail.jorgegilcavazos.ballislife.data.repository.games;

import com.gmail.jorgegilcavazos.ballislife.data.service.NbaGamesService;
import com.gmail.jorgegilcavazos.ballislife.features.model.GameV2;
import com.gmail.jorgegilcavazos.ballislife.util.DateFormatUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Single;

/**
 * Implementation of the {@link GamesRepository} interface.
 */
@Singleton
public class GamesRepositoryImpl implements GamesRepository {

    private final NbaGamesService gamesService;

    @Inject
    public GamesRepositoryImpl(NbaGamesService gamesService) {
        this.gamesService = gamesService;
    }

    @Override
    public Single<List<GameV2>> getGames(Calendar date) {
        return gamesService.getDayGames("\"timeUtc\"", DateFormatUtil.getDateStartUtc(date),
                DateFormatUtil.getDateEndUtc(date)).map(idGameV2Map -> new ArrayList<>
                (idGameV2Map.values())).map(gameV2s -> {
            Collections.sort(gameV2s, (g1, g2) -> g1.getTimeUtc() < g2.getTimeUtc() ? -1 : 1);
            return gameV2s;
                });
    }
}
