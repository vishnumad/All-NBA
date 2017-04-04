package com.gmail.jorgegilcavazos.ballislife.network.API;

import com.gmail.jorgegilcavazos.ballislife.features.model.GameThreadSummary;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface RedditGameThreadsService {

    @GET("gamethreads/{date}.json")
    Single<List<GameThreadSummary>> fetchGameThreads(@Path("date") String date);

}
