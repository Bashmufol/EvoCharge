package com.evocharge.api.dto;

import com.evocharge.api.model.StationStatus;

import java.time.Instant;

public class StatusEvent {

    private String stationId;
    private StationStatus status;
    private int waitMinutes;
    private Instant timestamp;

    public StatusEvent() {
    }

    public StatusEvent(String stationId, StationStatus status, int waitMinutes) {
        this.stationId = stationId;
        this.status = status;
        this.waitMinutes = waitMinutes;
        this.timestamp = Instant.now();
    }

    public String getStationId() {
        return stationId;
    }

    public void setStationId(String stationId) {
        this.stationId = stationId;
    }

    public StationStatus getStatus() {
        return status;
    }

    public void setStatus(StationStatus status) {
        this.status = status;
    }

    public int getWaitMinutes() {
        return waitMinutes;
    }

    public void setWaitMinutes(int waitMinutes) {
        this.waitMinutes = waitMinutes;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
