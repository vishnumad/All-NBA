package com.gmail.jorgegilcavazos.ballislife.data.repository.posts;

import com.gmail.jorgegilcavazos.ballislife.features.model.wrapper.CustomSubmission;

import net.dean.jraw.paginators.Sorting;
import net.dean.jraw.paginators.TimePeriod;

import java.util.List;

import io.reactivex.Single;

public interface PostsRepository {
    void reset(Sorting sorting, TimePeriod timePeriod, String subreddit);

    Single<List<CustomSubmission>> next();

    List<CustomSubmission> getCachedSubmissions();

    void clearCache();
}
