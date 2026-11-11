# Lab 4 - Advanced Spring Data JPA (Custom Queries, Relationships & Dynamic Pagination)

## Course Information
- **Course:** CPAN-228 Web Application Development
- **Topic:** JPA Entities, `@ManyToOne` Relationships, Custom Repository Queries (Derived & `@Query` JPQL), Dynamic Sorting, and Pagination with Spring Data JPA
- **Application Theme:** FitSquad Hub (Fitness Challenge & Athlete Workout Platform)

---

## Overview & Classroom Context

FitSquad Hub has been upgraded from an in-memory collection architecture to a full **Spring Data JPA + H2 Relational Database** system featuring three interconnected entities:

1. **`Coach` ([`Coach.java`](file:///src/main/java/com/example/FitnessChallengeApp_Test/model/Coach.java)):** Certified fitness coaches stored in the `COACHES` table and managed via `/coaches` and `/create-coach`.
2. **`Challenge` ([`Challenge.java`](file:///src/main/java/com/example/FitnessChallengeApp_Test/model/Challenge.java)):** Fitness challenges stored in the `CHALLENGES` table with a `@ManyToOne` relationship to `Coach` (`coach_id`).
3. **`WorkoutLog` ([`WorkoutLog.java`](file:///src/main/java/com/example/FitnessChallengeApp_Test/model/WorkoutLog.java)):** Athlete workout sessions stored in the `WORKOUT_LOGS` table with a `@ManyToOne` relationship to `Challenge` (`challenge_id`), complete with Jakarta Bean Validation and a reusable footer fragment ([`footer.html`](file:///src/main/resources/templates/fragments/footer.html)).

---

## Reference Examples in the Codebase

As part of the in-class lecture demonstration, the **`Challenge`** subsystem has been completely configured to support custom derived queries, JPQL `@Query` methods, dynamic sorting, and pagination:

- **Custom JPA Repository Queries:** Inspect [`ChallengeRepository.java`](file:///src/main/java/com/example/FitnessChallengeApp_Test/repository/ChallengeRepository.java) for examples of derived query methods (`findByTitleContainingIgnoreCase`, `findByRewardPointsGreaterThanEqual`, `findByDifficultyIgnoreCase`) and custom `@Query` JPQL queries (`findTopRewardChallenges`, `findEliteChallengesByCategory`).
- **Service & Pagination Controller Logic:** Inspect [`ChallengeService.java`](file:///src/main/java/com/example/FitnessChallengeApp_Test/service/ChallengeService.java) and [`WorkoutChallengeController.java`](file:///src/main/java/com/example/FitnessChallengeApp_Test/controller/WorkoutChallengeController.java) to see how `PageRequest`, `Sort`, `Pageable`, and `filterType` routing work together.
- **Thymeleaf Search, Sort & Pagination Controls:** Inspect [`challenges.html`](file:///src/main/resources/templates/challenges.html) for the search/filter bar, interactive sort buttons with `↑`/`↓` arrows, and Bootstrap pagination links that preserve query parameters across pages.
- **JPA Cheat Sheet:**
  - [`DATA_JPA_CHEAT_SHEET.md`](file:///DATA_JPA_CHEAT_SHEET.md) — Quick syntax reference for JPA annotations, `@ManyToOne`, derived queries, JPQL, and `Pageable`.

---

## Getting Started (GitHub Setup)

1. **Fork the Repository** on GitHub to your personal account.
2. **Clone Your Fork** locally:
   ```bash
   git clone https://github.com/YOUR-USERNAME/fitness-challenge-hub-week7-lab4.git
   cd fitness-challenge-hub-week7-lab4
   ```
3. **Add Upstream Remote:**
   ```bash
   git remote add upstream https://github.com/Web-App-Development-Fall-2026/fitness-challenge-hub-week7-lab4.git
   ```
4. **Create a Feature Branch** for Lab 4:
   ```bash
   git checkout -b feature/lab4-yourname
   ```
   *(Replace `yourname` with your actual name, e.g., `feature/lab4-john-doe`)*

---

## Your Lab Assignment (WorkoutLog Custom Queries, Sorting & Pagination)

Currently, the **Athlete Workout Logging** subsystem (`WorkoutLogRepository`, `WorkoutLogService`, `WorkoutLogController`, and `workout-logs.html`) uses basic `findAll()` CRUD without filtering, sorting, or pagination.

In this lab, you will upgrade the **WorkoutLog** subsystem to support **Derived Queries**, **JPQL `@Query` methods**, **Multi-Criteria Sorting**, and **Pagination**, using the `Challenge` implementation as your blueprint!

---

### Part 1 — Add Custom Query Methods to `WorkoutLogRepository`

Open [`WorkoutLogRepository.java`](file:///src/main/java/com/example/FitnessChallengeApp_Test/repository/WorkoutLogRepository.java) and add the following four custom methods:

#### Derived Query Methods (Spring Data method naming convention):
1. **`Page<WorkoutLog> findByAthleteNameContainingIgnoreCase(String athleteName, Pageable pageable)`**
   - Returns a paginated list of workout logs whose `athleteName` contains the search keyword (case-insensitive).
2. **`Page<WorkoutLog> findByCaloriesBurnedGreaterThanEqual(int minCalories, Pageable pageable)`**
   - Returns a paginated list of workout logs where `caloriesBurned` is greater than or equal to `minCalories`.

#### Custom `@Query` Methods (JPQL):
3. **`Page<WorkoutLog> findHighestCalorieLogs(Pageable pageable)`**
   - Annotate with `@Query("SELECT w FROM WorkoutLog w ORDER BY w.caloriesBurned DESC")`.
   - Returns workout logs ranked by highest calorie burn descending.
4. **`Page<WorkoutLog> findLogsByIntensityAndMinDuration(String intensity, int minDuration, Pageable pageable)`**
   - Annotate with `@Query("SELECT w FROM WorkoutLog w WHERE LOWER(w.intensityLevel) = LOWER(?1) AND w.durationMinutes >= ?2 ORDER BY w.workoutDate DESC")`.
   - Finds workout logs matching the specified intensity level (case-insensitive) and lasting at least `minDuration` minutes, ordered by workout date descending.

---

### Part 2 — Expose Paginated Search Methods in `WorkoutLogService` & Refactor `WorkoutLogController`

1. In [`WorkoutLogService.java`](file:///src/main/java/com/example/FitnessChallengeApp_Test/service/WorkoutLogService.java), add wrapper methods delegating to your new `WorkoutLogRepository` methods (plus `getLogs(Pageable pageable)` calling `workoutLogRepository.findAll(pageable)`).
2. In [`WorkoutLogController.java`](file:///src/main/java/com/example/FitnessChallengeApp_Test/controller/WorkoutLogController.java), update `GET /workout-logs` (`showWorkoutLogs`) to accept:
   - **Pagination Parameters:** `@RequestParam(defaultValue = "0") int page` and `@RequestParam(defaultValue = "5") int size`.
   - **Sorting Parameters:** `@RequestParam(defaultValue = "id") String sort` and `@RequestParam(defaultValue = "DESC") String direction`.
     - Support sorting by: `id`, `athleteName`, `workoutDate`, `durationMinutes`, `caloriesBurned`, `intensityLevel`.
   - **Search & Filter Parameters:**
     - `@RequestParam(required = false) String search`
     - `@RequestParam(defaultValue = "all") String filterType`:
       - `"athlete"` $\rightarrow$ calls `findByAthleteNameContainingIgnoreCase(search.trim(), pageable)`
       - `"calories"` $\rightarrow$ parses `search` as an `int` (defaulting to `200` on `NumberFormatException`) and calls `findByCaloriesBurnedGreaterThanEqual(minCalories, pageable)`
       - `"highestCalories"` $\rightarrow$ calls `findHighestCalorieLogs(pageable)`
       - `"intensityDuration"` $\rightarrow$ calls `findLogsByIntensityAndMinDuration(search.trim(), 25, pageable)` (using `25` minutes as the minimum duration threshold)
       - Default (`"all"`) $\rightarrow$ calls `findAll(pageable)`
3. Populate the Thymeleaf `Model` with:
   - `workoutLogs` (`logPage.getContent()`)
   - `totalPages`, `totalElements`, `currentPage`, `pageSize`
   - `hasPrevious`, `hasNext`
   - `search`, `sort`, `direction`, `filterType`

---

### Part 3 — Update `workout-logs.html` with Search, Sort & Pagination Controls

Open [`workout-logs.html`](file:///src/main/resources/templates/workout-logs.html) and enhance it using [`challenges.html`](file:///src/main/resources/templates/challenges.html) as your reference:

1. **Search / Filter Form at the Top:**
   - Add a `GET` form targeting `th:action="@{/workout-logs}"`.
   - Include hidden inputs for `sort`, `direction`, and `size`.
   - Add a text input for `search` (`th:value="${search}"`).
   - Add a `<select name="filterType">` dropdown with options for:
     - `all` (*All Workout Logs*)
     - `athlete` (*By Athlete Name*)
     - `calories` (*By Min Calories Burned*)
     - `highestCalories` (*Highest Calorie Burn - JPQL*)
     - `intensityDuration` (*By Intensity & Min 25m Duration - JPQL*)
   - Include **Search** and **Reset** buttons.
2. **Sortable Table Column Headers:**
   - Turn the table headers for **Athlete** (`athleteName`), **Date** (`workoutDate`), **Duration** (`durationMinutes`), **Calories** (`caloriesBurned`), and **Intensity** (`intensityLevel`) into clickable `<a>` links that toggle `direction` between `'ASC'` and `'DESC'` and display `↑` / `↓` indicator arrows.
3. **Pagination Navigation Footer:**
   - Display `"Page X of Y (Z total workout logs)"`.
   - Render Bootstrap pagination buttons (**First**, **Previous**, **Page Numbers**, **Next**, **Last**) that preserve `size`, `sort`, `direction`, `search`, and `filterType`.

---

## Running & Testing the Application

### Run Locally
```bash
./mvnw spring-boot:run
```
*(On Windows: `.\mvnw.cmd spring-boot:run`)*

- **Challenges Catalog (Paginated Reference):** [http://localhost:8080/challenges](http://localhost:8080/challenges)
- **Create Challenge Form:** [http://localhost:8080/create-challenge](http://localhost:8080/create-challenge)
- **Workout Logs Dashboard (Lab Target):** [http://localhost:8080/workout-logs](http://localhost:8080/workout-logs)
- **Log Workout Form:** [http://localhost:8080/log-workout](http://localhost:8080/log-workout)
- **Certified Coaches Roster:** [http://localhost:8080/coaches](http://localhost:8080/coaches)
- **H2 Database Console:** [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
  - **JDBC URL:** `jdbc:h2:mem:fitsquadhub`
  - **User Name:** `sa`
  - **Password:** *(leave blank)*

### Run Automated Tests
```bash
./mvnw test
```
*(On Windows: `.\mvnw.cmd test`)*
