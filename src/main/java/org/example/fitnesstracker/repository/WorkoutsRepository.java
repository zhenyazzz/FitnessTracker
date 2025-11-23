package org.example.fitnesstracker.repository;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

import org.example.fitnesstracker.model.Workout;

@Repository
public interface WorkoutsRepository extends JpaRepository<Workout, Long>, JpaSpecificationExecutor<Workout> {

    Optional<Workout> findByIdAndUserId(Long id, Long currentUserId);

}
