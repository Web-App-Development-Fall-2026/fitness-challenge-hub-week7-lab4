-- =========================================================================
-- 1. SEED COACHES (Table: COACHES)
-- =========================================================================
INSERT INTO COACHES (name, specialty, certification, experience_years, avatar_url)
VALUES ('Marcus Vance', 'Strength & Core', 'CSCS, NSCA Certified Coach', 12, 'https://images.unsplash.com/photo-1567013127542-490d757e51fc?auto=format&fit=crop&w=400&q=80');

INSERT INTO COACHES (name, specialty, certification, experience_years, avatar_url)
VALUES ('Elena Rostova', 'Cardio & HIIT', 'ACSM Certified Exercise Physiologist', 9, 'https://images.unsplash.com/photo-1594381898411-846e7d193883?auto=format&fit=crop&w=400&q=80');

INSERT INTO COACHES (name, specialty, certification, experience_years, avatar_url)
VALUES ('Darius Sterling', 'Hypertrophy & Power', 'USA Powerlifting Senior National Coach', 15, 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=400&q=80');

INSERT INTO COACHES (name, specialty, certification, experience_years, avatar_url)
VALUES ('Maya Lin', 'Flexibility & Recovery', 'RYT-500 Yoga Alliance & FRCms', 8, 'https://images.unsplash.com/photo-1518611012118-696072aa579a?auto=format&fit=crop&w=400&q=80');

INSERT INTO COACHES (name, specialty, certification, experience_years, avatar_url)
VALUES ('Liam O''Connor', 'Cardio Endurance', 'USATF Level 2 Endurance Specialist', 11, 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=400&q=80');

INSERT INTO COACHES (name, specialty, certification, experience_years, avatar_url)
VALUES ('Sofia Alvarez', 'Habit & Wellness', 'NASM Certified Wellness & Nutrition Coach', 7, 'https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&w=400&q=80');

-- =========================================================================
-- 2. SEED CHALLENGES (Table: CHALLENGES) — 12 Challenges for Pagination
-- =========================================================================
INSERT INTO CHALLENGES (created_at, title, category, description, target_goal, duration_days, reward_points, difficulty, image_url, active, coach_id)
VALUES (CURRENT_TIMESTAMP, '30-Day Core Crusher', 'Strength & Core',
        'Sculpt and fortify core stability with progressively escalating daily plank variations, hollow holds, and dynamic abdominal circuits.',
        '15 mins daily plank & core circuit', 30, 450, 'Intermediate',
        'https://images.unsplash.com/photo-1571019613454-1cb2f99b2d8b?auto=format&fit=crop&w=800&q=80', true, 1);

INSERT INTO CHALLENGES (created_at, title, category, description, target_goal, duration_days, reward_points, difficulty, image_url, active, coach_id)
VALUES (CURRENT_TIMESTAMP, 'HIIT Inferno Blast', 'Cardio & HIIT',
        'Torch calories, build cardiovascular endurance, and spike metabolic rate with high-intensity Tabata and explosive plyometric intervals.',
        '25 mins explosive interval training', 21, 500, 'Advanced',
        'https://images.unsplash.com/photo-1601422407692-ec4eeec1d9b3?auto=format&fit=crop&w=800&q=80', true, 2);

INSERT INTO CHALLENGES (created_at, title, category, description, target_goal, duration_days, reward_points, difficulty, image_url, active, coach_id)
VALUES (CURRENT_TIMESTAMP, 'Iron Titan Powerlifting', 'Hypertrophy & Power',
        'Focus on fundamental compound movements—barbell squat, bench press, deadlift, and overhead press—to construct raw functional power.',
        '4 heavy compound lifting sessions/wk', 45, 750, 'Advanced',
        'https://images.unsplash.com/photo-1517838277536-f5f99be501cd?auto=format&fit=crop&w=800&q=80', true, 3);

INSERT INTO CHALLENGES (created_at, title, category, description, target_goal, duration_days, reward_points, difficulty, image_url, active, coach_id)
VALUES (CURRENT_TIMESTAMP, 'Zen Flow Mobility & Yoga', 'Flexibility & Recovery',
        'Decompress tight joints, increase deep hip and spine mobility, and master breathwork through restorative vinyasa recovery sessions.',
        '20 mins mindful stretching & flow', 14, 250, 'Beginner',
        'https://images.unsplash.com/photo-1544367567-0f2fcb009e0b?auto=format&fit=crop&w=800&q=80', true, 4);

INSERT INTO CHALLENGES (created_at, title, category, description, target_goal, duration_days, reward_points, difficulty, image_url, active, coach_id)
VALUES (CURRENT_TIMESTAMP, 'Sunrise 5K Sprint Series', 'Cardio Endurance',
        'Build weekly running volume through structured pace drills, tempo intervals, and weekend outdoor endurance runs.',
        'Accumulate 25 km running per week', 28, 400, 'Intermediate',
        'https://images.unsplash.com/photo-1486218119243-13883505764c?auto=format&fit=crop&w=800&q=80', true, 5);

INSERT INTO CHALLENGES (created_at, title, category, description, target_goal, duration_days, reward_points, difficulty, image_url, active, coach_id)
VALUES (CURRENT_TIMESTAMP, 'Hydration & 10K Habit Builder', 'Habit & Wellness',
        'Dial in foundational wellness habits by consistently hitting 10,000 steps every day alongside optimal hydration discipline.',
        '10,000 steps + 3L water daily', 30, 300, 'Beginner',
        'https://images.unsplash.com/photo-1552674605-db6ffd4facb5?auto=format&fit=crop&w=800&q=80', true, 6);

INSERT INTO CHALLENGES (created_at, title, category, description, target_goal, duration_days, reward_points, difficulty, image_url, active, coach_id)
VALUES (CURRENT_TIMESTAMP, 'Kettlebell Tactical Armor', 'Strength & Core',
        'Master Turkish get-ups, two-handed swings, and goblet squats to build resilient shoulder stability and posterior chain endurance.',
        '20 mins daily kettlebell complex', 21, 550, 'Intermediate',
        'https://images.unsplash.com/photo-1517963879433-6ad2b056d712?auto=format&fit=crop&w=800&q=80', true, 1);

INSERT INTO CHALLENGES (created_at, title, category, description, target_goal, duration_days, reward_points, difficulty, image_url, active, coach_id)
VALUES (CURRENT_TIMESTAMP, 'Metabolic Row & Burpee Blitz', 'Cardio & HIIT',
        'Alternate 500m Concept2 ergometer sprints with cadence burpees to maximize VO2 max and anaerobic threshold capacity.',
        '5 rounds of 500m row + 15 burpees', 14, 600, 'Advanced',
        'https://images.unsplash.com/photo-1534438327276-14e5300c3a48?auto=format&fit=crop&w=800&q=80', true, 2);

INSERT INTO CHALLENGES (created_at, title, category, description, target_goal, duration_days, reward_points, difficulty, image_url, active, coach_id)
VALUES (CURRENT_TIMESTAMP, 'Olympic Pull & Squat Mastery', 'Hypertrophy & Power',
        'Develop explosive triple extension and front-rack strength through structured clean pulls, front squats, and overhead stability drills.',
        '45 mins strength & technique block', 60, 900, 'Advanced',
        'https://images.unsplash.com/photo-1526506118085-60ce8714f8c5?auto=format&fit=crop&w=800&q=80', true, 3);

INSERT INTO CHALLENGES (created_at, title, category, description, target_goal, duration_days, reward_points, difficulty, image_url, active, coach_id)
VALUES (CURRENT_TIMESTAMP, 'Evening Spine & Hip Decompression', 'Flexibility & Recovery',
        'Counteract desk posture with targeted thoracic extensions, 90/90 hip switches, and parasympathetic box breathing before sleep.',
        '15 mins evening mobility routine', 10, 200, 'Beginner',
        'https://images.unsplash.com/photo-1518611012118-696072aa579a?auto=format&fit=crop&w=800&q=80', true, 4);

INSERT INTO CHALLENGES (created_at, title, category, description, target_goal, duration_days, reward_points, difficulty, image_url, active, coach_id)
VALUES (CURRENT_TIMESTAMP, 'Half-Marathon Base Builder', 'Cardio Endurance',
        'Progressive aerobic base building combining Zone 2 conversational runs, hill repeats, and Sunday long-distance endurance progressions.',
        '40 km weekly cumulative distance', 60, 850, 'Advanced',
        'https://images.unsplash.com/photo-1461896836934-ffe607ba8211?auto=format&fit=crop&w=800&q=80', true, 5);

INSERT INTO CHALLENGES (created_at, title, category, description, target_goal, duration_days, reward_points, difficulty, image_url, active, coach_id)
VALUES (CURRENT_TIMESTAMP, 'Sleep & Cold Plunge Resilience', 'Habit & Wellness',
        'Optimize circadian recovery with 8 hours of consistent sleep hygiene paired with invigorating morning cold exposure protocols.',
        '8 hrs sleep + 2 min morning cold shower', 21, 350, 'Beginner',
        'https://images.unsplash.com/photo-1506126613408-eca07ce68773?auto=format&fit=crop&w=800&q=80', false, 6);

-- =========================================================================
-- 3. SEED WORKOUT LOGS (Table: WORKOUT_LOGS) — 8 Starter Logs
-- =========================================================================
INSERT INTO WORKOUT_LOGS (created_at, athlete_name, challenge_id, challenge_title, duration_minutes, calories_burned, intensity_level, workout_date, notes)
VALUES (CURRENT_TIMESTAMP, 'Alex Johnson', 1, '30-Day Core Crusher', 25, 220, 'High', '2026-10-25', 'Completed 4 sets of 90-second weighted planks and hollow body rocks.');

INSERT INTO WORKOUT_LOGS (created_at, athlete_name, challenge_id, challenge_title, duration_minutes, calories_burned, intensity_level, workout_date, notes)
VALUES (CURRENT_TIMESTAMP, 'Samantha Chen', 2, 'HIIT Inferno Blast', 30, 410, 'Extreme', '2026-10-26', 'Tabata jump squats and battle ropes pushed heart rate to 178 BPM!');

INSERT INTO WORKOUT_LOGS (created_at, athlete_name, challenge_id, challenge_title, duration_minutes, calories_burned, intensity_level, workout_date, notes)
VALUES (CURRENT_TIMESTAMP, 'Devon Brooks', 3, 'Iron Titan Powerlifting', 65, 580, 'High', '2026-10-27', 'Hit a new 5-rep personal record on barbell back squats at 315 lbs.');

INSERT INTO WORKOUT_LOGS (created_at, athlete_name, challenge_id, challenge_title, duration_minutes, calories_burned, intensity_level, workout_date, notes)
VALUES (CURRENT_TIMESTAMP, 'Priya Patel', 4, 'Zen Flow Mobility & Yoga', 20, 110, 'Low', '2026-10-28', 'Deep pigeon pose and thoracic openers felt amazing after a long study day.');

INSERT INTO WORKOUT_LOGS (created_at, athlete_name, challenge_id, challenge_title, duration_minutes, calories_burned, intensity_level, workout_date, notes)
VALUES (CURRENT_TIMESTAMP, 'Jordan Taylor', 5, 'Sunrise 5K Sprint Series', 35, 390, 'Moderate', '2026-10-29', 'Crisp morning 5K tempo run along the lakeshore trail at 4:45/km pace.');

INSERT INTO WORKOUT_LOGS (created_at, athlete_name, challenge_id, challenge_title, duration_minutes, calories_burned, intensity_level, workout_date, notes)
VALUES (CURRENT_TIMESTAMP, 'Lucas Moreau', 7, 'Kettlebell Tactical Armor', 30, 340, 'High', '2026-10-30', '100 unbroken kettlebell swings with the 24kg bell plus 10 Turkish get-ups.');

INSERT INTO WORKOUT_LOGS (created_at, athlete_name, challenge_id, challenge_title, duration_minutes, calories_burned, intensity_level, workout_date, notes)
VALUES (CURRENT_TIMESTAMP, 'Nadia Kowalski', 8, 'Metabolic Row & Burpee Blitz', 28, 460, 'Extreme', '2026-10-31', 'Held sub-1:50/500m pace across all 5 ergometer intervals.');

INSERT INTO WORKOUT_LOGS (created_at, athlete_name, challenge_id, challenge_title, duration_minutes, calories_burned, intensity_level, workout_date, notes)
VALUES (CURRENT_TIMESTAMP, 'Alex Johnson', 9, 'Olympic Pull & Squat Mastery', 55, 510, 'High', '2026-11-01', 'Front squat triples felt snappy; focused on upright torso positioning.');

