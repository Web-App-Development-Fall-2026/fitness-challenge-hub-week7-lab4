
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

import java.util.List;

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

    // Display the workout logging form
    @GetMapping("/log-workout")
    public String showLogWorkoutForm(Model model) {
        WorkoutLog workoutLog = new WorkoutLog();
        workoutLog.setChallenge(new Challenge());

        model.addAttribute("workoutLog", workoutLog);
        model.addAttribute("challenges", challengeService.getAllChallenges());
        model.addAttribute("intensityLevels",
                workoutLogService.getIntensityLevels());
        model.addAttribute("pageTitle",
                "FitSquad Hub - Log Workout Session");

        return "log-workout";
    }

    // Save a workout log after checking the form
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
            model.addAttribute("challenges",
                    challengeService.getAllChallenges());
            model.addAttribute("intensityLevels",
                    workoutLogService.getIntensityLevels());
            model.addAttribute("pageTitle",
                    "FitSquad Hub - Log Workout Session");

            return "log-workout";
        }

        workoutLogService.addLog(workoutLog, selectedChallenge);
        return "redirect:/workout-logs";
    }

    // Display workout logs with searching, sorting and pagination
    @GetMapping("/workout-logs")
    public String showWorkoutLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "DESC") String direction,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "all") String filterType,
            Model model) {

        // Make sure the page and size are valid
        page = Math.max(page, 0);
        size = Math.max(size, 1);

        // Allow sorting only by supported fields
        List<String> allowedSortFields = List.of(
                "id", "athleteName", "workoutDate",
                "durationMinutes", "caloriesBurned",
                "intensityLevel");

        if (!allowedSortFields.contains(sort)) {
            sort = "id";
        }

        // Set ascending or descending order
        Sort.Direction sortDirection =
                "ASC".equalsIgnoreCase(direction)
                        ? Sort.Direction.ASC
                        : Sort.Direction.DESC;

        direction = sortDirection.name();

        Pageable pageable = PageRequest.of(
                page, size, Sort.by(sortDirection, sort));

        Page<WorkoutLog> logPage;

        String searchText = search == null ? "" : search.trim();

        // Choose the search or filter option
        switch (filterType) {

            case "athlete":
                logPage = workoutLogService
                        .findByAthleteNameContainingIgnoreCase(
                                searchText, pageable);
                break;

            case "calories":
                int minCalories;
                try {
                    minCalories = Integer.parseInt(searchText);
                } catch (NumberFormatException e) {
                    minCalories = 200;
                }

                logPage = workoutLogService
                        .findByCaloriesBurnedGreaterThanEqual(
                                minCalories, pageable);
                break;

            case "highestCalories":
                logPage = workoutLogService
                        .findHighestCalorieLogs(pageable);
                break;

            case "intensityDuration":
                logPage = workoutLogService
                        .findLogsByIntensityAndMinDuration(
                                searchText, 25, pageable);
                break;

            default:
                filterType = "all";
                logPage = workoutLogService.getLogs(pageable);
                break;
        }

        // Send the results to the HTML page
        model.addAttribute("workoutLogs", logPage.getContent());
        model.addAttribute("totalPages", logPage.getTotalPages());
        model.addAttribute("totalElements", logPage.getTotalElements());
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("hasPrevious", logPage.hasPrevious());
        model.addAttribute("hasNext", logPage.hasNext());

        model.addAttribute("search", searchText);
        model.addAttribute("sort", sort);
        model.addAttribute("direction", direction);
        model.addAttribute("filterType", filterType);

        model.addAttribute("pageTitle",
                "FitSquad Hub - Community Workout Logs");

        return "workout-logs";
    }
}
