package com.gmail.jorgegilcavazos.ballislife.features.model;

/**
 * Class that holds general information about a specific NBA game.
 */
public class NbaGame {

    public static final String PRE_GAME = "1";
    public static final String IN_GAME = "2";
    public static final String POST_GAME = "3";

    private String id;
    private String date;
    private String time;
    private String timeUtc;
    private String arena;
    private String city;
    private String periodValue;
    private String periodStatus;
    private String gameStatus;
    private String gameClock;
    private String totalPeriods;
    private String periodName;

    private String homeTeamId;
    private String homeTeamKey;
    private String homeTeamCity;
    private String homeTeamAbbr;
    private String homeTeamNickname;
    private String homeTeamScore;

    private String awayTeamId;
    private String awayTeamKey;
    private String awayTeamCity;
    private String awayTeamAbbr;
    private String awayTeamNickname;
    private String awayTeamScore;


    public String toString() {
        return homeTeamAbbr + "(" + homeTeamScore + ") " + awayTeamAbbr + "(" + awayTeamScore + ")";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getArena() {
        return arena;
    }

    public void setArena(String arena) {
        this.arena = arena;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPeriodValue() {
        return periodValue;
    }

    public void setPeriodValue(String periodValue) {
        this.periodValue = periodValue;
    }

    public String getPeriodStatus() {
        return periodStatus;
    }

    public void setPeriodStatus(String periodStatus) {
        this.periodStatus = periodStatus;
    }

    public String getGameStatus() {
        return gameStatus;
    }

    public void setGameStatus(String gameStatus) {
        this.gameStatus = gameStatus;
    }

    public String getGameClock() {
        return gameClock;
    }

    public void setGameClock(String gameClock) {
        this.gameClock = gameClock;
    }

    public String getTotalPeriods() {
        return totalPeriods;
    }

    public void setTotalPeriods(String totalPeriods) {
        this.totalPeriods = totalPeriods;
    }

    public String getPeriodName() {
        return periodName;
    }

    public void setPeriodName(String periodName) {
        this.periodName = periodName;
    }

    public String getHomeTeamId() {
        return homeTeamId;
    }

    public void setHomeTeamId(String homeTeamId) {
        this.homeTeamId = homeTeamId;
    }

    public String getHomeTeamKey() {
        return homeTeamKey;
    }

    public void setHomeTeamKey(String homeTeamKey) {
        this.homeTeamKey = homeTeamKey;
    }

    public String getHomeTeamCity() {
        return homeTeamCity;
    }

    public void setHomeTeamCity(String homeTeamCity) {
        this.homeTeamCity = homeTeamCity;
    }

    public String getHomeTeamAbbr() {
        return homeTeamAbbr;
    }

    public void setHomeTeamAbbr(String homeTeamAbbr) {
        this.homeTeamAbbr = homeTeamAbbr;
    }

    public String getHomeTeamNickname() {
        return homeTeamNickname;
    }

    public void setHomeTeamNickname(String homeTeamNickname) {
        this.homeTeamNickname = homeTeamNickname;
    }

    public String getHomeTeamScore() {
        return homeTeamScore;
    }

    public void setHomeTeamScore(String homeTeamScore) {
        this.homeTeamScore = homeTeamScore;
    }

    public String getAwayTeamId() {
        return awayTeamId;
    }

    public void setAwayTeamId(String awayTeamId) {
        this.awayTeamId = awayTeamId;
    }

    public String getAwayTeamKey() {
        return awayTeamKey;
    }

    public void setAwayTeamKey(String awayTeamKey) {
        this.awayTeamKey = awayTeamKey;
    }

    public String getAwayTeamCity() {
        return awayTeamCity;
    }

    public void setAwayTeamCity(String awayTeamCity) {
        this.awayTeamCity = awayTeamCity;
    }

    public String getAwayTeamAbbr() {
        return awayTeamAbbr;
    }

    public void setAwayTeamAbbr(String awayTeamAbbr) {
        this.awayTeamAbbr = awayTeamAbbr;
    }

    public String getAwayTeamNickname() {
        return awayTeamNickname;
    }

    public void setAwayTeamNickname(String awayTeamNickname) {
        this.awayTeamNickname = awayTeamNickname;
    }

    public String getAwayTeamScore() {
        return awayTeamScore;
    }

    public void setAwayTeamScore(String awayTeamScore) {
        this.awayTeamScore = awayTeamScore;
    }
}
