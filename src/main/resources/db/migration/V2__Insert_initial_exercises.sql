-- Insert basic exercises for each muscle group

-- Chest exercises
INSERT INTO exercises (name, muscle_group, description) VALUES
('Bench Press', 'CHEST', 'Classic barbell bench press for chest development'),
('Dumbbell Press', 'CHEST', 'Dumbbell chest press for balanced development'),
('Push Ups', 'CHEST', 'Bodyweight chest exercise'),
('Incline Bench Press', 'CHEST', 'Targets upper chest muscles');

-- Back exercises
INSERT INTO exercises (name, muscle_group, description) VALUES
('Deadlift', 'BACK', 'Compound exercise for entire posterior chain'),
('Pull Ups', 'BACK', 'Bodyweight back exercise'),
('Barbell Row', 'BACK', 'Targets middle back muscles'),
('Lat Pulldown', 'BACK', 'Machine exercise for lats');

-- Legs exercises
INSERT INTO exercises (name, muscle_group, description) VALUES
('Squat', 'LEGS', 'King of leg exercises'),
('Leg Press', 'LEGS', 'Machine-based quad exercise');
('Romanian Deadlift', 'LEGS', 'Targets hamstrings and glutes'),
('Leg Curl', 'LEGS', 'Isolation for hamstrings');

-- Shoulders exercises
INSERT INTO exercises (name, muscle_group, description) VALUES
('Military Press', 'SHOULDERS', 'Overhead press for shoulder development'),
('Lateral Raises', 'SHOULDERS', 'Isolation for side delts'),
('Front Raises', 'SHOULDERS', 'Targets front deltoids'),
('Face Pulls', 'SHOULDERS', 'Rear delt and upper back exercise');

-- Arms exercises
INSERT INTO exercises (name, muscle_group, description) VALUES
('Barbell Curl', 'ARMS', 'Classic bicep builder'),
('Tricep Dips', 'ARMS', 'Bodyweight tricep exercise'),
('Hammer Curl', 'ARMS', 'Neutral grip bicep curl'),
('Tricep Pushdown', 'ARMS', 'Cable tricep isolation');

-- Core exercises
INSERT INTO exercises (name, muscle_group, description) VALUES
('Plank', 'CORE', 'Isometric core exercise'),
('Crunches', 'CORE', 'Basic ab exercise');
('Hanging Leg Raise', 'CORE', 'Advanced ab exercise'),
('Cable Crunch', 'CORE', 'Weighted ab exercise');

-- Full body exercises
INSERT INTO exercises (name, muscle_group, description) VALUES
('Burpees', 'FULL_BODY', 'Full body cardio exercise'),
('Running', 'FULL_BODY', 'Cardio exercise')
('Mountain Climbers', 'FULL_BODY', 'Core and cardio combination'),
('Box Jumps', 'FULL_BODY', 'Explosive full body movement');

