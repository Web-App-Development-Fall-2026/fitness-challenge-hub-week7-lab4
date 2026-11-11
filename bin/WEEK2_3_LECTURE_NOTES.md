# CPAN-228: Web Application Development — Week 2 Lecture Notes & Slide Guide

**Course:** CPAN-228 Web Application Development  
**Topic:** Diving Deeper into Spring Web: Form Processing, Model Binding & Jakarta Bean Validation  
**Platform Theme:** FitSquad Hub (Fitness Challenge Platform)  
**Instructor Guide:** Prof. Anton Kovunov / Instructional Team  

---

## Slide-by-Slide Modernized Curriculum Outline

### Slide 1: Title & Overview
- **Title:** Diving Deeper into Spring Web: Presenting Model Data & Processing Form Input
- **Course:** CPAN-228 Web Application Development for CPAN Diploma
- **Overview:**
  - Moving beyond read-only catalog views (`GET` requests).
  - Collecting user input via interactive HTML forms.
  - Two-way model binding with Thymeleaf (`th:object` and `th:field`).
  - Handling HTTP `POST` requests in Spring Controllers.
  - Preventing duplicate submissions via the **Post-Redirect-Get (PRG)** design pattern.
  - Robust server-side validation using **Jakarta Bean Validation** (`@Valid`, `BindingResult`).

---

### Slide 2: Domain Models & Lombok
- **Concept:** Creating rich domain models without boilerplate getters, setters, and constructors.
- **Modernized Code Example (`Challenge.java`):**
```java
package com.example.FitnessChallengeApp_Test.model;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Challenge {
    private Long id;

    @NotBlank(message = "Challenge title is required")
    @Size(min = 3, max = 60, message = "Title must be between 3 and 60 characters")
    private String title;

    @NotBlank(message = "Please select a category")
    private String category;

    @NotBlank(message = "Description cannot be empty")
    @Size(min = 10, max = 500, message = "Description must be between 10 and 500 characters")
    private String description;

    @NotBlank(message = "Target goal is required")
    private String targetGoal;

    @Min(value = 1, message = "Duration must be at least 1 day")
    @Max(value = 365, message = "Duration cannot exceed 365 days")
    private int durationDays;

    @Min(value = 50, message = "Reward points must be at least 50 XP")
    @Max(value = 5000, message = "Reward points cannot exceed 5,000 XP")
    private int rewardPoints;

    @NotBlank(message = "Please select a difficulty level")
    private String difficulty;

    private String imageUrl;
    private boolean active = true;
}
```
- **Takeaway:** `@Data` generates `@Getter`, `@Setter`, `@ToString`, `@EqualsAndHashCode`, and required constructor behind the scenes.

---

### Slide 3: Lombok & Maven Configuration
- **Concept:** How Lombok works at compile-time and why it requires annotation processor configuration in `pom.xml`.
- **Explanation:**
  - Lombok is an AST (Abstract Syntax Tree) transformation tool that generates bytecode during compilation.
  - It does not introduce heavy runtime overhead because it acts as compile-time syntactic sugar.
  - To prevent compilation issues in modern Java (Java 17+ / 21+), `pom.xml` must configure the `maven-compiler-plugin` with `annotationProcessorPaths` for Lombok.
- **Starter Dependency:**
```xml
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

---

### Slide 4: Architectural Foundation — Spring `@Service` vs `@SessionAttributes`
- **Concept:** Where does application state live?
- **Comparison:**
  - *Previous Approach (`@SessionAttributes`)*: Stores objects in the individual user's HTTP session. Good for multi-step wizards, but hard to share data across users and creates hidden session lifecycle bugs.
  - *Recommended Approach (Spring `@Service`)*: A singleton `@Service` bean (e.g., `ChallengeService`) holding an in-memory repository (or database). Shared across all requests, cleanly testable, and follows true separation of concerns.
- **Code Example (`ChallengeService.java`):**
```java
@Service
public class ChallengeService {
    private final List<Challenge> challenges = new ArrayList<>();
    private final AtomicLong idGenerator = new AtomicLong(6);

    public List<Challenge> getAllChallenges() {
        return new ArrayList<>(challenges);
    }

    public void addChallenge(Challenge challenge) {
        challenge.setId(idGenerator.incrementAndGet());
        challenges.add(challenge);
    }
}
```

---

### Slide 5: Form Design & Model Preparation (`GET /create-challenge`)
- **Concept:** Before a browser can render a form bound to an object, the controller must supply an initial, empty backing bean to the model.
- **Controller Implementation:**
```java
@GetMapping("/create-challenge")
public String showCreateForm(Model model) {
    model.addAttribute("challenge", new Challenge());
    model.addAttribute("categories", challengeService.getCategories());
    model.addAttribute("difficulties", challengeService.getDifficulties());
    return "create-challenge";
}
```
- **Thymeleaf Binding Concepts:**
  - `th:object="${challenge}"`: Binds the `<form>` to the backing bean.
  - `th:field="*{fieldName}"`: Generates HTML `id`, `name`, and current `value` automatically.
  - Dynamic `<select>`:
```html
<select th:field="*{category}">
    <option value="">-- Choose Category --</option>
    <option th:each="cat : ${categories}" th:value="${cat}" th:text="${cat}"></option>
</select>
```

---

### Slide 6: Processing Form Submission (`POST /create-challenge`) & PRG Pattern
- **Concept:** Handling incoming form payloads and preventing form duplicate resubmission.
- **The Post-Redirect-Get (PRG) Pattern:**
  - Direct rendering on POST causes browser warnings ("Confirm Form Resubmission") if the user refreshes.
  - Solution: Process data -> Return redirect string (`"redirect:/challenges"`).
  - The browser performs a clean `GET /challenges`, safely loading the updated list.
- **Controller Implementation:**
```java
@PostMapping("/create-challenge")
public String processCreateChallenge(
        @Valid @ModelAttribute("challenge") Challenge challenge,
        BindingResult bindingResult,
        Model model
) {
    if (bindingResult.hasErrors()) {
        // Re-populate helper collections needed by the view
        model.addAttribute("categories", challengeService.getCategories());
        model.addAttribute("difficulties", challengeService.getDifficulties());
        return "create-challenge"; // Return form view with errors
    }

    challengeService.addChallenge(challenge);
    return "redirect:/challenges"; // PRG Pattern
}
```
> **CRITICAL RULE:** `BindingResult` **MUST immediately follow** the `@Valid` model attribute in the method parameter list, or Spring will throw an uncaught `BindException`.

---

### Slide 7: Client Feedback & Thymeleaf Validation Rendering
- **Concept:** Displaying meaningful, accessible validation error messages next to invalid inputs.
- **Key Thymeleaf Validation Utilities:**
  - `#fields.hasErrors('fieldName')`: Checks if a specific field has validation violations.
  - `th:errors="*{fieldName}"`: Outputs the error message specified in the domain annotation (`message = "..."`).
  - `th:classappend`: Conditionally adds Bootstrap's `is-invalid` class.
- **Form Snippet:**
```html
<div class="mb-3">
    <label for="title" class="form-label">Challenge Title</label>
    <input type="text"
           id="title"
           class="form-control"
           th:field="*{title}"
           th:classappend="${#fields.hasErrors('title')} ? 'is-invalid' : ''">
    <div class="invalid-feedback" th:if="${#fields.hasErrors('title')}" th:errors="*{title}">
        Title error message
    </div>
</div>
```

---

### Slide 8: Classroom Live-Demo vs Student Lab 2 Bridge
- **In-Class Demo (Instructor Live Coding):**
  1. Build the Challenge creation form (`GET /create-challenge`).
  2. Implement form submission and redirect (`POST /create-challenge`).
  3. Add Jakarta Bean Validation annotations to `Challenge.java`.
  4. Display validation errors inline on `create-challenge.html`.
  5. Introduce in-memory service architecture: create `ChallengeService` as a singleton `@Service` with thread-safe collection and ID generation.
  6. Refactor `WorkoutChallengeController` to delegate operations to `ChallengeService`.
  7. Modularize layout using Thymeleaf fragments (`navbar.html`).
- **Student Lab 2 Assignment:**
  - Students implement the **Athlete Workout Logging** feature end-to-end with in-memory persistence:
    - Domain Model: `WorkoutLog.java` with Jakarta Bean Validation constraints (`@NotBlank`, `@Size`, `@NotNull`, `@Min`, `@Max`).
    - Service Layer: `WorkoutLogService.java` managing business operations and in-memory list storage.
    - Controller Layer: `WorkoutLogController` (`GET /log-workout`, `POST /log-workout` with `@Valid` + PRG, `GET /workout-logs`).
    - Templates: `log-workout.html` and `workout-logs.html` displaying logged sessions with validation feedback.
    - Extra Work (Bonus): Extract and integrate a reusable footer fragment (`fragments/footer.html`) across all templates.

---

## Instructor Live-Coding Step-by-Step Walkthrough

1. **Step 1: Check `pom.xml`**: Show students `spring-boot-starter-validation` and Lombok configuration.
2. **Step 2: Inspect `Challenge.java`**: Demonstrate adding validation constraints (`@NotBlank`, `@Size`, `@Min`, `@Max`).
3. **Step 3: In-Memory Service Setup**:
   - Implement `ChallengeService` with `@Service`, an in-memory list seeded with starter challenges, and an `AtomicLong` ID generator.
   - Explain thread-safety considerations and singleton service lifecycle.
4. **Step 4: Refactor `WorkoutChallengeController`**: Explain injecting `ChallengeService` into the controller layer.
5. **Step 5: Write `WorkoutChallengeController` GET & POST**:
   - Write `@GetMapping("/create-challenge")`.
   - Write `@PostMapping("/create-challenge")`.
   - Emphasize `@Valid`, `BindingResult`, and the `redirect:/challenges` return string (PRG pattern).
6. **Step 6: Code `create-challenge.html`**:
   - Show `th:object="${challenge}"`.
   - Show `th:field="*{title}"`.
   - Show `th:classappend` and `th:errors`.
7. **Step 7: Live Test in Browser**:
   - Submit empty form -> Observe red borders and validation errors.
   - Fill valid quest details -> Submit -> Observe automatic redirect to `/challenges` with newly created card added to the in-memory list.
8. **Step 8: Launch Lab 2**: Walk students through the `README.md` requirements for `WorkoutLog` and the footer fragment bonus challenge.
