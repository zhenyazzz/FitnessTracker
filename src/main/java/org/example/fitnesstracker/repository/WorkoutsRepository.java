package org.example.fitnesstracker.repository;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.time.LocalDate;

import org.example.fitnesstracker.model.Workout;
import org.example.fitnesstracker.model.enums.WorkoutType;

@Repository
public interface WorkoutsRepository extends JpaRepository<Workout, Long>, JpaSpecificationExecutor<Workout> {

    Optional<Workout> findByIdAndUserId(Long id, Long currentUserId);

    @Query("""
        SELECT COUNT(w)
        FROM Workout w
        WHERE w.user.id = :userId
        AND w.date >= COALESCE(:dateFrom, w.date)
        AND w.date <= COALESCE(:dateTo, w.date)
        """)
    Long calculateTotalWorkoutsByUserId(@Param("userId") Long userId,
                                        @Param("dateFrom") LocalDate dateFrom, 
                                        @Param("dateTo") LocalDate dateTo);

    @Query("""
        SELECT COALESCE(SUM(w.calories), 0)
        FROM Workout w
        WHERE w.user.id = :userId
        AND w.date >= COALESCE(:dateFrom, w.date)
        AND w.date <= COALESCE(:dateTo, w.date)
        """)
    Integer calculateTotalCaloriesBurnedByUserId(@Param("userId") Long userId,
                                               @Param("dateFrom") LocalDate dateFrom,
                                               @Param("dateTo") LocalDate dateTo);

    @Query("""
        SELECT COALESCE(SUM(w.duration), 0)
        FROM Workout w
        WHERE w.user.id = :userId
        AND w.date >= COALESCE(:dateFrom, w.date)
        AND w.date <= COALESCE(:dateTo, w.date)
        """)
    Integer calculateTotalDurationByUserId(@Param("userId") Long userId,
                                          @Param("dateFrom") LocalDate dateFrom,
                                          @Param("dateTo") LocalDate dateTo);

    @Query("""
        SELECT COALESCE(SUM(we.weight * we.reps * we.sets), 0.0)
        FROM WorkoutExercise we
        JOIN we.workout w
        WHERE w.user.id = :userId
        AND we.weight IS NOT NULL AND we.reps IS NOT NULL AND we.sets IS NOT NULL
        AND w.date >= COALESCE(:dateFrom, w.date)
        AND w.date <= COALESCE(:dateTo, w.date)
        """)
    Double calculateTotalWeightLiftedByUserId(@Param("userId") Long userId, 
                                               @Param("dateFrom") LocalDate dateFrom,
                                               @Param("dateTo") LocalDate dateTo);

    @Query("""
        SELECT COALESCE(MAX(w.calories), 0)
        FROM Workout w
        WHERE w.user.id = :userId
        AND w.date >= COALESCE(:dateFrom, w.date)
        AND w.date <= COALESCE(:dateTo, w.date)
        """)
    Integer calculateMaxCaloriesBurnedInWorkout(@Param("userId") Long userId,
                                               @Param("dateFrom") LocalDate dateFrom,
                                               @Param("dateTo") LocalDate dateTo);

    @Query("""
        SELECT COALESCE(MAX(w.duration), 0)
        FROM Workout w
        WHERE w.user.id = :userId
        AND w.date >= COALESCE(:dateFrom, w.date)
        AND w.date <= COALESCE(:dateTo, w.date)
        """)
    Integer calculateMaxDurationInWorkout(@Param("userId") Long userId,
                                          @Param("dateFrom") LocalDate dateFrom,
                                          @Param("dateTo") LocalDate dateTo);

    @Query("""
        SELECT w.type, COUNT(w)
        FROM Workout w
        WHERE w.user.id = :userId
        AND w.date >= COALESCE(:dateFrom, w.date)
        AND w.date <= COALESCE(:dateTo, w.date)
        GROUP BY w.type
        """)
    List<Object[]> findWorkoutsByTypeRaw(@Param("userId") Long userId,
                                      @Param("dateFrom") LocalDate dateFrom,
                                      @Param("dateTo") LocalDate dateTo);


    @Query("""
        SELECT e.name, MAX(we.weight)
        FROM WorkoutExercise we
        JOIN we.workout w
        JOIN we.exercise e
        WHERE w.user.id = :userId
        AND we.weight IS NOT NULL
        AND w.date >= COALESCE(:dateFrom, w.date)
        AND w.date <= COALESCE(:dateTo, w.date)
        GROUP BY e.name
        """)
    List<Object[]> findMaxWeightByExerciseRaw(@Param("userId") Long userId,
                                               @Param("dateFrom") LocalDate dateFrom,
                                               @Param("dateTo") LocalDate dateTo);

    @Query("""
        SELECT e.name, MAX(we.distance)
        FROM WorkoutExercise we
        JOIN we.workout w
        JOIN we.exercise e
        WHERE w.user.id = :userId
        AND we.distance IS NOT NULL
        AND w.date >= COALESCE(:dateFrom, w.date)
        AND w.date <= COALESCE(:dateTo, w.date)
        GROUP BY e.name
        """)
    List<Object[]> findMaxDistanceByExerciseRaw(@Param("userId") Long userId,
                                                 @Param("dateFrom") LocalDate dateFrom,
                                                 @Param("dateTo") LocalDate dateTo);

    @Query("""
        SELECT e.name, MAX(we.time)
        FROM WorkoutExercise we
        JOIN we.workout w
        JOIN we.exercise e
        WHERE w.user.id = :userId
        AND we.time IS NOT NULL
        AND w.date >= COALESCE(:dateFrom, w.date)
        AND w.date <= COALESCE(:dateTo, w.date)
        GROUP BY e.name
        """)
    List<Object[]> findMaxTimeByExerciseRaw(@Param("userId") Long userId,
                                            @Param("dateFrom") LocalDate dateFrom,
                                            @Param("dateTo") LocalDate dateTo);

    default Map<WorkoutType, Integer> findWorkoutsByType(Long userId, LocalDate dateFrom, LocalDate dateTo) {
        return findWorkoutsByTypeRaw(userId, dateFrom, dateTo).stream()
            .collect(Collectors.toMap(
                row -> (WorkoutType) row[0],
                row -> ((Number) row[1]).intValue()
            ));
    }
                                            
    default Map<String, Integer> findMaxWeightByExercise(Long userId, LocalDate dateFrom, LocalDate dateTo) {
        return findMaxWeightByExerciseRaw(userId, dateFrom, dateTo).stream()
            .collect(Collectors.toMap(
                row -> (String) row[0],
                row -> ((Number) row[1]).intValue()
            ));
    }

    default Map<String, Integer> findMaxDistanceByExercise(Long userId, LocalDate dateFrom, LocalDate dateTo) {
        return findMaxDistanceByExerciseRaw(userId, dateFrom, dateTo).stream()
            .collect(Collectors.toMap(
                row -> (String) row[0],
                row -> ((Number) row[1]).intValue()
            ));
    }

    default Map<String, Integer> findMaxTimeByExercise(Long userId, LocalDate dateFrom, LocalDate dateTo) {
        return findMaxTimeByExerciseRaw(userId, dateFrom, dateTo).stream()
            .collect(Collectors.toMap(
                row -> (String) row[0],
                row -> ((Number) row[1]).intValue()
            ));
    }
    
}
