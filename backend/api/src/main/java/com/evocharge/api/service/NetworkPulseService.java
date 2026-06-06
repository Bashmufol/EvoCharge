package com.evocharge.api.service;

import com.evocharge.api.dto.StatusEvent;
import com.evocharge.api.model.Station;
import com.evocharge.api.model.StationStatus;
import com.evocharge.api.repository.StationRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class NetworkPulseService {

    private final StationRepository stationRepository;
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private final Random random = new Random();

    public NetworkPulseService(StationRepository stationRepository) {
        this.stationRepository = stationRepository;
    }

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(0L);
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(e -> emitters.remove(emitter));
        try {
            emitter.send(SseEmitter.event().name("connected").data("{\"status\":\"connected\"}"));
        } catch (IOException e) {
            emitters.remove(emitter);
        }
        return emitter;
    }

    @Scheduled(fixedDelayString = "${evocharge.pulse.interval-seconds:120}000", initialDelay = 30000)
    public void pulseStatusUpdates() {
        List<Station> stations = stationRepository.findAll();
        if (stations.isEmpty()) {
            return;
        }

        int updates = Math.min(3, stations.size());
        for (int i = 0; i < updates; i++) {
            Station station = stations.get(random.nextInt(stations.size()));
            StationStatus newStatus = rotateStatus(station.getStatus());
            int wait = newStatus == StationStatus.OFFLINE ? 0 :
                    newStatus == StationStatus.BUSY ? 10 + random.nextInt(20) : random.nextInt(8);

            station.setStatus(newStatus);
            station.setWaitMinutes(wait);
            station.setLastUpdated(Instant.now());
            stationRepository.save(station);

            broadcast(new StatusEvent(station.getId(), newStatus, wait));
        }
    }

    private StationStatus rotateStatus(StationStatus current) {
        return switch (current) {
            case AVAILABLE -> random.nextBoolean() ? StationStatus.BUSY : StationStatus.AVAILABLE;
            case BUSY -> random.nextBoolean() ? StationStatus.AVAILABLE : StationStatus.BUSY;
            case OFFLINE -> random.nextInt(3) == 0 ? StationStatus.AVAILABLE : StationStatus.OFFLINE;
        };
    }

    private void broadcast(StatusEvent event) {
        List<SseEmitter> dead = new CopyOnWriteArrayList<>();
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name("status").data(event));
            } catch (IOException e) {
                dead.add(emitter);
            }
        }
        emitters.removeAll(dead);
    }
}
