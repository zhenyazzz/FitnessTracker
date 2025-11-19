package org.example.fitnesstracker.repository;

import org.springframework.stereotype.Repository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.example.fitnesstracker.model.Media;
import java.util.Optional;

@Repository
public interface MediaRepository extends JpaRepository<Media, Long> {
    Optional<Media> findByIdAndUserId(Long id, Long userId);

    Page<Media> findAll(Specification<Media> specification, PageRequest of);
}
