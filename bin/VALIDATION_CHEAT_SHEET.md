# Spring Boot & Thymeleaf Validation Cheat Sheet

This cheat sheet provides a quick reference for implementing validation in Spring Boot application models, controllers, and Thymeleaf views.

---

## 1. Common Jakarta/JSR-380 Validation Annotations

These annotations are placed on model fields to define constraints:

| Annotation | Description | Example |
| :--- | :--- | :--- |
| `@NotNull` | Element cannot be null (useful for Objects, Selects, dates, etc.) | `@NotNull(message = "Selection is required")` |
| `@NotEmpty` | Element cannot be null or empty (CharSequence, Collection, Map, Array) | `@NotEmpty(message = "List cannot be empty")` |
| `@NotBlank` | CharSequence cannot be null, and must have length > 0 after trimming | `@NotBlank(message = "Name is required")` |
| `@Size(min, max)` | Size/length must be within the specified limits | `@Size(min = 2, max = 50, message = "Must be 2-50 chars")` |
| `@Min(value)` | Must be a number greater than or equal to value | `@Min(value = 1, message = "Value must be at least 1")` |
| `@Max(value)` | Must be a number less than or equal to value | `@Max(value = 10, message = "Value cannot exceed 10")` |
| `@Positive` | Must be a strictly positive number (> 0) | `@Positive(message = "Must be positive")` |
| `@PositiveOrZero` | Must be a positive number or zero (>= 0) | `@PositiveOrZero(message = "Cannot be negative")` |
| `@Email` | String must be a well-formed email address | `@Email(message = "Must be a valid email address")` |
| `@FutureOrPresent` | Date/time must be in the present or future | `@FutureOrPresent(message = "Date must not be in the past")` |
| `@Future` | Date/time must be strictly in the future | `@Future(message = "Date must be in the future")` |
| `@PastOrPresent` | Date/time must be in the past or present | `@PastOrPresent(message = "Date cannot be in the future")` |
| `@Past` | Date/time must be strictly in the past | `@Past(message = "Date must be in the past")` |
| `@Pattern(regexp)` | Must match the specified regular expression | `@Pattern(regexp = "^[0-9]{5}$", message = "Must be a 5-digit ZIP code")` |

### Starter Dependency
Ensure `spring-boot-starter-validation` is included in your `pom.xml`:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

---

## 2. Spring Boot Controller Handling

To trigger validation in the controller, annotate the model attribute with `@Valid` (or `@Validated`) and include a `BindingResult` parameter directly following the validated object:

```java
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class MatchController {

    @PostMapping("/create-match")
    public String createMatch(
            @Valid @ModelAttribute("match") Match match, 
            BindingResult result, 
            Model model
    ) {
        // 1. Check for validation errors
        if (result.hasErrors()) {
            // Re-populate supporting drop-down lists/data if necessary
            model.addAttribute("teams", teamService.getTeams());
            return "create-match"; // Return to the form template (do NOT redirect)
        }

        // 2. Process valid data (Post-Redirect-Get pattern)
        matchService.addMatch(match);
        return "redirect:/matches";
    }
}
```

> [!IMPORTANT]
> **Parameter Order Rule:**  
> The `BindingResult` parameter **must immediately follow** the `@Valid @ModelAttribute` argument in your method parameter list. Placing any other argument (such as `Model`) in between will cause Spring to throw an uncaught `BindException`.

### Manual Validation Errors in Controller
You can also manually add custom business-logic errors to the `BindingResult` context:
```java
// Rejects the field value with a field name, error code, and default message
result.rejectValue("awayTeam.id", "error.awayTeam", "Away Team cannot be the same as home team");

// Or add a global error not tied to a single field:
result.reject("error.match", "Tournament registration has closed for this season.");
```

---

## 3. Thymeleaf HTML Template Integration

To display validation error messages dynamically:

### Step 1: Bind the Form Object
Use `th:object` on the `<form>` tag:
```html
<form th:action="@{/create-match}" th:object="${match}" method="POST" novalidate>
```

### Step 2: Add Inputs and Class Conditional Styling
Use `th:field` to bind inputs, and `th:classappend` to apply CSS validation styles like Bootstrap's `.is-invalid`:
```html
<select th:field="*{homeTeam.id}" 
        th:classappend="${#fields.hasErrors('homeTeam.id') ? 'is-invalid' : ''}" 
        class="form-select">
    <option value="">Select Team</option>
    <option th:each="t : ${teams}" th:value="${t.id}" th:text="${t.name}"></option>
</select>
```

### Step 3: Render Validation Message
Use `th:errors` to output the server-side error message. You can conditionally render the container using `th:if`:
```html
<div class="invalid-feedback" 
     th:if="${#fields.hasErrors('homeTeam.id')}" 
     th:errors="*{homeTeam.id}">
    Default fallback/design-time validation message.
</div>
```

> [!WARNING]
> **Common Typo Warning (from Live Lecture):**  
> Make sure to use **`th:errors`** (plural) and NOT `th:error` (singular). During the live lecture, using the singular `th:error` was a typo that caused the page to completely ignore the tag and fail to render the server-side error messages.

### Global Form Error Summary
To display an alert box summarizing all errors at the top of the form:
```html
<div class="alert alert-danger" th:if="${#fields.hasErrors('*')}">
    <p class="fw-bold mb-1">Please correct the following errors:</p>
    <ul class="mb-0">
        <li th:each="err : ${#fields.errors('*')}" th:text="${err}">Error detail</li>
    </ul>
</div>
```

---

## 4. FitSquad Hub Implementation Reference (`WorkoutLog`)

Here is how the concepts map directly to the FitSquad Hub Lab 2 & 3 assignment:

### Model (`WorkoutLog.java`)
```java
package com.example.FitnessChallengeApp_Test.model;

import jakarta.validation.constraints.*;
import lombok.*;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutLog {

    private Long id;

    private Date createdAt = new Date();

    @NotBlank(message = "Athlete name is required")
    @Size(min = 2, max = 40, message = "Athlete name must be between 2 and 40 characters")
    private String athleteName;

    @NotNull(message = "Please select an active challenge")
    private Long challengeId;

    private String challengeTitle;

    @NotNull(message = "Workout duration is required")
    @Min(value = 5, message = "Workout duration must be at least 5 minutes")
    @Max(value = 360, message = "Workout duration cannot exceed 360 minutes")
    private Integer durationMinutes;

    @NotNull(message = "Calories burned is required")
    @Min(value = 10, message = "Calories burned must be at least 10 kcal")
    @Max(value = 4000, message = "Calories burned cannot exceed 4,000 kcal")
    private Integer caloriesBurned;

    @NotBlank(message = "Please select an intensity level")
    private String intensityLevel;

    @NotBlank(message = "Workout date is required")
    private String workoutDate;

    @Size(max = 250, message = "Workout notes cannot exceed 250 characters")
    private String notes;
}
```

### Controller (`WorkoutLogController.java`)
```java
@PostMapping("/log-workout")
public String processLogWorkout(
        @Valid @ModelAttribute("workoutLog") WorkoutLog workoutLog,
        BindingResult bindingResult,
        Model model
) {
    if (bindingResult.hasErrors()) {
        // Re-populate helper collections for dropdowns
        model.addAttribute("challenges", challengeService.getAllChallenges());
        model.addAttribute("intensityLevels", workoutLogService.getIntensityLevels());
        model.addAttribute("pageTitle", "FitSquad Hub - Log Workout");
        return "log-workout"; // Return form view with errors
    }

    Challenge selectedChallenge = challengeService.getChallengeById(workoutLog.getChallengeId());
    workoutLogService.addLog(workoutLog, selectedChallenge);
    return "redirect:/workout-logs"; // Post-Redirect-Get pattern
}
```

### Template Snippet (`log-workout.html`)
```html
<form th:action="@{/log-workout}" th:object="${workoutLog}" method="post" novalidate>

    <!-- Athlete Name Input -->
    <div class="mb-3">
        <label for="athleteName" class="form-label">Athlete Name</label>
        <input type="text"
               id="athleteName"
               class="form-control"
               th:field="*{athleteName}"
               th:classappend="${#fields.hasErrors('athleteName')} ? 'is-invalid' : ''"
               placeholder="e.g., Alex Johnson">
        <div class="invalid-feedback"
             th:if="${#fields.hasErrors('athleteName')}"
             th:errors="*{athleteName}">
            Athlete name is required
        </div>
    </div>

    <!-- Challenge Dropdown -->
    <div class="mb-3">
        <label for="challengeId" class="form-label">Challenge</label>
        <select id="challengeId"
                class="form-select"
                th:field="*{challengeId}"
                th:classappend="${#fields.hasErrors('challengeId')} ? 'is-invalid' : ''">
            <option value="">-- Choose an Active Challenge --</option>
            <option th:each="c : ${challenges}"
                    th:value="${c.id}"
                    th:text="${c.title} + ' (' + ${c.category} + ')'"></option>
        </select>
        <div class="invalid-feedback"
             th:if="${#fields.hasErrors('challengeId')}"
             th:errors="*{challengeId}">
            Please select a challenge
        </div>
    </div>

    <button type="submit" class="btn btn-primary">Log Workout</button>
</form>
```

---

## 5. Quick Troubleshooting Checklist

- [ ] **Did you use `th:errors` (plural)?** Remember: `th:error` (singular) will silently do nothing.
- [ ] **Is `BindingResult` immediately after `@Valid`?** In your controller method parameters, `BindingResult` cannot be separated from `@Valid @ModelAttribute`.
- [ ] **Did you return the template name on errors instead of redirecting?** A `redirect:` creates a fresh HTTP GET request and clears all validation errors from memory.
- [ ] **Did you re-populate dropdown lists in the `hasErrors()` block?** When returning the form template after an error, model attributes passed to the previous `GET` request are not carried over automatically.
- [ ] **Is `th:object` defined on the `<form>`?** `th:field="*{...}"` requires `th:object="${...}"` on an ancestor element.
