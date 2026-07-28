package com.materia.backend.common.application;

import com.materia.backend.common.application.exceptions.ValidationException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.regex.Pattern;

/**
 * Base Validator class
 * Provides validation utilities and common validation methods
 * All validators should extend this class
 */
public abstract class BaseValidator {

    // Common regex patterns
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^\\+?[0-9]{10,15}$");

    private static final Pattern UUID_PATTERN =
            Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

    // --- Null/Empty Checks ---

    protected void validateNotNull(Object value, String fieldName) {
        if (value == null) {
            throw new ValidationException(fieldName + " cannot be null");
        }
    }

    protected void validateNotBlank(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(fieldName + " cannot be blank");
        }
    }

    protected void validateNotEmpty(Collection<?> collection, String fieldName) {
        if (collection == null || collection.isEmpty()) {
            throw new ValidationException(fieldName + " cannot be empty");
        }
    }

    protected void validateNotEmpty(Object[] array, String fieldName) {
        if (array == null || array.length == 0) {
            throw new ValidationException(fieldName + " cannot be empty");
        }
    }

    // --- Numeric Validations ---

    protected void validatePositive(Number value, String fieldName) {
        if (value == null || value.doubleValue() <= 0) {
            throw new ValidationException(fieldName + " must be positive");
        }
    }

    protected void validateNonNegative(Number value, String fieldName) {
        if (value == null || value.doubleValue() < 0) {
            throw new ValidationException(fieldName + " cannot be negative");
        }
    }

    protected void validateRange(Number value, Number min, Number max, String fieldName) {
        if (value != null) {
            double val = value.doubleValue();
            double minVal = min.doubleValue();
            double maxVal = max.doubleValue();
            if (val < minVal || val > maxVal) {
                throw new ValidationException(
                        String.format("%s must be between %s and %s", fieldName, min, max)
                );
            }
        }
    }

    protected void validateMin(Number value, Number min, String fieldName) {
        if (value != null && value.doubleValue() < min.doubleValue()) {
            throw new ValidationException(
                    String.format("%s must be at least %s", fieldName, min)
            );
        }
    }

    protected void validateMax(Number value, Number max, String fieldName) {
        if (value != null && value.doubleValue() > max.doubleValue()) {
            throw new ValidationException(
                    String.format("%s must be at most %s", fieldName, max)
            );
        }
    }

    // --- String Validations ---

    protected void validateLength(String value, int min, int max, String fieldName) {
        if (value != null) {
            int length = value.length();
            if (length < min || length > max) {
                throw new ValidationException(
                        String.format("%s length must be between %d and %d characters", fieldName, min, max)
                );
            }
        }
    }

    protected void validateEmail(String email) {
        if (email != null && !EMAIL_PATTERN.matcher(email).matches()) {
            throw new ValidationException("Invalid email format");
        }
    }

    protected void validatePhone(String phone) {
        if (phone != null && !PHONE_PATTERN.matcher(phone).matches()) {
            throw new ValidationException("Invalid phone number format");
        }
    }

    protected void validateUuid(String uuid) {
        if (uuid != null && !UUID_PATTERN.matcher(uuid).matches()) {
            throw new ValidationException("Invalid UUID format");
        }
    }

    // --- Date/Time Validations ---

    protected void validateDateInPast(LocalDate date, String fieldName) {
        if (date != null && date.isAfter(LocalDate.now())) {
            throw new ValidationException(fieldName + " must be in the past");
        }
    }

    protected void validateDateInFuture(LocalDate date, String fieldName) {
        if (date != null && date.isBefore(LocalDate.now())) {
            throw new ValidationException(fieldName + " must be in the future");
        }
    }

    protected void validateDateTimeInPast(LocalDateTime datetime, String fieldName) {
        if (datetime != null && datetime.isAfter(LocalDateTime.now())) {
            throw new ValidationException(fieldName + " must be in the past");
        }
    }

    protected void validateDateTimeInFuture(LocalDateTime datetime, String fieldName) {
        if (datetime != null && datetime.isBefore(LocalDateTime.now())) {
            throw new ValidationException(fieldName + " must be in the future");
        }
    }

    protected void validateDateRange(LocalDate start, LocalDate end, String fieldName) {
        if (start != null && end != null && start.isAfter(end)) {
            throw new ValidationException(
                    String.format("%s start date must be before end date", fieldName)
            );
        }
    }

    // --- Enum Validations ---

    protected <T extends Enum<T>> void validateEnumValue(String value, Class<T> enumClass, String fieldName) {
        if (value != null) {
            try {
                Enum.valueOf(enumClass, value.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new ValidationException(
                        String.format("%s must be one of: %s", fieldName,
                                String.join(", ", getEnumNames(enumClass)))
                );
            }
        }
    }

    protected <T extends Enum<T>> String[] getEnumNames(Class<T> enumClass) {
        T[] constants = enumClass.getEnumConstants();
        String[] names = new String[constants.length];
        for (int i = 0; i < constants.length; i++) {
            names[i] = constants[i].name();
        }
        return names;
    }

    // --- Custom Validations ---

    protected void validateCondition(boolean condition, String message) {
        if (!condition) {
            throw new ValidationException(message);
        }
    }

    protected void validateCondition(boolean condition, String message, Object... args) {
        if (!condition) {
            throw new ValidationException(String.format(message, args));
        }
    }

    // --- Business Rule Validations ---

    protected void validateBusinessRule(boolean condition, String message) {
        if (!condition) {
            throw new com.materia.backend.common.application.exceptions.BusinessException(message);
        }
    }
}