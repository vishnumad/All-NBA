package com.gmail.jorgegilcavazos.ballislife.data.repository.games;

import com.gmail.jorgegilcavazos.ballislife.data.service.NbaGamesService;
import com.gmail.jorgegilcavazos.ballislife.features.application.BallIsLifeApplication;
import com.gmail.jorgegilcavazos.ballislife.features.model.DayGames;
import com.gmail.jorgegilcavazos.ballislife.features.model.NbaGame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.functions.Function;
import retrofit2.Retrofit;

/**
 * Implemntation of the {@link GamesRepository} interface.
 */
public class GamesRepositoryImpl implements GamesRepository {

    @Inject
    Retrofit retrofit;

    private NbaGamesService nbaGamesService;

    private Map<String, List<NbaGame>> cachedDayGamesMap;

    @Inject
    public GamesRepositoryImpl() {
        BallIsLifeApplication.getAppComponent().inject(this);
        nbaGamesService = retrofit.create(NbaGamesService.class);
        cachedDayGamesMap = new HashMap<>();
    }

    @Override
    public void reset() {
        cachedDayGamesMap.clear();
    }

    @Override
    public Single<List<NbaGame>> getGames(final String date) {
        return nbaGamesService.getDayGames(date)
                .flatMap(new Function<DayGames, SingleSource<? extends List<NbaGame>>>() {
                    @Override
                    public SingleSource<? extends List<NbaGame>> apply(DayGames dayGames) throws
                            Exception {
                        if (dayGames.getNum_games() == 0) {
                            cachedDayGamesMap.remove(date);
                            return Single.just(new ArrayList<NbaGame>());
                        } else {
                            cachedDayGamesMap.put(date, dayGames.getGames());
                            return Single.just(dayGames.getGames());
                        }
                    }
                });
    }

    @Override
    public List<NbaGame> getCachedGames(String date) {
        return cachedDayGamesMap.get(date);
    }
}
