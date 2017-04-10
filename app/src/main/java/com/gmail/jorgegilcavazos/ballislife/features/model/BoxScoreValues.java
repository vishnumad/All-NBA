package com.gmail.jorgegilcavazos.ballislife.features.model;

public class BoxScoreValues {

    private BoxScoreTeam hls;
    private BoxScoreTeam vls;

    public BoxScoreValues(BoxScoreTeam hls, BoxScoreTeam vls) {
        this.hls = hls;
        this.vls = vls;
    }

    public BoxScoreTeam getHls() {
        return hls;
    }

    public void setHls(BoxScoreTeam hls) {
        this.hls = hls;
    }

    public BoxScoreTeam getVls() {
        return vls;
    }

    public void setVls(BoxScoreTeam vls) {
        this.vls = vls;
    }
}
