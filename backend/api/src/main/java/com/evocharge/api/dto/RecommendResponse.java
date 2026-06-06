package com.evocharge.api.dto;

import com.evocharge.api.model.Station;

import java.util.ArrayList;
import java.util.List;

public class RecommendResponse {

    private List<RankedStation> recommendations = new ArrayList<>();

    public List<RankedStation> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(List<RankedStation> recommendations) {
        this.recommendations = recommendations;
    }

    public static class RankedStation {
        private Station station;
        private double evoScore;
        private double distanceKm;
        private int etaMinutes;
        private String reason;

        public Station getStation() {
            return station;
        }

        public void setStation(Station station) {
            this.station = station;
        }

        public double getEvoScore() {
            return evoScore;
        }

        public void setEvoScore(double evoScore) {
            this.evoScore = evoScore;
        }

        public double getDistanceKm() {
            return distanceKm;
        }

        public void setDistanceKm(double distanceKm) {
            this.distanceKm = distanceKm;
        }

        public int getEtaMinutes() {
            return etaMinutes;
        }

        public void setEtaMinutes(int etaMinutes) {
            this.etaMinutes = etaMinutes;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }
}
