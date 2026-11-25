    public String processLogWorkout(@Valid @ModelAttribute("workoutLog") WorkoutLog log, BindingResult result, Model model) {
        Challenge selected = null;
        if (log.getChallenge() == null || log.getChallenge().getId() == null) {
            result.rejectValue("challenge.id", "error.challenge", "Please select an active challenge");
        } else {
            selected = challengeService.getChallengeById(log.getChallenge().getId());
            if (selected == null) result.rejectValue("challenge.id", "error.challenge", "Selected challenge does not exist");
        }
        if (result.hasErrors()) { populateForm(model); return "log-workout"; }
        workoutLogService.addLog(log, selected);
        return "redirect:/workout-logs";
    }
    @GetMapping("/workout-logs")
    public String showWorkoutLogs(@RequestParam(defaultValue="0") int page,
            @RequestParam(defaultValue="5") int size,
            @RequestParam(defaultValue="id") String sort,
            @RequestParam(defaultValue="DESC") String direction,
            @RequestParam(required=false) String search,
            @RequestParam(defaultValue="all") String filterType, Model model) {
        page = Math.max(0, page);
        size = size > 0 ? size : 5;
        if (!Set.of("id","athleteName","workoutDate","durationMinutes","caloriesBurned","intensityLevel").contains(sort)) sort = "id";
        direction = "ASC".equalsIgnoreCase(direction) ? "ASC" : "DESC";
        if (!Set.of("all","athlete","calories","highestCalories","intensityDuration").contains(filterType)) filterType = "all";
        search = search == null ? "" : search.trim();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sort));
        Page<WorkoutLog> logPage;
        switch (filterType) {
            case "athlete": logPage = workoutLogService.findByAthleteNameContainingIgnoreCase(search, pageable); break;
            case "calories":
                int minCalories;
                try { minCalories = Integer.parseInt(search); } catch (NumberFormatException e) { minCalories = 200; }
                logPage = workoutLogService.findByCaloriesBurnedGreaterThanEqual(minCalories, pageable); break;
            case "highestCalories": logPage = workoutLogService.findHighestCalorieLogs(pageable); break;
            case "intensityDuration": logPage = workoutLogService.findLogsByIntensityAndMinDuration(search, 25, pageable); break;
            default: logPage = workoutLogService.getLogs(pageable);
        }
        model.addAttribute("workoutLogs", logPage.getContent());
        model.addAttribute("totalPages", logPage.getTotalPages());
        model.addAttribute("totalElements", logPage.getTotalElements());
        model.addAttribute("currentPage", logPage.getNumber());
        model.addAttribute("pageSize", size);
        model.addAttribute("hasPrevious", logPage.hasPrevious());
        model.addAttribute("hasNext", logPage.hasNext());
        model.addAttribute("search", search);
        model.addAttribute("sort", sort);
        model.addAttribute("direction", direction);
        model.addAttribute("filterType", filterType);
        model.addAttribute("pageTitle", "FitSquad Hub - Community Workout Logs");
        return "workout-logs";
    }
}
