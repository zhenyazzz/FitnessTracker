package org.example.fitnesstracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.example.fitnesstracker.model.Exercise;

@Repository
public interface ExerciseRepository extends JpaRepository<Exercise, Long> {
}

