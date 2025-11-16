CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    username VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE exercises (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    muscle_group VARCHAR(50) NOT NULL,
    description VARCHAR(500)
);

CREATE INDEX idx_exercise_muscle_group ON exercises(muscle_group);

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

CREATE INDEX idx_workout_date ON workouts(date);
CREATE INDEX idx_workout_type ON workouts(type);

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

CREATE INDEX idx_body_measurement_date ON body_measurements(date);

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

CREATE INDEX idx_media_created_at ON media(created_at);

CREATE TABLE refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(500) NOT NULL UNIQUE,
    expiry_date TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_refresh_token_token ON refresh_tokens(token);

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
