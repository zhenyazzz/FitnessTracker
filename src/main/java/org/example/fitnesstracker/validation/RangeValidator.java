package org.example.fitnesstracker.validation;

import java.lang.reflect.Method;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;


public class RangeValidator implements ConstraintValidator<ValidRange, Object> {
    private String fromField;
    private String toField;

    @Override
    public void initialize(ValidRange constraintAnnotation) {
        this.fromField = constraintAnnotation.fromField();
        this.toField = constraintAnnotation.toField();
    }

    @Override
    public boolean isValid(Object dto, ConstraintValidatorContext context) {
        if (dto == null) {
            return true;
        }
        try {
            Object fromValue = getFieldValue(dto, fromField);
            Object toValue = getFieldValue(dto, toField);
            if (fromValue == null || toValue == null) {
                return true;
            }
            if (fromValue instanceof Number fromNum && toValue instanceof Number toNum) {
                return fromNum.doubleValue() <= toNum.doubleValue();
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private Object getFieldValue(Object object, String fieldName) throws Exception {
        Method method = object.getClass().getMethod(fieldName);
        return method.invoke(object);
    }
}
