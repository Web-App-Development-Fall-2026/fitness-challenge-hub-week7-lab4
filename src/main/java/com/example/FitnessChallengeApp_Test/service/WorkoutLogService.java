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
 * Supports custom queries, sorting, and pagination.
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

    // Get paginated workout logs
    public Page<WorkoutLog> getLogs(Pageable pageable) {
        return workoutLogRepository.findAll(pageable);
    }

    // Search workout logs by athlete name
    public Page<WorkoutLog> searchByAthleteName(
            String athleteName, Pageable pageable) {
        return workoutLogRepository.findByAthleteNameContainingIgnoreCase(
                athleteName, pageable);
    }

    // Filter workout logs by minimum calories burned
    public Page<WorkoutLog> filterByMinCalories(
            int minCalories, Pageable pageable) {
        return workoutLogRepository.findByCaloriesBurnedGreaterThanEqual(
                minCalories, pageable);
    }

    // Get workout logs ranked by highest calories burned
    public Page<WorkoutLog> getHighestCalorieLogs(Pageable pageable) {
        return workoutLogRepository.findHighestCalorieLogs(pageable);
    }

    // Filter by intensity and minimum duration
    public Page<WorkoutLog> filterByIntensityAndMinDuration(
            String intensity, int minDuration, Pageable pageable) {
        return workoutLogRepository.findLogsByIntensityAndMinDuration(
                intensity, minDuration, pageable);
    }

    // Get a workout log by ID
    public WorkoutLog getLogById(Long id) {
        if (id == null) {
            return null;
        }
        return workoutLogRepository.findById(id).orElse(null);
    }

    // Add a workout log with its associated challenge
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

    // Add a workout log
    public WorkoutLog addLog(WorkoutLog log) {
        return addLog(log, log.getChallenge());
    }

    // Get available intensity levels
    public List<String> getIntensityLevels() {
        return List.of("Low", "Moderate", "High", "Extreme");
    }
}