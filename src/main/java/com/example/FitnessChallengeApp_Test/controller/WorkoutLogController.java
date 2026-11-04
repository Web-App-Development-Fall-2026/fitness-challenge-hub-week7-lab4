package com.example.FitnessChallengeApp_Test.controller;

import com.example.FitnessChallengeApp_Test.model.Challenge;
import com.example.FitnessChallengeApp_Test.model.WorkoutLog;
import com.example.FitnessChallengeApp_Test.service.ChallengeService;
import com.example.FitnessChallengeApp_Test.service.WorkoutLogService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Set;

/**
 * Controller for Athlete Workout Logging.
 * Handles workout form rendering, @ManyToOne Challenge binding, Bean Validation,
 * and listing logged workouts. Students will refactor displayWorkoutLogs() in the Lab
 * to support pagination, sorting, and custom repository queries.
 */
@Controller
public class WorkoutLogController {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id", "athleteName", "workoutDate", "durationMinutes", "caloriesBurned", "intensityLevel"
    );

    private final WorkoutLogService workoutLogService;
    private final ChallengeService challengeService;

    public WorkoutLogController(WorkoutLogService workoutLogService, ChallengeService challengeService) {
        this.workoutLogService = workoutLogService;
        this.challengeService = challengeService;
    }

    @GetMapping("/log-workout")
    public String showLogWorkoutForm(Model model) {
        WorkoutLog workoutLog = new WorkoutLog();
        workoutLog.setChallenge(new Challenge());
        model.addAttribute("workoutLog", workoutLog);
        model.addAttribute("challenges", challengeService.getAllChallenges());
        model.addAttribute("intensityLevels", workoutLogService.getIntensityLevels());
        model.addAttribute("pageTitle", "FitSquad Hub - Log Workout Session");
        return "log-workout";
    }

    @PostMapping("/log-workout")
    public String processLogWorkout(
            @Valid @ModelAttribute("workoutLog") WorkoutLog workoutLog,
            BindingResult bindingResult,
            Model model
    ) {
        Challenge selectedChallenge = null;
        if (workoutLog.getChallenge() == null || workoutLog.getChallenge().getId() == null) {
            bindingResult.rejectValue("challenge.id", "error.challenge", "Please select an active challenge");
        } else {
            selectedChallenge = challengeService.getChallengeById(workoutLog.getChallenge().getId());
            if (selectedChallenge == null) {
                bindingResult.rejectValue("challenge.id", "error.challenge", "Selected challenge does not exist");
            }
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("challenges", challengeService.getAllChallenges());
            model.addAttribute("intensityLevels", workoutLogService.getIntensityLevels());
            model.addAttribute("pageTitle", "FitSquad Hub - Log Workout Session");
            return "log-workout";
        }

        workoutLogService.addLog(workoutLog, selectedChallenge);
        return "redirect:/workout-logs";
    }

    @GetMapping("/workout-logs")
    public String showWorkoutLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "DESC") String direction,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "all") String filterType,
            Model model
    ) {
        String sortField = ALLOWED_SORT_FIELDS.contains(sort) ? sort : "id";
        Sort.Direction sortDirection = "ASC".equalsIgnoreCase(direction)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        int pageNumber = Math.max(page, 0);
        int pageSize = Math.max(size, 1);
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(sortDirection, sortField));
        String searchTerm = search == null ? "" : search.trim();

        Page<WorkoutLog> logPage;
        switch (filterType) {
            case "athlete" ->
                    logPage = workoutLogService.findByAthleteNameContainingIgnoreCase(searchTerm, pageable);
            case "calories" -> {
                int minCalories;
                try {
                    minCalories = Integer.parseInt(searchTerm);
                } catch (NumberFormatException e) {
                    minCalories = 200;
                }
                logPage = workoutLogService.findByCaloriesBurnedGreaterThanEqual(minCalories, pageable);
            }
            case "highestCalories" -> logPage = workoutLogService.findHighestCalorieLogs(pageable);
            case "intensityDuration" ->
                    logPage = workoutLogService.findLogsByIntensityAndMinDuration(searchTerm, 25, pageable);
            default -> logPage = workoutLogService.getLogs(pageable);
        }

        model.addAttribute("workoutLogs", logPage.getContent());
        model.addAttribute("totalPages", logPage.getTotalPages());
        model.addAttribute("totalElements", logPage.getTotalElements());
        model.addAttribute("currentPage", pageNumber);
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("hasPrevious", logPage.hasPrevious());
        model.addAttribute("hasNext", logPage.hasNext());
        model.addAttribute("search", search);
        model.addAttribute("sort", sortField);
        model.addAttribute("direction", sortDirection.name());
        model.addAttribute("filterType", filterType);
        model.addAttribute("pageTitle", "FitSquad Hub - Community Workout Logs");
        return "workout-logs";
    }
}
