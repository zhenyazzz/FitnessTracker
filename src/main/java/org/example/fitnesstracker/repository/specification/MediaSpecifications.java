package org.example.fitnesstracker.repository.specification;

import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

import org.example.fitnesstracker.dto.request.media.MediaFilterDto;
import org.example.fitnesstracker.model.Media;
import org.example.fitnesstracker.security.SecurityUtils;

public class MediaSpecifications {
    public static Specification<Media> belongsToUser(Long userId) {
        return (root, query, criteriaBuilder) -> 
            userId == null ? null : criteriaBuilder.equal(root.get("user").get("id"), userId);
    }

    public static Specification<Media> hasDateFrom(LocalDate dateFrom) {
        return (root, query, criteriaBuilder) -> 
            dateFrom == null ? null : criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), dateFrom.atStartOfDay());
    }

    public static Specification<Media> hasDateTo(LocalDate dateTo) {
        return (root, query, criteriaBuilder) -> 
            dateTo == null ? null : criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), dateTo.atStartOfDay());
    }

    public static Specification<Media> buildSpecification(MediaFilterDto filter) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        Specification<Media> specification = belongsToUser(currentUserId);
        
        if (filter != null && filter.dateFilter() != null) {
            specification = specification
                .and(hasDateFrom(filter.dateFilter().dateFrom()))
                .and(hasDateTo(filter.dateFilter().dateTo()));
        }
        return specification;
    }
}
