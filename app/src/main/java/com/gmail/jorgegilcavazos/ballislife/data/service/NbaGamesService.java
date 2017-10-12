package com.gmail.jorgegilcavazos.ballislife.data.service;

import com.gmail.jorgegilcavazos.ballislife.features.model.BoxScoreValues;
import com.gmail.jorgegilcavazos.ballislife.features.model.GameV2;
import com.gmail.jorgegilcavazos.ballislife.features.model.NbaGame;

import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface NbaGamesService {

    @GET("games/2017-18/.json")
    Single<Map<String, GameV2>> getDayGames(
            @Query("orderBy") String orderBy,
            @Query("startAt") long startAt, @Query("endAt") long endAt);

    @GET("nba/{date}/games.json")
    Observable<List<NbaGame>> listGames(@Path("date") String date);

    @GET("boxscore/{gameId}/g.json")
    Single<BoxScoreValues> boxScore(@Path("gameId") String gameId);

}
