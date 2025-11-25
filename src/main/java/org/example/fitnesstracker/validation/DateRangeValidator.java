package org.example.fitnesstracker.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.example.fitnesstracker.dto.request.DateFilterDto;

public class DateRangeValidator implements ConstraintValidator<ValidDateRange, DateFilterDto> {

    @Override
    public boolean isValid(DateFilterDto dto, ConstraintValidatorContext context) {
        if (dto == null) {
            return true;
        }

        if (dto.dateFrom() == null || dto.dateTo() == null) {
            return true;
        }
        
        return !dto.dateFrom().isAfter(dto.dateTo());

    }

}
    