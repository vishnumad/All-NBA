package com.gmail.jorgegilcavazos.ballislife.data.service;

import com.gmail.jorgegilcavazos.ballislife.features.model.GameThreadSummary;
import com.gmail.jorgegilcavazos.ballislife.util.RedditUtils;

import java.util.List;

import io.reactivex.Single;

public class GameThreadFinderService {

    public static Single<String> findGameThreadInList(final List<GameThreadSummary> threads,
                                                      final String type,
                                                      final String homeTeamAbbr,
                                                      final String awayTeamAbbr) {
        return Single.create(e -> e.onSuccess(RedditUtils.findGameThreadId(threads, type,
                homeTeamAbbr, awayTeamAbbr)));
    }

}
