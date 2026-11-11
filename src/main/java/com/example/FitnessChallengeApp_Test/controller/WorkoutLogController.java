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
 * Handles workout form rendering, @ManyToOne Challenge binding,
 * Bean Validation, custom queries, sorting, and pagination.
 */
@Controller
public class WorkoutLogController {

    private final WorkoutLogService workoutLogService;
    private final ChallengeService challengeService;

    public WorkoutLogController(
            WorkoutLogService workoutLogService,
            ChallengeService challengeService) {
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
            Model model) {

        Challenge selectedChallenge = null;

        if (workoutLog.getChallenge() == null
                || workoutLog.getChallenge().getId() == null) {

            bindingResult.rejectValue(
                    "challenge.id",
                    "error.challenge",
                    "Please select an active challenge");

        } else {
            selectedChallenge = challengeService.getChallengeById(
                    workoutLog.getChallenge().getId());

            if (selectedChallenge == null) {
                bindingResult.rejectValue(
                        "challenge.id",
                        "error.challenge",
                        "Selected challenge does not exist");
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

    // Display workout logs with search, sorting, filtering, and pagination
    @GetMapping("/workout-logs")
    public String showWorkoutLogs(
            Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "DESC") String direction,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "all") String filterType) {

        // Validate sorting field
        String[] allowedSortFields = {
                "id",
                "athleteName",
                "workoutDate",
                "durationMinutes",
                "caloriesBurned",
                "intensityLevel"
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

        // Validate sorting direction
        if (!direction.equalsIgnoreCase("ASC")
                && !direction.equalsIgnoreCase("DESC")) {
            direction = "DESC";
        }

        // Validate pagination parameters
        page = Math.max(page, 0);
        size = Math.max(size, 1);

        // Create sorting and pagination objects
        Sort.Direction sortDirection =
                Sort.Direction.fromString(direction);

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(sortDirection, sort));

        // Apply search and filter options
        Page<WorkoutLog> logPage;

        if (search == null) {
            search = "";
        }

        switch (filterType) {

            case "athlete":
                if (!search.trim().isEmpty()) {
                    logPage = workoutLogService.searchByAthleteName(
                            search.trim(), pageable);
                } else {
                    logPage = workoutLogService.getLogs(pageable);
                }
                break;

            case "calories":
                int minCalories = 200;

                try {
                    minCalories = Integer.parseInt(search.trim());
                } catch (NumberFormatException e) {
                    minCalories = 200;
                }

                logPage = workoutLogService.filterByMinCalories(
                        minCalories, pageable);
                break;

            case "highestCalories":
                logPage = workoutLogService.getHighestCalorieLogs(pageable);
                break;

            case "intensityDuration":
                if (!search.trim().isEmpty()) {
                    logPage = workoutLogService.filterByIntensityAndMinDuration(
                            search.trim(), 25, pageable);
                } else {
                    logPage = workoutLogService.getLogs(pageable);
                }
                break;

            default:
                filterType = "all";
                logPage = workoutLogService.getLogs(pageable);
                break;
        }

        // Populate model attributes
        model.addAttribute("workoutLogs", logPage.getContent());
        model.addAttribute("totalPages", logPage.getTotalPages());
        model.addAttribute("totalElements", logPage.getTotalElements());
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);

        model.addAttribute("hasPrevious", logPage.hasPrevious());
        model.addAttribute("hasNext", logPage.hasNext());

        model.addAttribute("search", search);
        model.addAttribute("sort", sort);
        model.addAttribute("direction", direction);
        model.addAttribute("filterType", filterType);

        model.addAttribute(
                "pageTitle",
                "FitSquad Hub - Community Workout Logs");

        return "workout-logs";
    }
}
