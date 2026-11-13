package com.example.FitnessChallengeApp_Test.repository;

import com.example.FitnessChallengeApp_Test.model.WorkoutLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA Repository for WorkoutLog entities.
 * Students will extend this repository in the Lab with custom derived queries
 * and @Query JPQL methods supporting Pageable pagination and sorting.
 */
@Repository
public interface WorkoutLogRepository extends JpaRepository<WorkoutLog, Long> {
    Page<WorkoutLog> findByAthleteNameContainingIgnoreCase(String athleteName, Pageable pageable);
    Page<WorkoutLog> findByCaloriesBurnedGreaterThanEqual(int minCalories, Pageable pageable);

    @Query("SELECT w from WorkoutLog w Order by w.caloriesBurned DESC")
    Page<WorkoutLog> findHighestCalorieLog(Pageable pageable);

    @Query("SELECT w from WorkoutLog w WHERE LOWER(w.intensityLevel) = LOWER(?1) AND w.durationMinutes >= ?2 ORDER BY w.workoutDate DESC")
    Page<WorkoutLog> findLogsByIntensityLevelAndMinDuration(String intensity, int minDuration, Pageable pageable);
}
