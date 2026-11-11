# Thymeleaf Cheat Sheet (FitSquad Hub Edition)

## Quick Start

Thymeleaf uses the `th:` namespace to add server-side dynamic rendering to HTML. Declare the namespace on the root element and link your external static assets using `@{}`:

```html
<!DOCTYPE html>
<html lang="en" xmlns:th="http://www.thymeleaf.org">
<head>
    <title th:text="${pageTitle}">FitSquad Hub - Fitness Challenges</title>
    <!-- Static stylesheet linked from src/main/resources/static/css/style.css -->
    <link rel="stylesheet" th:href="@{/css/style.css}" href="/css/style.css">
</head>
```

---

## Core Domain Context (`Challenge.java`)

All examples in this cheat sheet bind directly to the FitSquad Hub model properties:

| Property | Type | Example Value |
| :--- | :--- | :--- |
| `id` | `Long` | `1L` |
| `title` | `String` | `"30-Day Core Crusher"` |
| `category` | `String` | `"Strength & Core"`, `"Cardio & HIIT"` |
| `description` | `String` | `"Sculpt and fortify core stability..."` |
| `targetGoal` | `String` | `"15 mins daily plank & core circuit"` |
| `durationDays` | `int` | `30` |
| `rewardPoints` | `int` | `450` |
| `difficulty` | `String` | `"Beginner"`, `"Intermediate"`, `"Advanced"` |
| `imageUrl` | `String` | `"https://images.unsplash.com/..."` |
| `active` | `boolean` | `true` / `false` |

---

## Core Attributes

### Variable Expression: `th:text` and `th:utext`

Display controller model attributes safely or render HTML formatted content.

```html
<!-- th:text = HTML escaped (safe, prevents XSS) -->
<h1 th:text="${pageTitle}">FitSquad Hub - Fitness Challenge Platform</h1>
<h3 th:text="${challenge.title}">Challenge Title</h3>

<!-- th:utext = unescaped raw HTML (use only for trusted rich content) -->
<div th:utext="${challenge.description}">Detailed workout instructions</div>
```

### Attribute Binding: `th:src`, `th:alt`, `th:attr`, and `th:disabled`

Dynamically bind model data to standard HTML attributes.

```html
<!-- Image source and alt attributes with image fallback -->
<img th:src="${challenge.imageUrl}" 
     th:alt="${challenge.title}" 
     class="challenge-img"
     onerror="this.src='/images/fallback-workout.jpg';">

<!-- Attribute replacement -->
<a th:attr="href=@{/challenges/{id}(id=${challenge.id})}" th:text="${challenge.title}">View</a>

<!-- Boolean attribute binding (e.g. disabled on enrollment button) -->
<button type="button" 
        class="btn btn-closed" 
        th:disabled="${!challenge.active}">
    <span th:text="${challenge.active ? 'Join Challenge' : 'Opening Soon'}">Enroll</span>
</button>
```

---

## Iteration: `th:each`

Loop through collections, such as the `challenges` list passed by `WorkoutChallengeController`.

```html
<!-- Basic list iteration -->
<div class="row g-4">
    <div class="col-md-6 col-lg-4" th:each="challenge : ${challenges}">
        <h3 th:text="${challenge.title}">30-Day Core Crusher</h3>
        <span th:text="${challenge.category}">Strength &amp; Core</span>
    </div>
</div>

<!-- Iteration with status tracking (index, count, first, last, size, even, odd) -->
<div th:each="challenge, stat : ${challenges}">
    <!-- 0-based index and 1-based count -->
    <span th:text="'Challenge #' + ${stat.count}">Challenge #1</span>
    <span th:text="'Index: ' + ${stat.index}">Index: 0</span>

    <!-- Highlighting first or last elements -->
    <span th:if="${stat.first}" class="badge bg-warning">Featured Spotlight</span>
    <span th:if="${stat.last}" class="badge bg-info">Latest Addition</span>

    <!-- Alternating row styling -->
    <div th:classappend="${stat.odd ? 'bg-dark' : 'bg-secondary'}">
        <p th:text="${challenge.title}">Title</p>
    </div>
</div>
```

---

## Conditionals: `th:if`, `th:unless`, and `th:switch`

Display or omit DOM elements based on boolean conditions.

```html
<!-- th:if: Rendered only when active is true -->
<span th:if="${challenge.active}" 
      class="badge bg-success-subtle text-success border border-success-subtle">
    <i class="bi bi-circle-fill animate-pulse"></i> Active
</span>

<!-- th:unless: Rendered only when active is false (evaluates !condition) -->
<span th:unless="${challenge.active}" 
      class="badge bg-secondary-subtle text-light-emphasis border border-secondary-subtle">
    <i class="bi bi-clock-history"></i> Upcoming
</span>

<!-- Empty state checking -->
<div th:if="${#lists.isEmpty(challenges)}" class="alert alert-dark text-center">
    <p>No Challenges Currently Scheduled</p>
</div>

<!-- Switch/Case for challenge difficulty levels -->
<div th:switch="${challenge.difficulty}">
    <span th:case="'Beginner'" class="diff-badge-beginner">Beginner Friendly</span>
    <span th:case="'Intermediate'" class="diff-badge-intermediate">Intermediate Workout</span>
    <span th:case="'Advanced'" class="diff-badge-advanced">Advanced Athletes Only</span>
    <span th:case="*">Open Level</span> <!-- Default fallback -->
</div>
```

---

## URL Generation: `@{}`

Construct context-aware URLs for endpoints and static resources.

```html
<!-- Root and basic endpoints -->
<a th:href="@{/challenges}">All Challenges</a>
<a th:href="@{/}">Home</a>

<!-- Static resource mapping (resolves to src/main/resources/static/css/style.css) -->
<link rel="stylesheet" th:href="@{/css/style.css}">

<!-- Path variables (e.g., /challenges/1) -->
<a th:href="@{/challenges/{id}(id=${challenge.id})}">View Details</a>

<!-- Query parameters (e.g., /challenges?category=Strength+%26+Core&difficulty=Advanced) -->
<a th:href="@{/challenges(category=${challenge.category},difficulty='Advanced')}">Filter Similar</a>

<!-- Multiple parameters with sorting and status -->
<a th:href="@{/challenges(active=true,minPoints=400,sort='rewardPoints,desc')}">High XP Challenges</a>
```

---

## CSS & Class Management

### `th:classappend` (Preferred for Preserving Base Classes)

Append classes dynamically without overriding existing Bootstrap/utility classes:

```html
<!-- Dynamic card border based on challenge status -->
<div class="card h-100 challenge-card"
     th:classappend="${challenge.active ? 'active-border' : 'inactive-card'}">
    <!-- Card content -->
</div>

<!-- Dynamic difficulty badge styling -->
<span class="badge rounded-pill fw-semibold"
      th:classappend="${challenge.difficulty == 'Beginner' ? 'diff-badge-beginner' : 
                       (challenge.difficulty == 'Intermediate' ? 'diff-badge-intermediate' : 'diff-badge-advanced')}"
      th:text="${challenge.difficulty}">
    Intermediate
</span>
```

### `th:class` and `th:style`

```html
<!-- Complete class replacement -->
<div th:class="${challenge.active} ? 'alert alert-success' : 'alert alert-secondary'">
    <span th:text="${challenge.title}">Challenge Title</span>
</div>

<!-- Dynamic inline styling -->
<div th:style="'background-image: url(' + ${challenge.imageUrl} + ');'"></div>
<div th:style="${challenge.active ? 'opacity: 1.0;' : 'opacity: 0.6;'}"></div>
```

---

## String Interpolation & Concatenation

Combine variables, literals, and units cleanly.

```html
<!-- Literal substitution syntax using pipes |...| -->
<h1 th:text="|Welcome to ${pageTitle}!|">FitSquad Hub</h1>
<p th:text="|This quest takes ${challenge.durationDays} days to complete.|">30 days</p>

<!-- String concatenation using '+' -->
<span th:text="${challenge.durationDays} + ' Days'">30 Days</span>
<span th:text="${challenge.rewardPoints} + ' XP'">450 XP</span>

<!-- Attribute interpolation -->
<a th:href="@{/challenges/{id}(id=${challenge.id})}" 
   th:title="|Click to join ${challenge.title}|">Join</a>
```

---

## Utility Objects (FitSquad Common Use Cases)

### `#lists` - Collection Operations

```html
<!-- Check if empty list -->
<div th:if="${#lists.isEmpty(challenges)}">
    No challenges available.
</div>

<!-- Get total count -->
<div class="display-6" th:text="${#lists.size(challenges)}">6</div>
<p th:text="|Showing ${#lists.size(challenges)} handpicked challenges|">Showing 6</p>
```

### `#strings` - String Manipulation

```html
<!-- Null or blank validation -->
<div th:if="${#strings.isEmpty(challenge.description)}">Description missing</div>

<!-- Case conversion & truncation -->
<span th:text="${#strings.toUpperCase(challenge.difficulty)}">INTERMEDIATE</span>
<p th:text="${#strings.abbreviate(challenge.description, 70)}">Short preview...</p>

<!-- Substring & containment check -->
<div th:if="${#strings.contains(challenge.category, 'Core')}">
    <i class="bi bi-shield-check"></i> Core Training Focus
</div>
```

### `#numbers` - Formatting XP & Durations

```html
<!-- Format points with commas or decimals -->
<span th:text="${#numbers.formatInteger(challenge.rewardPoints, 3, 'COMMA')} + ' XP'">1,000 XP</span>
<span th:text="${#numbers.formatDecimal(challenge.durationDays / 7.0, 1, 1)} + ' Weeks'">4.3 Weeks</span>
```

---

## Form Binding: `th:object` and `th:field`

Create forms to register or update challenges:

```html
<form th:action="@{/challenges/save}" th:object="${challenge}" method="post">
    <!-- Hidden ID Field -->
    <input type="hidden" th:field="*{id}">

    <!-- Title Input with Error Handling -->
    <div class="mb-3">
        <label for="title" class="form-label text-white">Challenge Title</label>
        <input type="text" 
               class="form-control bg-dark text-light border-secondary"
               th:field="*{title}"
               th:classappend="${#fields.hasErrors('title')} ? 'is-invalid' : ''"
               placeholder="e.g., 30-Day Core Crusher">
        <div class="invalid-feedback" th:if="${#fields.hasErrors('title')}" th:errors="*{title}">
            Title is required.
        </div>
    </div>

    <!-- Category Dropdown -->
    <div class="mb-3">
        <label for="category" class="form-label text-white">Category</label>
        <select class="form-select bg-dark text-light border-secondary" th:field="*{category}">
            <option value="Strength & Core">Strength &amp; Core</option>
            <option value="Cardio & HIIT">Cardio &amp; HIIT</option>
            <option value="Flexibility & Recovery">Flexibility &amp; Recovery</option>
            <option value="Habit & Wellness">Habit &amp; Wellness</option>
        </select>
    </div>

    <!-- Numbers: Duration & XP -->
    <div class="row g-3 mb-3">
        <div class="col-md-6">
            <label class="form-label text-white">Duration (Days)</label>
            <input type="number" class="form-control bg-dark text-light" th:field="*{durationDays}">
        </div>
        <div class="col-md-6">
            <label class="form-label text-white">Reward (XP)</label>
            <input type="number" class="form-control bg-dark text-light" th:field="*{rewardPoints}">
        </div>
    </div>

    <!-- Active Status Checkbox -->
    <div class="form-check mb-4">
        <input class="form-check-input" type="checkbox" th:field="*{active}" id="activeCheck">
        <label class="form-check-label text-light" for="activeCheck">Open for Enrollment</label>
    </div>

    <button type="submit" class="btn btn-join px-4">Create Challenge</button>
</form>
```

---

## Fragment Reusability: `th:fragment` & `th:replace`

Structure repeated layouts (Navbar, Card, Footer) across views.

### 1. Define fragments in `src/main/resources/templates/fragments/layout.html`:
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<body>

    <!-- Header Navigation Fragment -->
    <nav th:fragment="navbar" class="navbar navbar-expand-lg glass-nav sticky-top">
        <div class="container py-1">
            <a class="navbar-brand text-white fw-bold" th:href="@{/challenges}">
                FitSquad<span class="text-gradient">Hub</span>
            </a>
        </div>
    </nav>

    <!-- Footer Fragment -->
    <footer th:fragment="footer" class="container text-center mt-5 pt-4 text-secondary small">
        <p>&copy; 2026 FitSquad Hub &bull; Empowering Fitness Communities.</p>
    </footer>

</body>
</html>
```

### 2. Include fragments in `challenges.html`:
```html
<!-- Replaces placeholder with the navbar fragment -->
<div th:replace="~{fragments/layout :: navbar}"></div>

<!-- Main content -->
<main class="container">
    <!-- Challenge grid -->
</main>

<!-- Replaces placeholder with footer fragment -->
<div th:replace="~{fragments/layout :: footer}"></div>
```

---

## Local Variables: `th:with`

Define scoped variables to simplify repetitive checks:

```html
<!-- Compute high reward badge status once -->
<div th:with="isHighTier=${challenge.rewardPoints >= 500}">
    <span th:if="${isHighTier}" class="badge bg-warning text-dark">
        <i class="bi bi-star-fill"></i> High XP Bounty
    </span>
</div>

<!-- Calculate approximate weeks in loop -->
<div th:each="challenge : ${challenges}" th:with="weeks=${challenge.durationDays / 7}">
    <span class="text-muted" th:text="|~${weeks} Weeks Program|">~4 Weeks Program</span>
</div>
```

---

## Operators & Elvis Expression

```html
<!-- Elvis operator (provides a fallback if value is null) -->
<p th:text="${challenge.targetGoal} ?: 'Daily consistency workout'"></p>

<!-- Ternary operator -->
<span th:text="${challenge.rewardPoints >= 500 ? 'Epic Reward' : 'Standard Reward'}"></span>

<!-- Logical operators (and, or, not, !) -->
<div th:if="${challenge.active and challenge.difficulty == 'Advanced'}">
    <span class="badge bg-danger">Elite Challenge</span>
</div>

<div th:if="${challenge.durationDays <= 14 or challenge.difficulty == 'Beginner'}">
    <span class="badge bg-success">Great for Beginners</span>
</div>
```

---

## Comments

```html
<!-- Standard HTML comment (rendered in HTML source) -->
<!-- Visible in browser source inspection -->

<!-- Thymeleaf parser-level comment (stripped before reaching browser) -->
<!--/* This is completely stripped from server response */-->

<!-- Prototype-only comment (rendered when previewing file directly in browser, executed dynamically by Thymeleaf) -->
<!--/*/
<div th:text="${pageTitle}">Only visible when processed by Thymeleaf</div>
/*/-->
```

---

## Best Practices for FitSquad Hub

1. **Static Assets**: Always use `th:href="@{/css/style.css}"` for your static CSS so Spring Boot handles application context paths properly.
2. **Class Modifiers**: Use `th:classappend` for dynamic state badges (`diff-badge-*`, `active-border`) so Bootstrap styles aren't overwritten.
3. **Empty Collection Defense**: Pair `th:if="${#lists.isEmpty(challenges)}"` with `th:unless="${#lists.isEmpty(challenges)}"` to ensure your UI handles empty database responses gracefully.
4. **Data Safety**: Stick to `th:text` for titles and user goals to prevent XSS. Only use `th:utext` if you explicitly support sanitized rich text descriptions.
5. **Separation of Concerns**: Keep business calculations (e.g. XP computation, challenge queries) in `WorkoutChallengeController` or a service class rather than complex Thymeleaf expressions.
