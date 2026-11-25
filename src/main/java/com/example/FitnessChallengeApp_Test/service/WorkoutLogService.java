package com.example.FitnessChallengeApp_Test.service;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.FitnessChallengeApp_Test.model.Challenge;
import com.example.FitnessChallengeApp_Test.model.WorkoutLog;
import com.example.FitnessChallengeApp_Test.repository.WorkoutLogRepository;

@Service
public class WorkoutLogService {

    private final WorkoutLogRepository workoutLogRepository;

    public WorkoutLogService(WorkoutLogRepository workoutLogRepository) {
        this.workoutLogRepository = workoutLogRepository;
    }

    // ===== Original methods =====

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

    // ===== Lab 4: paginated search and sorting =====

    public Page<WorkoutLog> getLogs(Pageable p) {
        return workoutLogRepository.findAll(p);
    }

    public Page<WorkoutLog> searchByAthlete(String name, Pageable p) {
        return workoutLogRepository.findByAthleteNameContainingIgnoreCase(name, p);
    }

    public Page<WorkoutLog> filterByMinCalories(int min, Pageable p) {
        return workoutLogRepository.findByCaloriesBurnedGreaterThanEqual(min, p);
    }

    public Page<WorkoutLog> getHighestCalorieLogs(Pageable p) {
        return workoutLogRepository.findHighestCalorieLogs(p);
    }

    public Page<WorkoutLog> getByIntensityAndMinDuration(String intensity, int minDuration, Pageable p) {
        return workoutLogRepository.findLogsByIntensityAndMinDuration(intensity, minDuration, p);
    }
}