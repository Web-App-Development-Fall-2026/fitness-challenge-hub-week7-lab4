package com.example.FitnessChallengeApp_Test.repository;

import com.example.FitnessChallengeApp_Test.model.WorkoutLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA Repository for WorkoutLog entities.
 * Students will extend this repository in the Lab with custom derived queries
 * and @Query JPQL methods supporting Pageable pagination and sorting.
 */

package com.example.FitnessChallengeApp_Test.repository;

import com.example.FitnessChallengeApp_Test.model.WorkoutLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
@Repository
public interface WorkoutLogRepository extends JpaRepository<WorkoutLog, Long> {
	 // Derived queries (Spring builds the SQL from the method name)
    Page<WorkoutLog> findByAthleteNameContainingIgnoreCase(String athleteName, Pageable pageable);

    Page<WorkoutLog> findByCaloriesBurnedGreaterThanEqual(int minCalories, Pageable pageable);

    // Custom JPQL queries
    @Query("SELECT w FROM WorkoutLog w ORDER BY w.caloriesBurned DESC")
    Page<WorkoutLog> findHighestCalorieLogs(Pageable pageable);

    @Query("SELECT w FROM WorkoutLog w WHERE LOWER(w.intensityLevel) = LOWER(?1) AND w.durationMinutes >= ?2 ORDER BY w.workoutDate DESC")
    Page<WorkoutLog> findLogsByIntensityAndMinDuration(String intensity, int minDuration, Pageable pageable);
}
}

