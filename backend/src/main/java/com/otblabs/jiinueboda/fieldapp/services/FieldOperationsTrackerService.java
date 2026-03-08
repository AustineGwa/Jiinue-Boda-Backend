package com.otblabs.jiinueboda.fieldapp.services;

import com.otblabs.jiinueboda.fieldapp.models.*;
import com.otblabs.jiinueboda.fieldapp.repository.FieldOperationsTrackerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FieldOperationsTrackerService {

    private final FieldOperationsTrackerRepository trackerRepository;

    @Transactional
    public Long startDay(Long userId, StartDayRequest request) throws Exception{
        log.info("Starting day for user: {}", userId);

        // Check if user already has an active session today
        var existingSession = trackerRepository.findActiveSessionByUser(userId, LocalDate.now());
        if (existingSession.isPresent()) {
            throw new IllegalStateException("User already has an active session today");
        }

        var tracker = FieldOperationsTracker.builder()
                .userId(userId)
                .sessionDate(LocalDate.now())
                .startTime(LocalDateTime.now())
                .sessionStatus("active")
                .startLat(request.getStartLat())
                .startLng(request.getStartLng())
                .startLocationAccuracy(request.getStartLocationAccuracy())
                .workCounty(request.getWorkCounty())
                .workSubCounty(request.getWorkSubCounty())
                .workWard(request.getWorkWard())
                .workArea(request.getWorkArea())
                .workStage(request.getWorkStage())
                .activity(request.getActivity())
                .deviceInfo(request.getDeviceInfo())
                .networkStatus(request.getNetworkStatus())
                .batteryLevel(request.getBatteryLevel())
                .appVersion(request.getAppVersion())
                .weatherConditions(request.getWeatherConditions())
                .temperature(request.getTemperature())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        var savedTracker = trackerRepository.save(tracker);
        log.info("Day started successfully for user: {} with tracker ID: {}", userId, savedTracker.getId());

        return savedTracker.getId();
    }

    @Transactional
    public void endDay(Long userId, EndDayRequest request) {
        log.info("Ending day for user: {}", userId);

        var activeSession = trackerRepository.findActiveSessionByUser(userId, LocalDate.now())
                .orElseThrow(() -> new IllegalStateException("No active session found for user"));

        trackerRepository.endSession(
                activeSession.getId(),
                LocalDateTime.now(),
                request.getEndLat(),
                request.getEndLong(),
                request.getEndLocationAccuracy(),
                LocalDateTime.now()
        );

        log.info("Day ended successfully for user: {}", userId);
    }

    @Transactional
    public void pauseSession(Long userId, PauseSessionRequest request) {
        log.info("Pausing session for user: {}", userId);

        var activeSession = trackerRepository.findActiveSessionByUser(userId, LocalDate.now())
                .orElseThrow(() -> new IllegalStateException("No active session found for user"));

        trackerRepository.updateSessionStatus(activeSession.getId(), "paused", LocalDateTime.now());

        log.info("Session paused successfully for user: {}", userId);
    }

    @Transactional
    public void resumeSession(Long userId) {
        log.info("Resuming session for user: {}", userId);

        var pausedSession = trackerRepository.findActiveSessionByUser(userId, LocalDate.now())
                .orElseThrow(() -> new IllegalStateException("No active session found for user"));

        if (!"paused".equals(pausedSession.getSessionStatus())) {
            throw new IllegalStateException("Session is not in paused state");
        }

        trackerRepository.updateSessionStatus(pausedSession.getId(), "active", LocalDateTime.now());

        log.info("Session resumed successfully for user: {}", userId);
    }

    @Transactional
    public void updateSession(Long userId, UpdateSessionRequest request) {
        log.info("Updating session for user: {}", userId);

        var activeSession = trackerRepository.findActiveSessionByUser(userId, LocalDate.now())
                .orElseThrow(() -> new IllegalStateException("No active session found for user"));

        trackerRepository.updateSessionInfo(
                activeSession.getId(),
                request.getNetworkStatus(),
                request.getBatteryLevel(),
                request.getWeatherConditions(),
                request.getTemperature(),
                LocalDateTime.now()
        );

        log.info("Session updated successfully for user: {}", userId);
    }

    @Transactional
    public void cancelSession(Long userId) {
        log.info("Cancelling session for user: {}", userId);

        var activeSession = trackerRepository.findActiveSessionByUser(userId, LocalDate.now())
                .orElseThrow(() -> new IllegalStateException("No active session found for user"));

        trackerRepository.updateSessionStatus(activeSession.getId(), "cancelled", LocalDateTime.now());

        log.info("Session cancelled successfully for user: {}", userId);
    }

    public Optional<FieldOperationsTracker> getActiveSessionForUser(Long userId) {
        return  trackerRepository.findActiveSessionByUser(userId, LocalDate.now());
    }
}