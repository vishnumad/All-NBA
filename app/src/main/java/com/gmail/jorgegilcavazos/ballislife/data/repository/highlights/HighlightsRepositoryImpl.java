package com.gmail.jorgegilcavazos.ballislife.data.repository.highlights;

import com.gmail.jorgegilcavazos.ballislife.data.service.HighlightsService;
import com.gmail.jorgegilcavazos.ballislife.features.model.Highlight;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Single;

@Singleton
public class HighlightsRepositoryImpl implements HighlightsRepository {

    private static final String START_AT_ALL = "";
    public static final String ORDER_KEY = "\"$key\"";
    private final HighlightsService highlightsService;

    private String lastHighlightKey = "";
    private boolean firstLoad = true;

    private int itemsToLoad;

    private List<Highlight> cachedHighlights;

    @Inject
    public HighlightsRepositoryImpl(HighlightsService highlightsService) {
        this.highlightsService = highlightsService;
        cachedHighlights = new ArrayList<>();
    }

    @Override
    public void setItemsToLoad(int itemsToLoad) {
        this.itemsToLoad = itemsToLoad;
    }

    @Override
    public void reset() {
        firstLoad = true;
        lastHighlightKey = "";
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
            endAt = "\"" + lastHighlightKey + "\"";
            itemsToLoad = String.valueOf(this.itemsToLoad + 1);
        }

        return highlightsService.getHighlights(ORDER_KEY, startAt, endAt, itemsToLoad).flatMap
                (stringHighlightMap -> {
            List<Highlight> highlightList = new ArrayList<>();
            if (stringHighlightMap.isEmpty()) {
                cachedHighlights.addAll(highlightList);
                return Single.just(highlightList);
            } else {
                // Save the key of the 1st element (oldest on the map) for pagination.
                for (Map.Entry<String, Highlight> entry : stringHighlightMap.entrySet()) {
                    lastHighlightKey = entry.getKey();
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
                    });
    }

    @Override
    public List<Highlight> getCachedHighlights() {
        return cachedHighlights;
    }


}
