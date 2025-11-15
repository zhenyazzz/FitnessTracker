-- Create users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    username VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create exercises table (reference data)
CREATE TABLE exercises (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    muscle_group VARCHAR(50) NOT NULL,
    description VARCHAR(500)
);

-- Create index on muscle_group for filtering
CREATE INDEX idx_exercise_muscle_group ON exercises(muscle_group);

-- Create workouts table
CREATE TABLE workouts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(50) NOT NULL,
    date DATE NOT NULL,
    duration INTEGER NOT NULL,
    calories INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_workout_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create indexes for workouts filtering and sorting
CREATE INDEX idx_workout_date ON workouts(date);
CREATE INDEX idx_workout_type ON workouts(type);

-- Create body_measurements table
CREATE TABLE body_measurements (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    date DATE NOT NULL,
    height DOUBLE PRECISION,
    weight DOUBLE PRECISION,
    chest DOUBLE PRECISION,
    shoulders DOUBLE PRECISION,
    waist DOUBLE PRECISION,
    hip DOUBLE PRECISION,
    bicep DOUBLE PRECISION,
    thigh DOUBLE PRECISION,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_body_measurement_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create index for body measurements date filtering
CREATE INDEX idx_body_measurement_date ON body_measurements(date);

-- Create media table (progress photos)
CREATE TABLE media (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    path VARCHAR(255) NOT NULL,
    note VARCHAR(500),
    file_size BIGINT,
    mime_type VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_media_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create index for media sorting by creation date
CREATE INDEX idx_media_created_at ON media(created_at);

-- Create workout_exercises table (many-to-many with additional data)
CREATE TABLE workout_exercises (
    id BIGSERIAL PRIMARY KEY,
    workout_id BIGINT NOT NULL,
    exercise_id BIGINT NOT NULL,
    sets INTEGER,
    reps INTEGER,
    weight DOUBLE PRECISION,
    distance DOUBLE PRECISION,
    time INTEGER,
    CONSTRAINT fk_workout_exercise_workout FOREIGN KEY (workout_id) REFERENCES workouts(id) ON DELETE CASCADE,
    CONSTRAINT fk_workout_exercise_exercise FOREIGN KEY (exercise_id) REFERENCES exercises(id) ON DELETE RESTRICT
);

-- Comments for documentation
COMMENT ON TABLE users IS 'Application users';
COMMENT ON TABLE exercises IS 'Reference table with predefined exercises';
COMMENT ON TABLE workouts IS 'User workout sessions';
COMMENT ON TABLE body_measurements IS 'User body measurements for progress tracking';
COMMENT ON TABLE media IS 'Progress photos uploaded by users';
COMMENT ON TABLE workout_exercises IS 'Exercises performed in each workout with sets, reps, and weights';

