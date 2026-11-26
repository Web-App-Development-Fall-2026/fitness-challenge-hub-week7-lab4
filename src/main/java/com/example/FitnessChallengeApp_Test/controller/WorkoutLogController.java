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
 * and listing logged workouts with pagination, sorting, and filtering.
 */
@Controller
public class WorkoutLogController {

    private static final Set<String> SORT_FIELDS = Set.of(
            "id", "athleteName", "workoutDate", "durationMinutes", "caloriesBurned", "intensityLevel"
    );
    private static final Set<String> FILTER_TYPES = Set.of("athlete", "calories", "highestCalories", "intensityDuration");

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
        String searchTerm = search == null ? "" : search.trim();
        String selectedFilter = FILTER_TYPES.contains(filterType) ? filterType : "all";
        String sortField = SORT_FIELDS.contains(sort) ? sort : "id";
        Sort.Direction sortDirection = "ASC".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort ordering = Sort.by(sortDirection, sortField);
        if (!"id".equals(sortField)) {
            ordering = ordering.and(Sort.by(Sort.Direction.DESC, "id"));
        }

        Pageable pageable = PageRequest.of(Math.max(0, page), size > 0 ? size : 5, ordering);
        Page<WorkoutLog> logPage = findLogs(selectedFilter, searchTerm, pageable);
        if (pageable.getPageNumber() >= logPage.getTotalPages() && pageable.getPageNumber() > 0) {
            pageable = pageable.withPage(Math.max(0, logPage.getTotalPages() - 1));
            logPage = findLogs(selectedFilter, searchTerm, pageable);
        }

        model.addAttribute("workoutLogs", logPage.getContent());
        model.addAttribute("totalPages", logPage.getTotalPages());
        model.addAttribute("totalElements", logPage.getTotalElements());
        model.addAttribute("currentPage", logPage.getNumber());
        model.addAttribute("pageSize", logPage.getSize());
        model.addAttribute("hasPrevious", logPage.hasPrevious());
        model.addAttribute("hasNext", logPage.hasNext());
        model.addAttribute("search", searchTerm);
        model.addAttribute("sort", sortField);
        model.addAttribute("direction", sortDirection.name());
        model.addAttribute("filterType", selectedFilter);
        model.addAttribute("pageTitle", "FitSquad Hub - Community Workout Logs");
        return "workout-logs";
    }

    private Page<WorkoutLog> findLogs(String filterType, String search, Pageable pageable) {
        return switch (filterType) {
            case "athlete" -> workoutLogService.findByAthleteNameContainingIgnoreCase(search, pageable);
            case "calories" -> workoutLogService.findByCaloriesBurnedGreaterThanEqual(minCalories(search), pageable);
            case "highestCalories" -> workoutLogService.findHighestCalorieLogs(pageable);
            case "intensityDuration" -> workoutLogService.findLogsByIntensityAndMinDuration(search, 25, pageable);
            default -> workoutLogService.getLogs(pageable);
        };
    }

    private int minCalories(String search) {
        try {
            return Integer.parseInt(search);
        } catch (NumberFormatException ignored) {
            return 200;
        }
    }
}
