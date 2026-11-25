package com.example.FitnessChallengeApp_Test.repository;
import com.example.FitnessChallengeApp_Test.model.WorkoutLog;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
@Repository
public interface WorkoutLogRepository extends JpaRepository<WorkoutLog, Long> {
    Page<WorkoutLog> findByAthleteNameContainingIgnoreCase(String athleteName, Pageable pageable);
    Page<WorkoutLog> findByCaloriesBurnedGreaterThanEqual(int minCalories, Pageable pageable);
    @Query("SELECT w FROM WorkoutLog w ORDER BY w.caloriesBurned DESC")
    Page<WorkoutLog> findHighestCalorieLogs(Pageable pageable);
    @Query("SELECT w FROM WorkoutLog w WHERE LOWER(w.intensityLevel) = LOWER(?1) AND w.durationMinutes >= ?2 ORDER BY w.workoutDate DESC")
    Page<WorkoutLog> findLogsByIntensityAndMinDuration(String intensity, int minDuration, Pageable pageable);
}
