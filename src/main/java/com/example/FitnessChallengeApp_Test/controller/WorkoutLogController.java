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

    @GetMapping("/workout-logs")
    public String showWorkoutLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "DESC") String direction,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "all") String filterType,
            Model model) {

        Set<String> allowedSorts = Set.of(
                "id",
                "athleteName",
                "workoutDate",
                "durationMinutes",
                "caloriesBurned",
                "intensityLevel");

        if (!allowedSorts.contains(sort)) {
            sort = "id";
        }

        page = Math.max(0, page);
        size = Math.max(1, Math.min(size, 100));

        direction = "ASC".equalsIgnoreCase(direction)
                ? "ASC" : "DESC";

        String keyword = search == null ? "" : search.trim();

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.fromString(direction), sort));

        Page<WorkoutLog> logPage;

        switch (filterType) {

            case "athlete":
                logPage = workoutLogService
                        .findByAthleteNameContainingIgnoreCase(
                                keyword, pageable);
                break;

            case "calories":
                int minCalories;

                try {
                    minCalories = Integer.parseInt(keyword);
                } catch (NumberFormatException e) {
                    minCalories = 200;
                }

                logPage = workoutLogService
                        .findByCaloriesBurnedGreaterThanEqual(
                                minCalories, pageable);
                break;

            case "highestCalories":
                logPage = workoutLogService
                        .findHighestCalorieLogs(
                                PageRequest.of(page, size));
                break;

            case "intensityDuration":
                logPage = workoutLogService
                        .findLogsByIntensityAndMinDuration(
                                keyword, 25,
                                PageRequest.of(page, size));
                break;

            default:
                filterType = "all";
                logPage = workoutLogService.getLogs(pageable);
                break;
        }

        model.addAttribute("workoutLogs", logPage.getContent());
        model.addAttribute("totalPages", logPage.getTotalPages());
        model.addAttribute("totalElements", logPage.getTotalElements());
        model.addAttribute("currentPage", logPage.getNumber());
        model.addAttribute("pageSize", size);
        model.addAttribute("hasPrevious", logPage.hasPrevious());
        model.addAttribute("hasNext", logPage.hasNext());

        model.addAttribute("search", keyword);
        model.addAttribute("sort", sort);
        model.addAttribute("direction", direction);
        model.addAttribute("filterType", filterType);

        model.addAttribute("pageTitle",
                "FitSquad Hub - Community Workout Logs");

        return "workout-logs";
    }
}
