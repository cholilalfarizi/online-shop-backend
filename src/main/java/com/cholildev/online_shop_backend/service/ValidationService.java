package com.cholildev.online_shop_backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ValidationService {

    @Autowired
    private Validator validator;

    public String validate(Object request) {
        Set<ConstraintViolation<Object>> constraintViolations = validator.validate(request);
        if (!constraintViolations.isEmpty()) {
            String errorMessage = extractMessages(constraintViolations);
            throw new ConstraintViolationException(errorMessage, constraintViolations);
        }
        return null;
    }

    private String extractMessages(Set<ConstraintViolation<Object>> constraintViolations) {
        return constraintViolations.stream()
                .map(violation -> violation.getPropertyPath() + " " + violation.getMessage())
                .collect(Collectors.joining(", "));
    }
}