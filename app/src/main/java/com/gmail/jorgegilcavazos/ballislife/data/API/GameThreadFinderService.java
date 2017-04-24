package com.gmail.jorgegilcavazos.ballislife.data.API;

import com.gmail.jorgegilcavazos.ballislife.features.model.GameThreadSummary;
import com.gmail.jorgegilcavazos.ballislife.util.RedditUtils;

import java.util.List;

import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;

public class GameThreadFinderService {

    public static Single<String> findGameThreadInList(final List<GameThreadSummary> threads,
                                                      final String type,
                                                      final String homeTeamAbbr,
                                                      final String awayTeamAbbr) {
        return Single.create(new SingleOnSubscribe<String>() {
            @Override
            public void subscribe(SingleEmitter<String> e) throws Exception {
                e.onSuccess(RedditUtils.findGameThreadId(threads, type,
                        homeTeamAbbr, awayTeamAbbr));
            }
        });
    }

}
