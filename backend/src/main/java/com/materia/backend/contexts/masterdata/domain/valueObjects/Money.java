package com.materia.backend.contexts.masterData.domain.valueObjects;

import com.materia.backend.contexts.masterData.domain.enums.CurrencyCode;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Money Value Object
 * Represents a monetary amount with its currency
 *
 * Characteristics:
 * - Immutable
 * - Automatic validation
 * - Safe operations (addition, subtraction)
 * - Advanced formatting
 * - Type-safe
 */
public class Money {

    // ============================================================
    // CONSTANTS
    // ============================================================

    public static final int DEFAULT_SCALE = 2;
    public static final RoundingMode DEFAULT_ROUNDING = RoundingMode.HALF_UP;

    // ============================================================
    // ATTRIBUTES
    // ============================================================

    private final BigDecimal amount;
    private final CurrencyCode currency;

    // ============================================================
    // PRIVATE CONSTRUCTOR
    // ============================================================

    private Money(BigDecimal amount, CurrencyCode currency) {
        validate(amount, currency);
        this.amount = amount.setScale(DEFAULT_SCALE, DEFAULT_ROUNDING);
        this.currency = currency;
    }

    // ============================================================
    // FACTORY METHODS
    // ============================================================

    /**
     * Creates a Money from an amount and a currency
     */
    public static Money of(BigDecimal amount, CurrencyCode currency) {
        return new Money(amount, currency);
    }

    /**
     * Creates a Money with zero amount
     */
    public static Money zero(CurrencyCode currency) {
        return new Money(BigDecimal.ZERO, currency);
    }

    /**
     * Creates a Money with zero amount from a currency string
     */
    public static Money zero(String currencyCode) {
        return new Money(BigDecimal.ZERO, CurrencyCode.valueOf(currencyCode));
    }

    /**
     * Creates a Money from an amount (String) and a currency
     */
    public static Money of(String amount, CurrencyCode currency) {
        return new Money(new BigDecimal(amount), currency);
    }

    /**
     * Creates a Money from an amount (double) and a currency
     */
    public static Money of(double amount, CurrencyCode currency) {
        return new Money(BigDecimal.valueOf(amount), currency);
    }

    /**
     * Creates a Money in MAD
     */
    public static Money ofMAD(BigDecimal amount) {
        return new Money(amount, CurrencyCode.MAD);
    }

    /**
     * Creates a Money in EUR
     */
    public static Money ofEUR(BigDecimal amount) {
        return new Money(amount, CurrencyCode.EUR);
    }

    /**
     * Creates a Money in USD
     */
    public static Money ofUSD(BigDecimal amount) {
        return new Money(amount, CurrencyCode.USD);
    }

    // ============================================================
    // VALIDATION
    // ============================================================

    private void validate(BigDecimal amount, CurrencyCode currency) {
        if (amount == null) {
            throw new IllegalArgumentException("Amount is required");
        }
        if (currency == null) {
            throw new IllegalArgumentException("Currency is required");
        }
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount cannot be negative: " + amount);
        }
        // Decimal places check
        int actualScale = amount.stripTrailingZeros().scale();
        if (actualScale > currency.getDecimalPlaces()) {
            throw new IllegalArgumentException(
                    "Amount cannot have more than " +
                            currency.getDecimalPlaces() + " decimal places for currency " +
                            currency.getCode()
            );
        }
    }

    // ============================================================
    // BUSINESS OPERATIONS
    // ============================================================

    /**
     * Adds two amounts (same currency)
     */
    public Money add(Money other) {
        if (other == null) {
            throw new IllegalArgumentException("The other amount is required");
        }
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                    "Cannot add amounts of different currencies: " +
                            this.currency.getCode() + " vs " + other.currency.getCode()
            );
        }
        return new Money(this.amount.add(other.amount), this.currency);
    }

    /**
     * Subtracts two amounts (same currency)
     */
    public Money subtract(Money other) {
        if (other == null) {
            throw new IllegalArgumentException("The other amount is required");
        }
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                    "Cannot subtract amounts of different currencies: " +
                            this.currency.getCode() + " vs " + other.currency.getCode()
            );
        }
        return new Money(this.amount.subtract(other.amount), this.currency);
    }

    /**
     * Multiplies by a factor
     */
    public Money multiply(BigDecimal factor) {
        if (factor == null) {
            throw new IllegalArgumentException("Factor is required");
        }
        if (factor.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Factor cannot be negative");
        }
        return new Money(this.amount.multiply(factor), this.currency);
    }

    /**
     * Multiplies by a factor (double)
     */
    public Money multiply(double factor) {
        return multiply(BigDecimal.valueOf(factor));
    }

    /**
     * Multiplies by an integer factor
     */
    public Money multiply(int factor) {
        return multiply(BigDecimal.valueOf(factor));
    }

    /**
     * Divides by a divisor
     */
    public Money divide(BigDecimal divisor) {
        if (divisor == null) {
            throw new IllegalArgumentException("Divisor is required");
        }
        if (divisor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Divisor must be positive");
        }
        BigDecimal result = this.amount.divide(divisor, DEFAULT_SCALE, DEFAULT_ROUNDING);
        return new Money(result, this.currency);
    }

    /**
     * Divides by a divisor (double)
     */
    public Money divide(double divisor) {
        return divide(BigDecimal.valueOf(divisor));
    }

    /**
     * Applies a percentage
     */
    public Money applyPercentage(BigDecimal percentage) {
        if (percentage == null) {
            throw new IllegalArgumentException("Percentage is required");
        }
        BigDecimal factor = percentage.divide(new BigDecimal("100"), DEFAULT_SCALE, DEFAULT_ROUNDING);
        return multiply(factor);
    }

    /**
     * Applies a percentage (double)
     */
    public Money applyPercentage(double percentage) {
        return applyPercentage(BigDecimal.valueOf(percentage));
    }

    /**
     * Adds VAT (tax)
     */
    public Money addTax(BigDecimal taxRate) {
        return applyPercentage(new BigDecimal("100").add(taxRate));
    }

    /**
     * Adds VAT (double)
     */
    public Money addTax(double taxRate) {
        return addTax(BigDecimal.valueOf(taxRate));
    }

    // ============================================================
    // COMPARISON METHODS
    // ============================================================

    /**
     * Checks if the amount is equal to another (same currency)
     */
    public boolean isEqualTo(Money other) {
        if (other == null) return false;
        if (!this.currency.equals(other.currency)) return false;
        return this.amount.compareTo(other.amount) == 0;
    }

    /**
     * Checks if the amount is greater than another (same currency)
     */
    public boolean isGreaterThan(Money other) {
        if (other == null) { throw new IllegalArgumentException("The other amount is required"); }
        if (!this.currency.equals(other.currency)) { throw new IllegalArgumentException("Different currencies"); }
        return this.amount.compareTo(other.amount) > 0;
    }

    /**
     * Checks if the amount is less than another (same currency)
     */
    public boolean isLessThan(Money other) {
        if (other == null) { throw new IllegalArgumentException("The other amount is required"); }
        if (!this.currency.equals(other.currency)) { throw new IllegalArgumentException("Different currencies"); }
        return this.amount.compareTo(other.amount) < 0;
    }

    /**
     * Checks if the amount is zero
     */
    public boolean isZero() { return amount.compareTo(BigDecimal.ZERO) == 0; }

    /**
     * Checks if the amount is positive
     */
    public boolean isPositive() { return amount.compareTo(BigDecimal.ZERO) > 0; }

    // ============================================================
    // FORMATTING
    // ============================================================

    /**
     * Formats the amount with the symbol
     * Example: DH 100.50
     */
    public String format() { return currency.format(amount); }

    /**
     * Formats the amount with the code
     * Example: MAD 100.50
     */
    public String formatWithCode() { return currency.formatWithCode(amount); }

    /**
     * Formats the amount without symbol
     * Example: 100.50
     */
    public String formatPlain() { return currency.formatPlain(amount); }

    /**
     * Formats with a custom pattern
     */
    public String format(String pattern) {
        return String.format(pattern, amount, currency.getSymbol(), currency.getCode());
    }

    // ============================================================
    // GETTERS
    // ============================================================

    public BigDecimal getAmount() { return amount; }
    public CurrencyCode getCurrency() { return currency; }
    public String getCurrencyCode() { return currency.getCode(); }
    public String getCurrencySymbol() { return currency.getSymbol(); }

    // ============================================================
    // EQUALS & HASHCODE
    // ============================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Money money = (Money) o;
        return amount.compareTo(money.amount) == 0 &&
                Objects.equals(currency, money.currency);
    }

    @Override
    public int hashCode() { return Objects.hash(amount, currency); }

    // ============================================================
    // TOSTRING
    // ============================================================

    @Override
    public String toString() { return format(); }
}
