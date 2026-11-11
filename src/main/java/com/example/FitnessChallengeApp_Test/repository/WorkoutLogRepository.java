
package com.example.FitnessChallengeApp_Test.repository;

import com.example.FitnessChallengeApp_Test.model.WorkoutLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA Repository for WorkoutLog entities.
 * Demonstrates Derived Query Methods, Custom JPQL @Query methods,
 * and Pagination & Sorting via Pageable.
 */
@Repository
public interface WorkoutLogRepository extends JpaRepository<WorkoutLog, Long> {

    // Derived Query Methods (Method Name Keywords)

    // Case-insensitive partial athlete name search with pagination
    Page<WorkoutLog> findByAthleteNameContainingIgnoreCase(
            String athleteName, Pageable pageable);

    // Numeric threshold filter with pagination
    Page<WorkoutLog> findByCaloriesBurnedGreaterThanEqual(
            int minCalories, Pageable pageable);

    // Custom JPQL Queries (@Query)

    // Rank workout logs by highest calories burned descending
    @Query("SELECT w FROM WorkoutLog w ORDER BY w.caloriesBurned DESC")
    Page<WorkoutLog> findHighestCalorieLogs(Pageable pageable);

    // Filter by intensity and minimum duration, ordered by workout date
    @Query("SELECT w FROM WorkoutLog w WHERE LOWER(w.intensityLevel) = LOWER(?1) AND w.durationMinutes >= ?2 ORDER BY w.workoutDate DESC")
    Page<WorkoutLog> findLogsByIntensityAndMinDuration(
            String intensity, int minDuration, Pageable pageable);

}

