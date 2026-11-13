package com.example.FitnessChallengeApp_Test.service;

import com.example.FitnessChallengeApp_Test.model.Challenge;
import com.example.FitnessChallengeApp_Test.model.WorkoutLog;
import com.example.FitnessChallengeApp_Test.repository.WorkoutLogRepository;
import lombok.Getter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * Service managing Spring Data JPA persistence for Athlete Workout Logs.
 */
@Service
@Getter
public class WorkoutLogService {

    private final WorkoutLogRepository workoutLogRepository;

    public WorkoutLogService(WorkoutLogRepository workoutLogRepository) {
        this.workoutLogRepository = workoutLogRepository;
    }

    //Paginated list of workout logs
    public Page<WorkoutLog> getLogs(Pageable pageable){
        return workoutLogRepository.findAll(pageable);
    }

    public List<WorkoutLog> getAllLogs() {
        return workoutLogRepository.findAll();
    }

    public WorkoutLog getLogById(Long id) {
        if (id == null) {
            return null;
        }
        return workoutLogRepository.findById(id).orElse(null);
    }

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

    public List<String> getIntensityLevels() {
        return List.of("Low", "Moderate", "High", "Extreme");
    }

    public Page<WorkoutLog> getLogsByAthleteName(String athleteName, Pageable pageable) {
        return workoutLogRepository.findByAthleteNameContainingIgnoreCase(athleteName, pageable);
    }
    public Page<WorkoutLog> getLogsByMinCalories(int minCalories, Pageable pageable) {
        return workoutLogRepository.findByCaloriesBurnedGreaterThanEqual(minCalories, pageable);
    }
    public Page<WorkoutLog> getHighestCalorieLog(Pageable pageable) {
        return workoutLogRepository.findHighestCalorieLog(pageable);
    }
    public Page<WorkoutLog> getLogsByIntensityAndDuration(String intensity, int minDuration, Pageable pageable) {
        return workoutLogRepository.findLogsByIntensityLevelAndMinDuration(intensity, minDuration, pageable);
    }
}

