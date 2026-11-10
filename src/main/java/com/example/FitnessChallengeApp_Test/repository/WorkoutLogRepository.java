package com.example.FitnessChallengeApp_Test.repository;

import com.example.FitnessChallengeApp_Test.model.WorkoutLog;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * Repository for managing WorkoutLog entities.
 * Lab 4: Custom Queries, Sorting, Filtering and Pagination.
 */
public interface WorkoutLogRepository
        extends JpaRepository<WorkoutLog, Long> {

    // ==========================================
    // LAB 4 - DERIVED QUERY METHODS
    // ==========================================

    // Derived Query 1: Search workout logs by athlete name
    // Case-insensitive search with pagination
    Page<WorkoutLog> findByAthleteNameContainingIgnoreCase(
            String athleteName, Pageable pageable);

    // Derived Query 2: Filter workout logs by minimum calories burned
    // Returns workouts with calories >= minCalories
    Page<WorkoutLog> findByCaloriesBurnedGreaterThanEqual(
            int minCalories, Pageable pageable);

    // ==========================================
    // LAB 4 - CUSTOM JPQL QUERIES
    // ==========================================

    // JPQL Query 1: Get highest calorie workout logs
    // Sorted by calories burned in descending order
    @Query("SELECT w FROM WorkoutLog w ORDER BY w.caloriesBurned DESC")
    Page<WorkoutLog> findHighestCalorieLogs(Pageable pageable);

    // JPQL Query 2: Filter workout logs by intensity and duration
    // Minimum duration will be 25 minutes in the controller
    // Results sorted by workout date descending
    @Query("SELECT w FROM WorkoutLog w " +
           "WHERE LOWER(w.intensityLevel) = LOWER(?1) " +
           "AND w.durationMinutes >= ?2 " +
           "ORDER BY w.workoutDate DESC")
    Page<WorkoutLog> findLogsByIntensityAndMinDuration(
            String intensity, int minDuration, Pageable pageable);
}