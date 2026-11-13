
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

/**
 * Controller for Athlete Workout Logging.
 * Handles workout form rendering, @ManyToOne Challenge binding, Bean Validation,
 * and listing logged workouts.
 * Supports pagination, sorting, and custom repository queries.
 */
@Controller
public class WorkoutLogController {

    private final WorkoutLogService workoutLogService;
    private final ChallengeService challengeService;

    public WorkoutLogController(WorkoutLogService workoutLogService, ChallengeService challengeService) {
        this.workoutLogService = workoutLogService;
        this.challengeService = challengeService;
    }

    // =========================================================================
    // 1. Display Workout Logging Form
    // =========================================================================

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

    // =========================================================================
    // 2. Process Workout Logging Form
    // =========================================================================

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

    // =========================================================================
    // 3. Display Workout Logs with Pagination, Sorting & Filtering
    // =========================================================================

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

        // Validate sorting column
        String[] allowedSortFields = {
                "id", "athleteName", "workoutDate",
                "durationMinutes", "caloriesBurned", "intensityLevel"
        };

        boolean validSort = false;

        for (String field : allowedSortFields) {
            if (field.equals(sort)) {
                validSort = true;
                break;
            }
        }

        if (!validSort) {
            sort = "id";
        }

        // Set sorting direction
        Sort.Direction sortDirection =
                direction.equalsIgnoreCase("ASC")
                        ? Sort.Direction.ASC
                        : Sort.Direction.DESC;

        direction = sortDirection.name();

        // Create Pageable object
        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.max(size, 1),
                Sort.by(sortDirection, sort)
        );

        // Prepare search keyword
        search = (search == null) ? "" : search.trim();

        // =========================================================================
        // 4. Apply Search & Filter Options
        // =========================================================================

        Page<WorkoutLog> logPage;

        switch (filterType) {

            case "athlete":
                logPage = workoutLogService.findByAthleteNameContainingIgnoreCase(
                        search, pageable);
                break;

            case "calories":
                int minCalories;
                try {
                    minCalories = Integer.parseInt(search);
                } catch (NumberFormatException e) {
                    minCalories = 200;
                }

                logPage = workoutLogService.findByCaloriesBurnedGreaterThanEqual(
                        minCalories, pageable);
                break;

            case "highestCalories":
                logPage = workoutLogService.findHighestCalorieLogs(pageable);
                break;

            case "intensityDuration":
                logPage = workoutLogService.findLogsByIntensityAndMinDuration(
                        search, 25, pageable);
                break;

            default:
                filterType = "all";
                logPage = workoutLogService.getLogs(pageable);
                break;
        }

        // =========================================================================
        // 5. Add Pagination & Search Data to Thymeleaf Model
        // =========================================================================

        model.addAttribute("workoutLogs", logPage.getContent());

        model.addAttribute("totalPages", logPage.getTotalPages());
        model.addAttribute("totalElements", logPage.getTotalElements());
        model.addAttribute("currentPage", logPage.getNumber());
        model.addAttribute("pageSize", size);

        model.addAttribute("hasPrevious", logPage.hasPrevious());
        model.addAttribute("hasNext", logPage.hasNext());

        // Preserve search and sorting selections
        model.addAttribute("search", search);
        model.addAttribute("sort", sort);
        model.addAttribute("direction", direction);
        model.addAttribute("filterType", filterType);

        model.addAttribute("pageTitle", "FitSquad Hub - Community Workout Logs");

        return "workout-logs";
    }
}
