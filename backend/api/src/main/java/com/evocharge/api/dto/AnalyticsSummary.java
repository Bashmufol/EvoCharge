package com.evocharge.api.dto;

import java.util.LinkedHashMap;
import java.util.Map;

public class AnalyticsSummary {

    private int totalStations;
    private int available;
    private int busy;
    private int offline;
    private double utilizationPercent;
    private double avgWaitMinutes;
    private int peakHour;
    private Map<String, Integer> byOperator = new LinkedHashMap<>();

    public int getTotalStations() {
        return totalStations;
    }

    public void setTotalStations(int totalStations) {
        this.totalStations = totalStations;
    }

    public int getAvailable() {
        return available;
    }

    public void setAvailable(int available) {
        this.available = available;
    }

    public int getBusy() {
        return busy;
    }

    public void setBusy(int busy) {
        this.busy = busy;
    }

    public int getOffline() {
        return offline;
    }

    public void setOffline(int offline) {
        this.offline = offline;
    }

    public double getUtilizationPercent() {
        return utilizationPercent;
    }

    public void setUtilizationPercent(double utilizationPercent) {
        this.utilizationPercent = utilizationPercent;
    }

    public double getAvgWaitMinutes() {
        return avgWaitMinutes;
    }

    public void setAvgWaitMinutes(double avgWaitMinutes) {
        this.avgWaitMinutes = avgWaitMinutes;
    }

    public int getPeakHour() {
        return peakHour;
    }

    public void setPeakHour(int peakHour) {
        this.peakHour = peakHour;
    }

    public Map<String, Integer> getByOperator() {
        return byOperator;
    }

    public void setByOperator(Map<String, Integer> byOperator) {
        this.byOperator = byOperator;
    }
}
