package com.gmail.jorgegilcavazos.ballislife.features.model;

import java.util.List;

public class Standings {

    private List<TeamStanding> east;
    private List<TeamStanding> west;

    public Standings(List<TeamStanding> east, List<TeamStanding> west) {
        this.east = east;
        this.west = west;
    }

    public List<TeamStanding> getEast() {
        return east;
    }

    public void setEast(List<TeamStanding> east) {
        this.east = east;
    }

    public List<TeamStanding> getWest() {
        return west;
    }

    public void setWest(List<TeamStanding> west) {
        this.west = west;
    }

    public class TeamStanding {

        private String abbreviation;
        private String location;
        private String name;
        private String seed;
        private List<StandingStat> stats;

        public TeamStanding(String abbreviation, String location, String name, String seed, List<StandingStat> stats) {
            this.abbreviation = abbreviation;
            this.location = location;
            this.name = name;
            this.seed = seed;
            this.stats = stats;
        }

        public String getAbbreviation() {
            return abbreviation;
        }

        public void setAbbreviation(String abbreviation) {
            this.abbreviation = abbreviation;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getSeed() {
            return seed;
        }

        public void setSeed(String seed) {
            this.seed = seed;
        }

        public List<StandingStat> getStats() {
            return stats;
        }

        public void setStats(List<StandingStat> stats) {
            this.stats = stats;
        }
    }

    public class StandingStat {

        private String name;
        private String value;

        public StandingStat(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
