package com.gradge.erp.shared.validation;

public record ValidationError(
        String field,
        String message
) {
}