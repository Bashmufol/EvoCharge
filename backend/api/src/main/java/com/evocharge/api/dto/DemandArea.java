package com.evocharge.api.dto;

public class DemandArea {

    private String area;
    private String city;
    private double lat;
    private double lng;
    private int stationCount;
    private double demandScore;
    private boolean unmetDemand;
    private int estimatedDailySessions;

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public int getStationCount() {
        return stationCount;
    }

    public void setStationCount(int stationCount) {
        this.stationCount = stationCount;
    }

    public double getDemandScore() {
        return demandScore;
    }

    public void setDemandScore(double demandScore) {
        this.demandScore = demandScore;
    }

    public boolean isUnmetDemand() {
        return unmetDemand;
    }

    public void setUnmetDemand(boolean unmetDemand) {
        this.unmetDemand = unmetDemand;
    }

    public int getEstimatedDailySessions() {
        return estimatedDailySessions;
    }

    public void setEstimatedDailySessions(int estimatedDailySessions) {
        this.estimatedDailySessions = estimatedDailySessions;
    }
}
