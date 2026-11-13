
package com.example.FitnessChallengeApp_Test.repository;

import com.example.FitnessChallengeApp_Test.model.WorkoutLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA Repository for WorkoutLog entities.
 * Lab 4 - Derived Queries, JPQL Queries and Pagination.
 */
@Repository
public interface WorkoutLogRepository extends JpaRepository<WorkoutLog, Long> {

    // Derived Query 1: Search by athlete name
    Page<WorkoutLog> findByAthleteNameContainingIgnoreCase(
            String athleteName, Pageable pageable);

    // Derived Query 2: Filter by minimum calories
    Page<WorkoutLog> findByCaloriesBurnedGreaterThanEqual(
            int minCalories, Pageable pageable);

    // JPQL Query 1: Highest calorie burn first
    @Query("SELECT w FROM WorkoutLog w ORDER BY w.caloriesBurned DESC")
    Page<WorkoutLog> findHighestCalorieLogs(Pageable pageable);

    // JPQL Query 2: Intensity and minimum duration
    @Query("SELECT w FROM WorkoutLog w " +
           "WHERE LOWER(w.intensityLevel) = LOWER(?1) " +
           "AND w.durationMinutes >= ?2 " +
           "ORDER BY w.workoutDate DESC")
    Page<WorkoutLog> findLogsByIntensityAndMinDuration(
            String intensity, int minDuration, Pageable pageable);
}
