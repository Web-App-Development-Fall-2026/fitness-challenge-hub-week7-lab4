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
 * and listing logged workouts. Students will refactor displayWorkoutLogs() in the Lab
 * to support pagination, sorting, and custom repository queries.
 */
@Controller
public class WorkoutLogController {

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
            Model model) {

        Sort.Direction dir = direction.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, sort));
        Page<WorkoutLog> workoutLogsPage;

        switch (filterType.toLowerCase()) {
            case "athlete":
                String athleteSearch = (search != null) ? search.trim() : "";
                workoutLogsPage = workoutLogService.getLogsByAthleteName(athleteSearch, pageable);
                break;

            case "calories":
                int minCalories = 200;
                if (search != null && !search.trim().isEmpty()) {
                    try {
                        minCalories = Integer.parseInt(search.trim());
                    } catch (NumberFormatException e) {
                        minCalories = 200;
                    }
                }
                workoutLogsPage = workoutLogService.getLogsByMinCalories(minCalories, pageable);
                break;

            case "highestcalories":
                workoutLogsPage = workoutLogService.getHighestCalorieLog(pageable);
                break;

            case "intensityduration":
                String intensity = (search != null) ? search.trim() : "";
                workoutLogsPage = workoutLogService.getLogsByIntensityAndDuration(intensity, 25, pageable);
                break;

            case "all":
            default:
                workoutLogsPage = workoutLogService.getLogs(pageable);
                break;
        }

        model.addAttribute("workoutLogs", workoutLogService.getAllLogs());
        model.addAttribute("pageTitle", "FitSquad Hub - Community Workout Logs");
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("sort", sort);
        model.addAttribute("direction", direction);
        model.addAttribute("search", search);
        model.addAttribute("filterType", filterType);

        return "workout-logs";
    }
}

