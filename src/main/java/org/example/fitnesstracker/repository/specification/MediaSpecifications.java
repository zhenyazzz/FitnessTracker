package org.example.fitnesstracker.repository.specification;

import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

import org.example.fitnesstracker.model.Media;

public class MediaSpecifications {
    public static Specification<Media> belongsToUser(Long userId) {
        return (root, query, criteriaBuilder) -> 
            userId == null ? null : criteriaBuilder.equal(root.get("user").get("id"), userId);
    }

    public static Specification<Media> hasDateFrom(LocalDate dateFrom) {
        return (root, query, criteriaBuilder) -> 
            dateFrom == null ? null : criteriaBuilder.greaterThanOrEqualTo(root.get("date"), dateFrom);
    }

    public static Specification<Media> hasDateTo(LocalDate dateTo) {
        return (root, query, criteriaBuilder) -> 
            dateTo == null ? null : criteriaBuilder.lessThanOrEqualTo(root.get("date"), dateTo);
    }
}
