package com.evocharge.api.dto;

import com.evocharge.api.dto.RecommendResponse.RankedStation;

import java.util.ArrayList;
import java.util.List;

public class AdvisorResponse {

    private String answer;
    private List<RankedStation> stations = new ArrayList<>();

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public List<RankedStation> getStations() {
        return stations;
    }

    public void setStations(List<RankedStation> stations) {
        this.stations = stations;
    }
}
