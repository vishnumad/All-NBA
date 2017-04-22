package com.gmail.jorgegilcavazos.ballislife.features.highlights;

import com.gmail.jorgegilcavazos.ballislife.features.model.Highlight;

import java.util.List;

public interface HighlightsView {

    void setLoadingIndicator(boolean active);

    void showHighlights(List<Highlight> highlights);

    void showNoHighlightsAvailable();

    void showErrorLoadingHighlights();
}
