package org.example.fitnesstracker.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Index;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Column;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import org.hibernate.annotations.CreationTimestamp;

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
@Table(name = "body_measurements", indexes = {
    @Index(name = "idx_body_measurement_date", columnList = "date")
})
public class BodyMeasurement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @NotNull(message = "Date is required")
    @PastOrPresent(message = "Date cannot be in the future")
    @Column(nullable = false)
    private LocalDate date;

    @DecimalMin(value = "0.0", message = "Height must be positive")
    private Double height;

    @DecimalMin(value = "0.0", message = "Weight must be positive")
    private Double weight;

    @DecimalMin(value = "0.0", message = "Chest must be positive")
    private Double chest;

    @DecimalMin(value = "0.0", message = "Shoulders must be positive")
    private Double shoulders;

    @DecimalMin(value = "0.0", message = "Waist must be positive")
    private Double waist;

    @DecimalMin(value = "0.0", message = "Hip must be positive")
    private Double hip;

    @DecimalMin(value = "0.0", message = "Bicep must be positive")
    private Double bicep;

    @DecimalMin(value = "0.0", message = "Thigh must be positive")
    private Double thigh;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
