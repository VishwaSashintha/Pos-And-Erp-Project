package com.gradge.erp.shared.validation;

import java.util.ArrayList;
import java.util.List;

public class ValidationResult {

    private final List<ValidationError> errors = new ArrayList<>();

    public void addError(String field, String message) {
        errors.add(new ValidationError(field, message));
    }

    public boolean isValid() {
        return errors.isEmpty();
    }

    public List<ValidationError> getErrors() {
        return errors;
    }
}