package com.gmail.jorgegilcavazos.ballislife.data.service;

import com.gmail.jorgegilcavazos.ballislife.features.model.Streamable;

import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface StreamableService {

    @GET("videos/{shortcode}")
    Single<Streamable> getStreamable(@Path("shortcode") String shortcode);
}
