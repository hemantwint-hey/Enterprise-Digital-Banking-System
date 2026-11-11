package org.example.enterprisedigitalbankingsystem.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.time.Period;

/**
 * Jakarta Bean Validation validator enforcing {@link MinimumAge} against today's date.
 */
public class MinimumAgeValidator implements ConstraintValidator<MinimumAge, LocalDate> {

    private int minimumAge;

    @Override
    public void initialize(MinimumAge constraintAnnotation) {
        this.minimumAge = constraintAnnotation.value();
    }

    @Override
    public boolean isValid(LocalDate dob, ConstraintValidatorContext context) {
        // Leave nullability to @NotNull so missing fields report one concise error.
        if (dob == null) {
            return true;
        }
        LocalDate today = LocalDate.now();
        if (dob.isAfter(today)) {
            return false;
        }
        return Period.between(dob, today).getYears() >= minimumAge;
    }
}

