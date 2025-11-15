package org.example.fitnesstracker.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Index;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "media", indexes = {
    @Index(name = "idx_media_created_at", columnList = "created_at")
})
public class ProgressMedia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @NotBlank(message = "Photo path is required")
    @Column(nullable = false)
    private String path;

    @Size(max = 500, message = "Note cannot exceed 500 characters")
    @Column(length = 500)
    private String note;

    private Long fileSize; // in bytes

    @Size(max = 100, message = "MIME type cannot exceed 100 characters")
    @Column(length = 100)
    private String mimeType; // e.g., "image/jpeg", "image/png"

    @CreationTimestamp
    private LocalDateTime createdAt;
}

