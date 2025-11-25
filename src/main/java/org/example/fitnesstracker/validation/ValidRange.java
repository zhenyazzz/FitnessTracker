package org.example.fitnesstracker.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = RangeValidator.class)
public @interface ValidRange {
    String message() default "Range is invalid";
    String fromField();
    String toField();
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

}
