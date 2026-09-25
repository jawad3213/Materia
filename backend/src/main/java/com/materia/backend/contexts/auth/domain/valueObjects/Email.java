package com.materia.backend.contexts.auth.domain.valueObjects;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * 🔹 EMAIL VALUE OBJECT
 * 
 * Immutable value object encapsulating email formatting and validation rules.
 */
public class Email {
    
    private static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    private static final Pattern PATTERN = Pattern.compile(EMAIL_PATTERN);
    
    private final String value;
    
    private Email(String value) {
        validate(value);
        this.value = value.toLowerCase().trim();
    }
    
    public static Email of(String value) { 
        return new Email(value); 
    }
    
    private void validate(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("L'email est obligatoire");
        }
        if (!PATTERN.matcher(value.trim()).matches()) {
            throw new IllegalArgumentException("Format d'email invalide: " + value);
        }
    }
    
    public String getValue() { 
        return value; 
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Email email = (Email) o;
        return Objects.equals(value, email.value);
    }
    
    @Override
    public int hashCode() { 
        return Objects.hash(value); 
    }
    
    @Override
    public String toString() { 
        return value; 
    }
}
