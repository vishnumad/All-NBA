package com.gmail.jorgegilcavazos.ballislife.data.repository.posts;

import com.gmail.jorgegilcavazos.ballislife.features.model.SubmissionWrapper;

import net.dean.jraw.paginators.Sorting;
import net.dean.jraw.paginators.TimePeriod;

import java.util.List;

import io.reactivex.Single;

public interface PostsRepository {
    void reset(Sorting sorting, TimePeriod timePeriod, String subreddit);

    void reset(Sorting sorting, TimePeriod timePeriod, String multiOwner, String multiName);

    Single<List<SubmissionWrapper>> next();

    List<SubmissionWrapper> getCachedSubmissions();

    void clearCache();

    Sorting getCurrentSorting();
}
