package com.demo.identity_service.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

//Build một validator để xử lý business logic
public class DobValidator implements ConstraintValidator<DobConstraint, LocalDate> {

    private int min;

    //Tổ hợp phím Ctrl + Home để implements methods
    @Override
    public boolean isValid(LocalDate value, ConstraintValidatorContext context) {
        if(Objects.isNull(value)) return true;

        long years = ChronoUnit.YEARS.between(value, LocalDate.now());

        return years >= min;
    }

    @Override
    public void initialize(DobConstraint constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
        min = constraintAnnotation.min();
    }
}
