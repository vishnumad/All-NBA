package com.gmail.jorgegilcavazos.ballislife.network.API;

import com.gmail.jorgegilcavazos.ballislife.features.model.Standings;

import io.reactivex.Observable;
import io.reactivex.Single;
import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface NbaStandingsService {

    @GET("standings/{SeasonId}.json")
    Single<Standings> getStandings(@Path("SeasonId") String seasonId);

}
