package com.gmail.jorgegilcavazos.ballislife.data.service;

import com.gmail.jorgegilcavazos.ballislife.features.model.GameThreadSummary;

import java.util.Map;

import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface RedditGameThreadsService {

    @GET("gamethreads/{date}.json")
    Single<Map<String, GameThreadSummary>> fetchGameThreads(@Path("date") String date);

}
