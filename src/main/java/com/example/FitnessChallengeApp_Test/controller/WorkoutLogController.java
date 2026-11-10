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
 * Lab 4: Custom Queries, Sorting, Filtering and Pagination.
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

    // ==========================================
    // DISPLAY WORKOUT FORM
    // ==========================================

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

    // ==========================================
    // PROCESS WORKOUT FORM
    // ==========================================

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

            model.addAttribute(
                    "challenges",
                    challengeService.getAllChallenges());

            model.addAttribute(
                    "intensityLevels",
                    workoutLogService.getIntensityLevels());

            model.addAttribute(
                    "pageTitle",
                    "FitSquad Hub - Log Workout Session");

            return "log-workout";
        }

        workoutLogService.addLog(workoutLog, selectedChallenge);

        return "redirect:/workout-logs";
    }

    // ==========================================
    // LAB 4 - SEARCH, SORT AND PAGINATION
    // ==========================================

    @GetMapping("/workout-logs")
    public String showWorkoutLogs(

            @RequestParam(defaultValue = "0") int page,

            @RequestParam(defaultValue = "5") int size,

            @RequestParam(defaultValue = "id") String sort,

            @RequestParam(defaultValue = "DESC") String direction,

            @RequestParam(required = false) String search,

            @RequestParam(defaultValue = "all") String filterType,

            Model model) {

        // Validate pagination parameters
        page = Math.max(page, 0);
        size = Math.max(size, 1);

        // Validate sorting field
        Set<String> allowedSortFields = Set.of(
                "id",
                "athleteName",
                "workoutDate",
                "durationMinutes",
                "caloriesBurned",
                "intensityLevel"
        );

        if (!allowedSortFields.contains(sort)) {
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
                page,
                size,
                Sort.by(sortDirection, sort)
        );

        // Store workout log results
        Page<WorkoutLog> logPage;

        String searchText = search == null ? "" : search.trim();

        // Apply search filters
        switch (filterType) {

            case "athlete":

                logPage = workoutLogService.getLogsByAthlete(
                        searchText,
                        pageable
                );

                break;

            case "calories":

                int minCalories;

                try {
                    minCalories = Integer.parseInt(searchText);
                } catch (NumberFormatException e) {
                    minCalories = 200;
                }

                logPage = workoutLogService.getLogsByMinCalories(
                        minCalories,
                        pageable
                );

                break;

            case "highestCalories":

                logPage = workoutLogService.getHighestCalorieLogs(
                        pageable
                );

                break;

            case "intensityDuration":

                logPage = workoutLogService.getLogsByIntensityAndDuration(
                        searchText,
                        25,
                        pageable
                );

                break;

            default:

                filterType = "all";

                logPage = workoutLogService.getLogs(
                        pageable
                );

                break;
        }

        // Add workout logs to Thymeleaf model
        model.addAttribute(
                "workoutLogs",
                logPage.getContent()
        );

        // Pagination details
        model.addAttribute(
                "totalPages",
                logPage.getTotalPages()
        );

        model.addAttribute(
                "totalElements",
                logPage.getTotalElements()
        );

        model.addAttribute(
                "currentPage",
                logPage.getNumber()
        );

        model.addAttribute(
                "pageSize",
                size
        );

        model.addAttribute(
                "hasPrevious",
                logPage.hasPrevious()
        );

        model.addAttribute(
                "hasNext",
                logPage.hasNext()
        );

        // Search and sorting details
        model.addAttribute("search", searchText);
        model.addAttribute("sort", sort);
        model.addAttribute("direction", direction);
        model.addAttribute("filterType", filterType);

        model.addAttribute(
                "pageTitle",
                "FitSquad Hub - Community Workout Logs"
        );

        return "workout-logs";
    }
}