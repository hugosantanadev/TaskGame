package com.gasmtask.shared.validation;

import java.nio.charset.StandardCharsets;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class Utf8MaxBytesValidator implements ConstraintValidator<Utf8MaxBytes, String> {

    private int maxBytes;

    @Override
    public void initialize(Utf8MaxBytes annotation) {
        this.maxBytes = annotation.value();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value == null || value.getBytes(StandardCharsets.UTF_8).length <= maxBytes;
    }
}
