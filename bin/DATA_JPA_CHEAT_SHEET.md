# Spring Data JPA & JPQL Cheat Sheet (FitSquad Hub Edition)

A comprehensive reference for Spring Data JPA, Jakarta Persistence (JPA), Entity Relationships, Dynamic Pagination/Sorting, and Java Persistence Query Language (JPQL) for **CPAN-228 Web Application Development**.

---

## 1. Quick Start & Architecture

Spring Data JPA eliminates virtually all boilerplate database access code. Instead of writing SQL queries, `PreparedStatement` parameters, and `RowMapper` code manually with `JdbcTemplate`, you declare an interface and Spring generates the implementation dynamically at runtime.

### Architectural Data Flow

```
[ HTTP Request ]
       │
       ▼
[ WorkoutChallengeController / WorkoutLogController ]
       │  (Calls business methods with Pageable / Sort / Filters)
       ▼
[ ChallengeService / WorkoutLogService ]
       │  (Calls repository methods)
       ▼
[ ChallengeRepository / WorkoutLogRepository ]  <─── Spring Data JPA creates proxy bean at startup
       │
       ▼
[ Hibernate ORM / EntityManager ]
       │  (Translates Entities & JPQL to dialect SQL)
       ▼
[ H2 / MySQL / PostgreSQL Database Engine ]
```

### Core Concept Comparison: Relational SQL vs. Spring Data JPA

| Relational Database Concept | Spring Data JPA Concept | FitSquad Hub Example |
| :--- | :--- | :--- |
| **Table** (`COACHES`, `CHALLENGES`, `WORKOUT_LOGS`) | **Entity Class** (`@Entity`) | [`Coach.java`](file:///src/main/java/com/example/FitnessChallengeApp_Test/model/Coach.java), [`Challenge.java`](file:///src/main/java/com/example/FitnessChallengeApp_Test/model/Challenge.java), [`WorkoutLog.java`](file:///src/main/java/com/example/FitnessChallengeApp_Test/model/WorkoutLog.java) |
| **Row / Record** | **Entity Object Instance** | `new Challenge(...)` |
| **Column** (`reward_points`) | **Field / Property** (`@Column`) | `private int rewardPoints;` |
| **Foreign Key** (`coach_id`, `challenge_id`) | **Object Relationship** (`@ManyToOne`) | `private Coach coach;` / `private Challenge challenge;` |
| **`SELECT` / `INSERT` / `UPDATE` / `DELETE`** | **Repository Interface Methods** | `challengeRepository.findAll()`, `save(challenge)` |

### Spring JDBC vs. Spring Data JPA Comparison

| Feature | Spring JDBC (`JdbcTemplate`) | Spring Data JPA (`JpaRepository`) |
| :--- | :--- | :--- |
| **Code Volume** | High (manual queries, mapping, parameter binding) | Near Zero (interface declaration only) |
| **Row Mapping** | Manual (`ResultSet` $\rightarrow$ `Challenge` mapping) | Automatic via JPA `@Entity` metadata |
| **Basic CRUD** | Manual `INSERT`, `UPDATE`, `SELECT`, `DELETE` | Pre-built out-of-the-box methods |
| **Dialect Portability** | Vendor SQL dependent | Database agnostic (Hibernate translates dialects) |
| **Query Mechanism** | Native SQL strings | Derived Method Names, JPQL (`@Query`), or Native SQL |
| **Relationship Management** | Manual JOINs and foreign key mapping | Object references (`@OneToMany`, `@ManyToOne`) |
| **Pagination & Sorting** | Manual `LIMIT` / `OFFSET` and dynamic `ORDER BY` | Built-in via `Pageable`, `PageRequest`, `Sort`, and `Page<T>` |

---

## 2. Dependencies & Configuration

### Maven Dependencies (`pom.xml`)

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-h2console</artifactId>
</dependency>
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>
```

### Application Properties (`application.properties`)

#### Strategy A: JPA Entity-Driven Mode (Used in FitSquad Hub — `@Entity` DDL + `data.sql`)
When you omit `schema.sql` and want JPA `@Entity` classes to automatically create your database schema before `data.sql` seeds the rows:
```properties
# H2 Console (Accessible at http://localhost:8080/h2-console)
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# H2 In-Memory Database Configuration
spring.datasource.url=jdbc:h2:mem:fitsquadhub;DB_CLOSE_DELAY=-1
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# 1. Instruct Hibernate to create/update tables automatically from @Entity classes
spring.jpa.hibernate.ddl-auto=update
# (Use 'create-drop' for clean in-memory test runs, or 'update' to preserve schema changes)

# 2. CRITICAL: Defer data.sql execution until AFTER Hibernate generates tables
spring.jpa.defer-datasource-initialization=true

# 3. Ensure SQL script initialization is enabled
spring.sql.init.mode=always

# 4. Show SQL in console (essential for learning & debugging)
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

> [!IMPORTANT]
> **Why `defer-datasource-initialization=true` is required:**
> By default in Spring Boot 2.5+, SQL scripts (`schema.sql` / `data.sql`) run **before** JPA/Hibernate initializes. If there is no `schema.sql`, `data.sql` attempts to insert records into non-existent tables, throwing a `Table "CHALLENGES" not found` error. Setting `spring.jpa.defer-datasource-initialization=true` reverses this sequence so Hibernate builds the tables first, followed by `data.sql` seeding the data.

#### Strategy B: Hybrid Mode (`schema.sql` + `data.sql`)
When you have a dedicated `schema.sql` file manually managing table creation:
```properties
# H2 In-Memory Database Configuration
spring.datasource.url=jdbc:h2:mem:fitsquadhub;DB_CLOSE_DELAY=-1
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# Execute schema.sql and data.sql on startup
spring.sql.init.mode=always

# ddl-auto=none ensures schema.sql controls table creation without conflicts
spring.jpa.hibernate.ddl-auto=none

# Show formatted SQL in console
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Disable Open-Session-In-View for strict transaction boundaries (if all associations are EAGER)
spring.jpa.open-in-view=false
```

### `spring.jpa.hibernate.ddl-auto` Options Reference

| Value | Behavior | Use Case |
| :--- | :--- | :--- |
| `none` | Hibernate performs no schema creation or validation. | Production, or when using `schema.sql` / Flyway / Liquibase. |
| `validate` | Validates tables and columns match `@Entity` definitions; aborts startup if mismatch. | Production sanity checks. |
| `update` | Updates table structures (adds missing tables/columns); never removes existing columns. | Classroom demos & local development. |
| `create` | Drops existing tables on startup, then creates fresh tables from entities. | Development testing (erases data on restart). |
| `create-drop` | Creates tables on startup and drops them when `EntityManagerFactory` shuts down. | In-memory testing (H2) and automated test suites. |

### Data Seeding Alternatives (When Not Using `data.sql`)

1. **Hibernate Native `import.sql`**:
   - Place `src/main/resources/import.sql`.
   - When `ddl-auto=create` or `create-drop`, Hibernate automatically runs `import.sql` right after schema generation without needing `defer-datasource-initialization`.
2. **Java `CommandLineRunner` / `ApplicationRunner` (Recommended Enterprise Pattern)**:
   - Type-safe, database-agnostic, and runs strictly after Spring Data JPA is fully ready:
   ```java
   @Configuration
   public class DataLoader {
       @Bean
       CommandLineRunner initData(ChallengeRepository repository) {
           return args -> {
               if (repository.count() == 0) {
                   Challenge challenge = new Challenge(
                           null,
                           "30-Day Core Crusher",
                           "Strength & Core",
                           "Progressive daily core and plank circuit.",
                           "15 mins daily plank & core circuit",
                           30,
                           450,
                           "Intermediate",
                           "https://images.unsplash.com/photo-1571019613454-1cb2f99b2d8b?auto=format&fit=crop&w=800&q=80",
                           true
                   );
                   repository.save(challenge);
               }
           };
       }
   }
   ```

---

## 3. Entity Mapping Essentials (`Challenge.java`)

An entity is a lightweight Java domain object mapped to a relational database table.

```java
package com.example.FitnessChallengeApp_Test.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.util.Date;

@Entity                                          // 1. Marks class as a JPA entity
@Table(name = "CHALLENGES")                      // 2. Maps entity to table 'CHALLENGES'
@Data
@NoArgsConstructor                               // 3. MANDATORY: JPA requires default no-arg constructor
@AllArgsConstructor
public class Challenge {

    @Id                                          // 4. Primary key indicator
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 5. Auto-increment identity column
    private Long id;

    @Column(name = "created_at")
    private Date createdAt = new Date();

    @NotBlank(message = "Challenge title is required")
    @Size(min = 3, max = 60, message = "Title must be between 3 and 60 characters")
    @Column(nullable = false, length = 60)
    private String title;

    @NotBlank(message = "Please select a category")
    @Column(nullable = false, length = 50)
    private String category;

    @NotBlank(message = "Description cannot be empty")
    @Size(min = 10, max = 500)
    @Column(nullable = false, length = 500)
    private String description;

    @NotBlank(message = "Target goal is required")
    @Size(min = 5, max = 100)
    @Column(name = "target_goal", nullable = false, length = 100)
    private String targetGoal;

    @Min(value = 1)
    @Max(value = 365)
    @Column(name = "duration_days")
    private int durationDays;

    @Min(value = 50)
    @Max(value = 5000)
    @Column(name = "reward_points")
    private int rewardPoints;

    @NotBlank(message = "Please select a difficulty level")
    @Column(nullable = false, length = 30)
    private String difficulty;

    @Column(name = "image_url", length = 300)
    private String imageUrl;

    private boolean active = true;

    // 6. Many Challenges can be led by One Coach
    @ManyToOne
    @JoinColumn(name = "coach_id")
    private Coach coach;

    // 7. Optional JPA Lifecycle Callback Hook
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = new Date();
        }
    }
}
```

### Essential JPA Annotations Reference

| Annotation | Placement | Description |
| :--- | :--- | :--- |
| `@Entity` | Class | Declares the class as a persistent entity managed by JPA/Hibernate. |
| `@Table(name = "...")` | Class | Specifies the database table name (defaults to class name if omitted). |
| `@Id` | Field | Declares the primary key field. |
| `@GeneratedValue(...)` | Field | Defines primary key generation strategy (`IDENTITY`, `SEQUENCE`, `AUTO`, `UUID`). |
| `@Column(name = "...", nullable = false, length = 50)` | Field | Configures column name mapping, `NOT NULL` constraints, and `VARCHAR` length. |
| `@ManyToOne` | Field | Defines a many-to-one relationship to another `@Entity`. |
| `@JoinColumn(name = "...")` | Field | Specifies the foreign key column name in the owning table. |
| `@Transient` | Field | Instructs JPA to ignore this field; not saved to the database. |
| `@Enumerated(EnumType.STRING)` | Field | Maps a Java `enum` by its string name instead of numeric ordinal index. |
| `@PrePersist` / `@PreUpdate` | Method | Lifecycle callbacks invoked automatically before `INSERT` or `UPDATE`. |

---

## 4. Repository Hierarchy

Spring Data organizes persistence interfaces into a clear inheritance hierarchy:

```
          Repository<T, ID>                 (Marker interface)
                 │
                 ▼
        CrudRepository<T, ID>              (save, findById, existsById, findAll, count, deleteById)
                 │
                 ▼
   ListCrudRepository<T, ID>               (Returns List<T> instead of Iterable<T>)
                 │
                 ▼
    PagingAndSortingRepository<T, ID>      (findAll(Sort), findAll(Pageable))
                 │
                 ▼
        JpaRepository<T, ID>               (Combines all of the above + flush, saveAndFlush, deleteInBatch)
```

### Methods Provided Free by `JpaRepository<Challenge, Long>`

| Method | Return Type | Description |
| :--- | :--- | :--- |
| `save(challenge)` | `Challenge` | `INSERT`s if `id == null`; `UPDATE`s if `id != null`. |
| `saveAll(iterable)` | `List<Challenge>` | Saves all entities in a collection. |
| `findById(1L)` | `Optional<Challenge>` | Retrieves entity wrapped in `Optional` (use `.orElse(null)`). |
| `existsById(1L)` | `boolean` | Fast check if entity exists (`SELECT COUNT(*) > 0`). |
| `findAll()` | `List<Challenge>` | Returns all records in the table. |
| `findAll(Sort sort)` | `List<Challenge>` | Returns all records sorted by the given criteria. |
| `findAll(Pageable pageable)` | `Page<Challenge>` | Returns a single page slice with total count metadata. |
| `findAllById(ids)` | `List<Challenge>` | Batch retrieves entities by a list of primary key IDs. |
| `count()` | `long` | Returns total row count. |
| `deleteById(1L)` | `void` | Deletes row by primary key. |
| `delete(challenge)` | `void` | Deletes the given entity instance. |
| `deleteAll()` | `void` | Deletes all records in the table. |
| `flush()` | `void` | Synchronizes pending EntityManager changes with the database immediately. |
| `saveAndFlush(c)` | `Challenge` | Persists and immediately flushes in a single step. |

---

## 5. Derived Query Methods (Method Name Keywords)

Spring Data JPA parses method names to synthesize queries automatically without writing any SQL or JPQL:

```java
@Repository
public interface ChallengeRepository extends JpaRepository<Challenge, Long> {
    List<Challenge> findByCategory(String category);
    Page<Challenge> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    Page<Challenge> findByRewardPointsGreaterThanEqual(int minRewardPoints, Pageable pageable);
}
```

### Complete Keyword Cheat Sheet

| Keyword | Method Name Pattern | Equivalent SQL WHERE Clause |
| :--- | :--- | :--- |
| **Exact Match** | `findByCategory(String cat)` | `WHERE category = ?` |
| **Not** | `findByCategoryNot(String cat)` | `WHERE category <> ?` |
| **Boolean True/False** | `findByActiveTrue()` | `WHERE active = TRUE` |
| **Between** | `findByDurationDaysBetween(10, 30)` | `WHERE duration_days BETWEEN ? AND ?` |
| **LessThan / Equal** | `findByDurationDaysLessThanEqual(30)` | `WHERE duration_days <= ?` |
| **GreaterThan / Equal** | `findByRewardPointsGreaterThanEqual(500)` | `WHERE reward_points >= ?` |
| **After / Before** | `findByCreatedAtAfter(Date date)` | `WHERE created_at > ?` |
| **IsNull / IsNotNull** | `findByImageUrlIsNull()` | `WHERE image_url IS NULL` |
| **Like** | `findByTitleLike("%Core%")` | `WHERE title LIKE ?` |
| **StartingWith** | `findByTitleStartingWith("30-Day")` | `WHERE title LIKE '30-Day%'` |
| **EndingWith** | `findByTitleEndingWith("Blast")` | `WHERE title LIKE '%Blast'` |
| **Containing** | `findByTitleContaining("Inferno")` | `WHERE title LIKE '%Inferno%'` |
| **IgnoreCase** | `findByTitleContainingIgnoreCase("core")` | `WHERE LOWER(title) LIKE LOWER('%core%')` |
| **In** | `findByCategoryIn(List<String> cats)` | `WHERE category IN (?, ?)` |
| **NotIn** | `findByCategoryNotIn(List<String> cats)` | `WHERE category NOT IN (?, ?)` |
| **And** | `findByCategoryAndDifficulty(cat, diff)` | `WHERE category = ? AND difficulty = ?` |
| **Or** | `findByCategoryOrDifficulty(cat, diff)` | `WHERE category = ? OR difficulty = ?` |
| **Relationship Traversal** | `findByCoachNameIgnoreCase(String name)` | ` JOIN coaches c ON ... WHERE LOWER(c.name) = LOWER(?)` |
| **OrderBy** | `findByActiveTrueOrderByRewardPointsDesc()` | `WHERE active = TRUE ORDER BY reward_points DESC` |
| **Limit / Top** | `findTop3ByOrderByRewardPointsDesc()` | `ORDER BY reward_points DESC FETCH FIRST 3 ROWS` |
| **Distinct** | `findDistinctByCategory(String cat)` | `SELECT DISTINCT ... WHERE category = ?` |
| **Count** | `countByCategory(String cat)` | `SELECT COUNT(id) WHERE category = ?` |
| **Exists** | `existsByTitleIgnoreCase(String title)` | `SELECT COUNT(id) > 0 WHERE LOWER(title) = ?` |
| **Delete** | `deleteByActiveFalse()` | `DELETE FROM challenges WHERE active = FALSE` |

---

## 6. Custom JPQL Queries (`@Query`) Masterclass

**Java Persistence Query Language (JPQL)** is an object-oriented query language. Instead of querying database tables and columns, **JPQL queries Java entities and fields**.

### JPQL vs. SQL Rules

```
  JPQL:  SELECT c FROM Challenge c WHERE c.rewardPoints >= :minReward
                        ▲                    ▲
                   Entity Name           Java Field

   SQL:  SELECT * FROM CHALLENGES WHERE reward_points >= ?
                           ▲                 ▲
                      Table Name        Database Column
```

> [!IMPORTANT]
> - **Entity & field names are CASE-SENSITIVE** in JPQL (`Challenge`, not `challenges`; `rewardPoints`, not `reward_points`).
> - **JPQL keywords are CASE-INSENSITIVE** (`SELECT`, `select`, `WHERE`, `where`). Uppercase is standard convention.

---

### JPQL Examples

#### 1. Positional Parameters (`?1`, `?2`)
```java
@Query("SELECT w FROM WorkoutLog w WHERE LOWER(w.intensityLevel) = LOWER(?1) AND w.durationMinutes >= ?2 ORDER BY w.workoutDate DESC")
Page<WorkoutLog> findLogsByIntensityAndMinDuration(String intensity, int minDuration, Pageable pageable);
```

#### 2. Named Parameters (`:param` + `@Param`) *(Recommended)*
```java
@Query("SELECT c FROM Challenge c WHERE LOWER(c.category) LIKE LOWER(CONCAT('%', :cat, '%')) AND c.rewardPoints >= :minReward ORDER BY c.rewardPoints DESC")
Page<Challenge> findEliteChallengesByCategory(
        @Param("cat") String category,
        @Param("minReward") int minReward,
        Pageable pageable
);
```

#### 3. Full-Text Search with `LIKE`, `LOWER()`, and `CONCAT()`
```java
@Query("SELECT c FROM Challenge c WHERE " +
       "LOWER(c.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
       "LOWER(c.description) LIKE LOWER(CONCAT('%', :search, '%'))")
List<Challenge> searchByKeyword(@Param("search") String search);
```

#### 4. Range Query with `BETWEEN`
```java
@Query("SELECT c FROM Challenge c WHERE c.durationDays BETWEEN :minDays AND :maxDays ORDER BY c.durationDays ASC")
List<Challenge> findByDurationRange(
        @Param("minDays") int minDays,
        @Param("maxDays") int maxDays
);
```

#### 5. Membership Query with `IN (:collection)`
```java
@Query("SELECT c FROM Challenge c WHERE c.category IN :categories AND c.active = true")
List<Challenge> findByCategoriesIn(@Param("categories") List<String> categories);
```

#### 6. Projection: Selecting Specific Columns / Distinct Values
```java
// Returns List<String> rather than List<Challenge>
@Query("SELECT DISTINCT c.category FROM Challenge c ORDER BY c.category ASC")
List<String> findDistinctCategories();
```

#### 7. Aggregate Functions (`COUNT`, `AVG`, `MAX`, `MIN`, `SUM`)
```java
@Query("SELECT AVG(c.durationDays) FROM Challenge c WHERE c.category = :category")
Double findAverageDurationByCategory(@Param("category") String category);

@Query("SELECT MAX(c.rewardPoints) FROM Challenge c")
Integer findMaxRewardPoints();

// Group By aggregate
@Query("SELECT c.difficulty, COUNT(c) FROM Challenge c GROUP BY c.difficulty")
List<Object[]> countChallengesByDifficulty();
```

#### 8. Modifying Queries (DML: `UPDATE` and `DELETE`)
> [!WARNING]
> Any JPQL query that executes `UPDATE` or `DELETE` **MUST** be annotated with `@Modifying` and `@Transactional`.

```java
@Modifying
@Transactional
@Query("UPDATE Challenge c SET c.active = :active WHERE c.id = :id")
int updateActiveStatus(@Param("id") Long id, @Param("active") boolean active);

@Modifying
@Transactional
@Query("UPDATE Challenge c SET c.rewardPoints = c.rewardPoints + :bonus WHERE c.difficulty = :difficulty")
int addBonusPointsToDifficulty(@Param("difficulty") String difficulty, @Param("bonus") int bonus);

@Modifying
@Transactional
@Query("DELETE FROM Challenge c WHERE c.active = false")
int deleteInactiveChallenges();
```

#### 9. Native SQL Queries (`nativeQuery = true`)
Use when you need database-specific SQL features (e.g., window functions, CTEs, or vendor dialect extensions):

```java
@Query(value = "SELECT * FROM CHALLENGES WHERE duration_days <= :days AND active = TRUE ORDER BY reward_points DESC", nativeQuery = true)
List<Challenge> findShortActiveChallengesNative(@Param("days") int days);
```

---

## 7. Pagination and Dynamic Sorting (`Pageable`, `PageRequest`, `Sort`, `Page<T>`)

Spring Data JPA provides first-class pagination via `Pageable`, `PageRequest`, `Sort`, and `Page<T>`.

### A. Repository & Service Layer
```java
// In ChallengeRepository.java
Page<Challenge> findByTitleContainingIgnoreCase(String title, Pageable pageable);

// In ChallengeService.java
public Page<Challenge> searchByTitleIgnoreCase(String title, Pageable pageable) {
    return challengeRepository.findByTitleContainingIgnoreCase(title, pageable);
}
```

### B. Controller Layer (`WorkoutChallengeController.java`)
```java
@GetMapping({"/challenges"})
public String showChallenges(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "6") int size,
        @RequestParam(defaultValue = "id") String sort,
        @RequestParam(defaultValue = "ASC") String direction,
        @RequestParam(required = false) String search,
        @RequestParam(defaultValue = "all") String filterType,
        Model model
) {
    // 1. Construct Sort & Pageable (0-indexed page number)
    Sort.Direction sortDir = Sort.Direction.fromString(direction);
    Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sort));

    // 2. Execute paginated query
    Page<Challenge> challengePage = challengeService.getChallenges(pageable);

    // 3. Pass current page content & pagination metadata to Thymeleaf
    model.addAttribute("challenges", challengePage.getContent());
    model.addAttribute("totalPages", challengePage.getTotalPages());
    model.addAttribute("totalElements", challengePage.getTotalElements());
    model.addAttribute("currentPage", page);
    model.addAttribute("pageSize", size);
    model.addAttribute("hasPrevious", challengePage.hasPrevious());
    model.addAttribute("hasNext", challengePage.hasNext());

    // 4. Preserve filter & sort state across page clicks
    model.addAttribute("search", search);
    model.addAttribute("sort", sort);
    model.addAttribute("direction", direction);
    model.addAttribute("filterType", filterType);

    return "challenges";
}
```

### C. Thymeleaf Template Links (`challenges.html`)
```html
<!-- 1. Toggle Sort Direction Link (ASC <-> DESC) -->
<a th:href="@{/challenges(page=${currentPage}, size=${pageSize}, sort='rewardPoints', direction=${sort == 'rewardPoints' && direction == 'ASC' ? 'DESC' : 'ASC'}, search=${search}, filterType=${filterType})}">
    Reward XP
    <span th:if="${sort == 'rewardPoints'}" th:text="${direction == 'ASC' ? '↑' : '↓'}"></span>
</a>

<!-- 2. Next Page Button Preserving Sort & Search Parameters -->
<li class="page-item" th:classappend="${!hasNext} ? 'disabled'">
    <a class="page-link"
       th:href="@{/challenges(page=${currentPage + 1}, size=${pageSize}, sort=${sort}, direction=${direction}, search=${search}, filterType=${filterType})}">
        Next
    </a>
</li>
```

---

## 8. Entity Relationship Mappings (`@ManyToOne` in FitSquad Hub)

FitSquad Hub models a three-tier relational hierarchy:

```
┌─────────────────┐             ┌─────────────────┐             ┌─────────────────┐
│      COACH      │ 1         * │    CHALLENGE    │ 1         * │   WORKOUT_LOG   │
│ (COACHES Table) ├─────────────┤(CHALLENGES Tbl) ├─────────────┤(WORKOUT_LOGS Tbl│
└─────────────────┘  coach_id   └─────────────────┘ challenge_id└─────────────────┘
```

### Child Entity Mapping (`WorkoutLog.java`)
```java
@Entity
@Table(name = "WORKOUT_LOGS")
public class WorkoutLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "athlete_name", nullable = false, length = 40)
    private String athleteName;

    // Many WorkoutLogs reference One Challenge
    @ManyToOne
    @JoinColumn(name = "challenge_id")
    private Challenge challenge;
}
```

### Binding a `@ManyToOne` Relationship in a Thymeleaf Form (`log-workout.html`)
1. In `GET /log-workout`, initialize the nested object on the backing bean so Thymeleaf can bind `*{challenge.id}`:
   ```java
   WorkoutLog workoutLog = new WorkoutLog();
   workoutLog.setChallenge(new Challenge());
   model.addAttribute("workoutLog", workoutLog);
   model.addAttribute("challenges", challengeService.getAllChallenges());
   ```
2. In `log-workout.html`, bind the `<select>` to `*{challenge.id}`:
   ```html
   <select id="challengeId" th:field="*{challenge.id}" class="form-select">
       <option value="">-- Choose an Active Challenge --</option>
       <option th:each="c : ${challenges}"
               th:value="${c.id}"
               th:text="${c.title} + ' (' + ${c.category} + ')'"></option>
   </select>
   ```
3. In `POST /log-workout`, look up the selected `Challenge` entity by ID and attach it before calling `save()`:
   ```java
   Challenge selectedChallenge = challengeService.getChallengeById(workoutLog.getChallenge().getId());
   workoutLogService.addLog(workoutLog, selectedChallenge);
   ```

---

## 9. Common Classroom & Exam Pitfalls

| # | Pitfall | Cause | Solution |
| :--- | :--- | :--- | :--- |
| **1** | `InstantiationException: No default constructor` | JPA entities require a no-arg constructor to instantiate via reflection. | Add `@NoArgsConstructor` from Lombok or define `public Challenge() {}`. |
| **2** | `QuerySyntaxException: unexpected token` or `isn't mapped` | Referencing the SQL table name (e.g., `FROM CHALLENGES`) in JPQL. | Refer to the case-sensitive Java Entity name: `FROM Challenge c`. |
| **3** | `PropertyReferenceException: No property 'reward_points' found` | Passing a snake_case SQL column name to `Sort.by(...)` or JPQL. | Always use the Java camelCase property name (`rewardPoints`, `durationMinutes`). |
| **4** | `InvalidDataAccessApiUsageException: Modifying queries can only use...` | Missing `@Modifying` on JPQL `UPDATE` or `DELETE`. | Add both `@Modifying` and `@Transactional` to the repository method. |
| **5** | Parameter index out of bounds or parameter name not found | Mismatch between JPQL placeholder (`:cat`) and method parameter. | Ensure parameter name matches or annotate with `@Param("cat") String category`. |
| **6** | Changes not visible after JPQL bulk update | Entity in first-level persistence cache is stale after bulk update. | Add `@Modifying(clearAutomatically = true)` to clear the persistence context. |
| **7** | `NoUniqueBeanDefinitionException` | Multiple Spring beans implement the same interface. | Use `@Primary` on the active repository or specify `@Qualifier("name")`. |
| **8** | `Table "..." not found` executing `data.sql` (without `schema.sql`) | Spring Boot runs `data.sql` *before* Hibernate creates tables via `ddl-auto`. | Add `spring.jpa.defer-datasource-initialization=true` in `application.properties`. |
| **9** | Primary key collision (`Unique index or primary key violation`) on `save()` | `data.sql` manually inserted `id` values (`1, 2, 3`) without advancing the `IDENTITY` sequence. | Omit the `id` column in `INSERT INTO ...` statements in `data.sql` so H2 auto-increments cleanly. |
