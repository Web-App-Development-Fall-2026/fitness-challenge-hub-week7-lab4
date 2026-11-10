package com.example.FitnessChallengeApp_Test.service;

import com.example.FitnessChallengeApp_Test.model.Challenge;
import com.example.FitnessChallengeApp_Test.model.WorkoutLog;
import com.example.FitnessChallengeApp_Test.repository.WorkoutLogRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * Service managing Spring Data JPA persistence for Athlete Workout Logs.
 * Lab 4: Custom Queries, Sorting, Filtering and Pagination.
 */
@Service
public class WorkoutLogService {

    private final WorkoutLogRepository workoutLogRepository;

    public WorkoutLogService(WorkoutLogRepository workoutLogRepository) {
        this.workoutLogRepository = workoutLogRepository;
    }

    // Get all workout logs
    public List<WorkoutLog> getAllLogs() {
        return workoutLogRepository.findAll();
    }

    // Get workout log by ID
    public WorkoutLog getLogById(Long id) {
        if (id == null) {
            return null;
        }
        return workoutLogRepository.findById(id).orElse(null);
    }

    // Add workout log with challenge
    public WorkoutLog addLog(WorkoutLog log, Challenge challenge) {
        if (challenge != null) {
            log.setChallenge(challenge);
            log.setChallengeTitle(challenge.getTitle());
        } else if (log.getChallenge() != null
                && log.getChallenge().getTitle() != null) {
            log.setChallengeTitle(log.getChallenge().getTitle());
        }

        if (log.getCreatedAt() == null) {
            log.setCreatedAt(new Date());
        }

        return workoutLogRepository.save(log);
    }

    // Add workout log
    public WorkoutLog addLog(WorkoutLog log) {
        return addLog(log, log.getChallenge());
    }

    // Get available intensity levels
    public List<String> getIntensityLevels() {
        return List.of("Low", "Moderate", "High", "Extreme");
    }

    // ==========================================
    // LAB 4 - CUSTOM QUERIES AND PAGINATION
    // ==========================================

    // Get all workout logs with pagination
    public Page<WorkoutLog> getLogs(Pageable pageable) {
        return workoutLogRepository.findAll(pageable);
    }

    // Search workout logs by athlete name
    public Page<WorkoutLog> getLogsByAthlete(
            String athleteName, Pageable pageable) {

        return workoutLogRepository
                .findByAthleteNameContainingIgnoreCase(
                        athleteName, pageable);
    }

    // Filter workout logs by minimum calories burned
    public Page<WorkoutLog> getLogsByMinCalories(
            int minCalories, Pageable pageable) {

        return workoutLogRepository
                .findByCaloriesBurnedGreaterThanEqual(
                        minCalories, pageable);
    }

    // Get workout logs with highest calories burned
    public Page<WorkoutLog> getHighestCalorieLogs(Pageable pageable) {

        return workoutLogRepository
                .findHighestCalorieLogs(pageable);
    }

    // Filter workout logs by intensity and minimum duration
    public Page<WorkoutLog> getLogsByIntensityAndDuration(
            String intensity, int minDuration, Pageable pageable) {

        return workoutLogRepository
                .findLogsByIntensityAndMinDuration(
                        intensity, minDuration, pageable);
    }
}