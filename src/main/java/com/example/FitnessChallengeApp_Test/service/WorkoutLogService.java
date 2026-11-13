
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
 */
@Service
public class WorkoutLogService {

    private final WorkoutLogRepository workoutLogRepository;

    public WorkoutLogService(WorkoutLogRepository workoutLogRepository) {
        this.workoutLogRepository = workoutLogRepository;
    }

    // =========================================================================
    // 1. Basic CRUD Operations
    // =========================================================================

    public List<WorkoutLog> getAllLogs() {
        return workoutLogRepository.findAll();
    }

    public WorkoutLog getLogById(Long id) {
        if (id == null) {
            return null;
        }
        return workoutLogRepository.findById(id).orElse(null);
    }

    // =========================================================================
    // 2. Pagination
    // =========================================================================

    // Get workout logs using pagination and sorting
    public Page<WorkoutLog> getLogs(Pageable pageable) {
        return workoutLogRepository.findAll(pageable);
    }

    // =========================================================================
    // 3. Derived Query Methods
    // =========================================================================

    // Search workout logs by athlete name (case-insensitive)
    public Page<WorkoutLog> findByAthleteNameContainingIgnoreCase(
            String athleteName, Pageable pageable) {
        return workoutLogRepository.findByAthleteNameContainingIgnoreCase(
                athleteName, pageable);
    }

    // Find workout logs with calories burned greater than or equal to a value
    public Page<WorkoutLog> findByCaloriesBurnedGreaterThanEqual(
            int minCalories, Pageable pageable) {
        return workoutLogRepository.findByCaloriesBurnedGreaterThanEqual(
                minCalories, pageable);
    }

    // =========================================================================
    // 4. Custom JPQL Queries
    // =========================================================================

    // Find workout logs ranked by highest calories burned
    public Page<WorkoutLog> findHighestCalorieLogs(Pageable pageable) {
        return workoutLogRepository.findHighestCalorieLogs(pageable);
    }

    // Find workout logs by intensity and minimum duration
    public Page<WorkoutLog> findLogsByIntensityAndMinDuration(
            String intensity, int minDuration, Pageable pageable) {
        return workoutLogRepository.findLogsByIntensityAndMinDuration(
                intensity, minDuration, pageable);
    }

    // =========================================================================
    // 5. Add Workout Logs
    // =========================================================================

    public WorkoutLog addLog(WorkoutLog log, Challenge challenge) {
        if (challenge != null) {
            log.setChallenge(challenge);
            log.setChallengeTitle(challenge.getTitle());
        } else if (log.getChallenge() != null && log.getChallenge().getTitle() != null) {
            log.setChallengeTitle(log.getChallenge().getTitle());
        }

        if (log.getCreatedAt() == null) {
            log.setCreatedAt(new Date());
        }

        return workoutLogRepository.save(log);
    }

    public WorkoutLog addLog(WorkoutLog log) {
        return addLog(log, log.getChallenge());
    }

    // =========================================================================
    // 6. Intensity Levels
    // =========================================================================

    public List<String> getIntensityLevels() {
        return List.of("Low", "Moderate", "High", "Extreme");
    }
}
