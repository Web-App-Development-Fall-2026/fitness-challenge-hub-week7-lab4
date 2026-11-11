package com.example.FitnessChallengeApp_Test;

import com.example.FitnessChallengeApp_Test.controller.WorkoutLogController;
import com.example.FitnessChallengeApp_Test.model.WorkoutLog;
import com.example.FitnessChallengeApp_Test.repository.WorkoutLogRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ExtendedModelMap;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class WorkoutLogQueriesTest {
    @Autowired WorkoutLogRepository repository;
    @Autowired WorkoutLogController controller;

    private void log(String name, int calories, int duration, String intensity, String date) {
        WorkoutLog log = new WorkoutLog();
        log.setAthleteName(name);
        log.setCaloriesBurned(calories);
        log.setDurationMinutes(duration);
        log.setIntensityLevel(intensity);
        log.setWorkoutDate(date);
        repository.saveAndFlush(log);
    }

    @Test
    void queriesRespectCaseThresholdsRankingAndPagination() {
        repository.deleteAll();
        log("Alex One", 200, 25, "High", "2026-11-01");
        log("Alex Two", 500, 24, "High", "2026-11-03");
        log("Taylor Three", 350, 40, "High", "2026-11-02");
        log("Taylor Four", 199, 40, "Low", "2026-11-04");
        var athletes = repository.findByAthleteNameContainingIgnoreCase("aLeX", PageRequest.of(0, 1));
        assertEquals(2, athletes.getTotalElements());
        assertEquals(2, athletes.getTotalPages());
        assertEquals(3, repository.findByCaloriesBurnedGreaterThanEqual(200, PageRequest.of(0, 10)).getTotalElements());
        var ranked = repository.findHighestCalorieLogs(PageRequest.of(0, 2));
        assertEquals(500, ranked.getContent().get(0).getCaloriesBurned());
        assertEquals(350, ranked.getContent().get(1).getCaloriesBurned());
        var intense = repository.findLogsByIntensityAndMinDuration("hIgH", 25, PageRequest.of(0, 10));
        assertEquals(2, intense.getTotalElements());
        assertEquals("2026-11-02", intense.getContent().get(0).getWorkoutDate());
    }

    @Test
    void controllerDefaultsInvalidCaloriesAndPreservesNavigationState() {
        repository.deleteAll();
        log("Alex One", 200, 25, "High", "2026-11-01");
        log("Taylor Two", 199, 30, "Low", "2026-11-02");
        var model = new ExtendedModelMap();
        assertEquals("workout-logs", controller.showWorkoutLogs(0, 1, "athleteName", "ASC", "invalid", "calories", model));
        assertEquals(1L, model.get("totalElements"));
        assertEquals("calories", model.get("filterType"));
        assertEquals("ASC", model.get("direction"));
        assertEquals(false, model.get("hasNext"));
        model.clear();
        controller.showWorkoutLogs(-1, 0, "unknown", "invalid", null, "all", model);
        assertEquals(0, model.get("currentPage"));
        assertEquals(1, model.get("pageSize"));
        assertEquals("id", model.get("sort"));
        assertEquals("DESC", model.get("direction"));
        assertEquals(true, model.get("hasNext"));
    }
}
