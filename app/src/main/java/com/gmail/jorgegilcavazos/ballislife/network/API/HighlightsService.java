package com.gmail.jorgegilcavazos.ballislife.network.API;

import com.gmail.jorgegilcavazos.ballislife.features.model.Highlight;

import java.util.Map;

import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface HighlightsService {

    @GET("streamables.json")
    Single<Map<String, Highlight>> getHighlights(@Query("orderBy") String orderBy,
                                                 @Query("startAt") String startAt,
                                                 @Query("endAt") String endAt,
                                                 @Query("limitToLast") String limitToLast);
}
