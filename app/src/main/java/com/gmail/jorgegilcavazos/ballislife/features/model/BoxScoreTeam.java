package com.gmail.jorgegilcavazos.ballislife.features.model;

import java.util.List;

public class BoxScoreTeam {

    private List<StatLine> pstsg;

    public BoxScoreTeam(List<StatLine> pstsg) {
        this.pstsg = pstsg;
    }

    public List<StatLine> getPstsg() {
        return pstsg;
    }

    public void setPstsg(List<StatLine> pstsg) {
        this.pstsg = pstsg;
    }
}
