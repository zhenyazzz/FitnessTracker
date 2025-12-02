package org.example.fitnesstracker.repository.specification;

import java.time.LocalDate;

import org.example.fitnesstracker.dto.request.analytics.AnalyticsRequest;
import org.example.fitnesstracker.dto.request.workouts.WorkoutFilterDto;
import org.example.fitnesstracker.model.Workout;
import org.example.fitnesstracker.model.enums.WorkoutType;
import org.example.fitnesstracker.security.SecurityUtils;
import org.springframework.data.jpa.domain.Specification;

public class WorkoutSpecifications {

    public static Specification<Workout> belongsToUser(Long userId) {
        return (root, query, criteriaBuilder) -> 
            userId == null ? null : criteriaBuilder.equal(root.get("user").get("id"), userId);
    }

    public static Specification<Workout> hasType(WorkoutType type) {
        return (root, query, criteriaBuilder) -> 
            type == null ? null : criteriaBuilder.equal(root.get("type"), type);
    }

    public static Specification<Workout> hasDateFrom(LocalDate dateFrom) {
        return (root, query, criteriaBuilder) -> 
            dateFrom == null ? null : criteriaBuilder.greaterThanOrEqualTo(root.get("date"), dateFrom);
    }

    public static Specification<Workout> hasDateTo(LocalDate dateTo) {
        return (root, query, criteriaBuilder) -> 
            dateTo == null ? null : criteriaBuilder.lessThanOrEqualTo(root.get("date"), dateTo);
    }

    public static Specification<Workout> hasDurationFrom(Integer durationFrom) {
        return (root, query, criteriaBuilder) -> 
            durationFrom == null ? null : criteriaBuilder.greaterThanOrEqualTo(root.get("duration"), durationFrom);
    }

    public static Specification<Workout> hasDurationTo(Integer durationTo) {
        return (root, query, criteriaBuilder) -> 
            durationTo == null ? null : criteriaBuilder.lessThanOrEqualTo(root.get("duration"), durationTo);
    }

    public static Specification<Workout> hasCaloriesFrom(Integer caloriesFrom) {
        return (root, query, criteriaBuilder) -> 
            caloriesFrom == null ? null : criteriaBuilder.greaterThanOrEqualTo(root.get("calories"), caloriesFrom);
    }

    public static Specification<Workout> hasCaloriesTo(Integer caloriesTo) {
        return (root, query, criteriaBuilder) -> 
            caloriesTo == null ? null : criteriaBuilder.lessThanOrEqualTo(root.get("calories"), caloriesTo);
    }

    public static Specification<Workout> buildSpecification(WorkoutFilterDto filter) {
        Long currentUserId = SecurityUtils.getCurrentUserId();

        Specification<Workout> specification = belongsToUser(currentUserId);
        
        if (filter != null) {
            if (filter.type() != null) {
                specification = specification.and(hasType(filter.type()));
            }
            
            if (filter.dateFilter() != null) {
                specification = specification
                    .and(hasDateFrom(filter.dateFilter().dateFrom()))
                    .and(hasDateTo(filter.dateFilter().dateTo()));
            }
            
            if (filter.durationFilter() != null) {
                specification = specification
                    .and(hasDurationFrom(filter.durationFilter().durationFrom()))
                    .and(hasDurationTo(filter.durationFilter().durationTo()));
            }
            
            if (filter.caloriesFilter() != null) {
                specification = specification
                    .and(hasCaloriesFrom(filter.caloriesFilter().caloriesFrom()))
                    .and(hasCaloriesTo(filter.caloriesFilter().caloriesTo()));
            }
        }
        
        return specification;
    }

    public static Specification<Workout> buildWorkoutsInPeriodSpecification(AnalyticsRequest request) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        Specification<Workout> periodSpec = belongsToUser(currentUserId);
        if (request != null && request.dateFilter() != null) {
            periodSpec = periodSpec
                .and(hasDateFrom(request.dateFilter().dateFrom()))
                .and(hasDateTo(request.dateFilter().dateTo()));
        }
        return periodSpec;
    }
}
