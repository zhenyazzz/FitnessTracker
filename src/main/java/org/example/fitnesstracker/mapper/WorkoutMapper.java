package org.example.fitnesstracker.mapper;

import org.example.fitnesstracker.dto.request.workouts.CreateWorkoutRequest;
import org.example.fitnesstracker.dto.request.workouts.CreateWorkoutExerciseRequest;
import org.example.fitnesstracker.dto.request.workouts.UpdateWorkoutRequest;
import org.example.fitnesstracker.dto.request.workouts.UpdateWorkoutExerciseRequest;
import org.example.fitnesstracker.dto.response.workouts.WorkoutResponse;
import org.example.fitnesstracker.dto.response.workouts.WorkoutExerciseResponse;
import org.example.fitnesstracker.model.Workout;
import org.example.fitnesstracker.model.WorkoutExercise;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.BeanMapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import java.util.List;

@Mapper(componentModel = "spring")
public interface WorkoutMapper {
    
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "workoutExercises", ignore = true)
    Workout toEntity(CreateWorkoutRequest request);
    
    

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "workout", ignore = true)
    @Mapping(target = "exercise", ignore = true)
    WorkoutExercise toEntity(CreateWorkoutExerciseRequest request);

    List<WorkoutExercise> toEntityList(List<CreateWorkoutExerciseRequest> requests);
    
    
    
    @Mapping(target = "exercises", source = "workoutExercises")
    WorkoutResponse toResponse(Workout workout);

    List<WorkoutResponse> toResponseList(List<Workout> workouts);
    
    
    @Mapping(target = "exerciseId", source = "exercise.id")
    @Mapping(target = "exerciseName", source = "exercise.name")
    @Mapping(target = "muscleGroup", source = "exercise.muscleGroup")
    WorkoutExerciseResponse toExerciseResponse(WorkoutExercise workoutExercise);
    
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "workoutExercises", ignore = true)
    void updateEntityFromRequest(UpdateWorkoutRequest request, @MappingTarget Workout workout);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "workout", ignore = true)
    @Mapping(target = "exercise", ignore = true)
    void updateExerciseFromRequest(UpdateWorkoutExerciseRequest request, @MappingTarget WorkoutExercise workoutExercise);
    

}
