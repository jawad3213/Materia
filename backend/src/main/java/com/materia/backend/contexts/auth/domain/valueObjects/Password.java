package com.materia.backend.contexts.auth.domain.valueObjects;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * 🔹 PASSWORD VALUE OBJECT
 * 
 * Encapsulates password strength validation and hashed/raw value representations.
 */
public class Password {
    
    private static final int MIN_LENGTH = 8;
    private static final Pattern UPPERCASE = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE = Pattern.compile("[a-z]");
    private static final Pattern DIGIT = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL = Pattern.compile("[!@#$%^&*(),.?\":{}|<>]");
    
    private final String hashedValue;
    private final boolean isHashed;
    
    private Password(String value, boolean isHashed) {
        if (isHashed) {
            this.hashedValue = value;
            this.isHashed = true;
        } else {
            validate(value);
            this.hashedValue = value;
            this.isHashed = false;
        }
    }
    
    public static Password fromRaw(String rawPassword) { 
        return new Password(rawPassword, false); 
    }
    
    public static Password fromHash(String hashedPassword) { 
        return new Password(hashedPassword, true); 
    }
    
    private void validate(String value) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("Le mot de passe est obligatoire");
        }
        if (value.length() < MIN_LENGTH) {
            throw new IllegalArgumentException("Le mot de passe doit contenir au moins " + MIN_LENGTH + " caractères");
        }
        if (!UPPERCASE.matcher(value).find()) {
            throw new IllegalArgumentException("Le mot de passe doit contenir au moins une majuscule");
        }
        if (!LOWERCASE.matcher(value).find()) {
            throw new IllegalArgumentException("Le mot de passe doit contenir au moins une minuscule");
        }
        if (!DIGIT.matcher(value).find()) {
            throw new IllegalArgumentException("Le mot de passe doit contenir au moins un chiffre");
        }
        if (!SPECIAL.matcher(value).find()) {
            throw new IllegalArgumentException("Le mot de passe doit contenir au moins un caractère spécial");
        }
    }
    
    public String getHashedValue() { 
        return hashedValue; 
    }
    
    public boolean isHashed() { 
        return isHashed; 
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Password password = (Password) o;
        return Objects.equals(hashedValue, password.hashedValue);
    }
    
    @Override
    public int hashCode() { 
        return Objects.hash(hashedValue); 
    }
    
    @Override
    public String toString() { 
        return "********"; 
    }
}
