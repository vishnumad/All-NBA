package com.gmail.jorgegilcavazos.ballislife.data.repository.highlights;

import com.gmail.jorgegilcavazos.ballislife.data.service.HighlightsService;
import com.gmail.jorgegilcavazos.ballislife.features.application.BallIsLifeApplication;
import com.gmail.jorgegilcavazos.ballislife.features.model.Highlight;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.functions.Function;
import retrofit2.Retrofit;

public class HighlightsRepositoryImpl implements HighlightsRepository {

    public static final String ORDER_KEY = "\"$key\"";
    private static final String START_AT_ALL = "";

    @Inject
    Retrofit retrofit;

    private HighlightsService highlightsService;
    private String lastHighlighKey = "";
    private boolean firstLoad = true;

    private int itemsToLoad;

    private List<Highlight> cachedHighlights;

    public HighlightsRepositoryImpl(int itemsToLoad) {
        BallIsLifeApplication.getAppComponent().inject(this);
        this.itemsToLoad = itemsToLoad;
        highlightsService = retrofit.create(HighlightsService.class);
        cachedHighlights = new ArrayList<>();
    }

    @Override
    public void reset() {
        firstLoad = true;
        lastHighlighKey = "";
        cachedHighlights.clear();
    }

    @Override
    public Single<List<Highlight>> next() {
        String startAt, endAt, itemsToLoad;
        if (firstLoad) {
            startAt = START_AT_ALL;
            endAt = null;
            itemsToLoad = String.valueOf(this.itemsToLoad);
        } else {
            startAt = null;
            endAt = "\"" + lastHighlighKey + "\"";
            itemsToLoad = String.valueOf(this.itemsToLoad + 1);
        }

        return highlightsService.getHighlights(ORDER_KEY, startAt, endAt, itemsToLoad)
                    .flatMap(new Function<Map<String, Highlight>, SingleSource<? extends List<Highlight>>>() {
                        @Override
                        public SingleSource<? extends List<Highlight>> apply(Map<String, Highlight> stringHighlightMap) throws Exception {
                            List<Highlight> highlightList = new ArrayList<>();
                            if (stringHighlightMap.isEmpty()) {
                                cachedHighlights.addAll(highlightList);
                                return Single.just(highlightList);
                            } else {
                                // Save the key of the 1st element (oldest on the map) for pagination.
                                for (Map.Entry<String, Highlight> entry : stringHighlightMap.entrySet()) {
                                    lastHighlighKey = entry.getKey();
                                    break;
                                }

                                highlightList.addAll(stringHighlightMap.values());

                                if (!firstLoad) {
                                    // Last element was already emitted in the previous loadHighlights.
                                    highlightList.remove(highlightList.size() - 1);
                                }

                                firstLoad = false;

                                // Reverse items so that they're sorted from most recent to oldest.
                                Collections.reverse(highlightList);
                                cachedHighlights.addAll(highlightList);
                                return Single.just(highlightList);
                            }
                        }
                    });
    }

    @Override
    public List<Highlight> getCachedHighlights() {
        return cachedHighlights;
    }


}
