package com.gmail.jorgegilcavazos.ballislife.network.API;

import com.gmail.jorgegilcavazos.ballislife.features.model.BoxScoreValues;
import com.gmail.jorgegilcavazos.ballislife.features.model.DayGames;
import com.gmail.jorgegilcavazos.ballislife.features.model.NbaGame;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface NbaGamesService {

    @GET("nba/{date}/.json")
    Single<DayGames> getDayGames(@Path("date") String date);

    @GET("nba/{date}/games.json")
    Observable<List<NbaGame>> listGames(@Path("date") String date);

    @GET("boxscore/{gameId}/g.json")
    Single<BoxScoreValues> boxScore(@Path("gameId") String gameId);

}
