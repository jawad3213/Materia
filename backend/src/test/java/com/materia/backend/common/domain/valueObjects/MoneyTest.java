package com.materia.backend.common.domain.valueObjects;

import com.materia.backend.common.domain.enums.CurrencyCode;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class MoneyTest {

    /**
     * These cases state the correct behaviour and are expected to pass once the defect is
     * resolved. Money validates decimal places on the raw result before rounding it, so any
     * fractional multiply, percentage, or VAT calculation on a non-round amount throws.
     */
    private static final String FINDING_008 =
            "FINDING-008: fractional arithmetic on ordinary prices throws instead of rounding "
                    + "- see specs/001-backend-module-tests/research.md";

    private static Money mad(String amount) {
        return Money.of(amount, CurrencyCode.MAD);
    }

    // ---- Construction ----

    @Test
    @DisplayName("of: amounts are normalised to two decimal places")
    void of_normalisesScale() {
        assertEquals(new BigDecimal("10.00"), mad("10").getAmount());
        assertEquals(new BigDecimal("10.50"), mad("10.5").getAmount());
    }

    @Test
    @DisplayName("of: a negative amount is refused")
    void of_negativeAmount_isRefused() {
        assertThrows(IllegalArgumentException.class, () -> mad("-0.01"));
    }

    @Test
    @DisplayName("of: an amount with more decimal places than the currency allows is refused")
    void of_excessDecimalPlaces_isRefused() {
        assertThrows(IllegalArgumentException.class, () -> mad("10.005"));
    }

    @Test
    @DisplayName("of: trailing zeros beyond the currency's decimal places are accepted")
    void of_trailingZeros_areAccepted() {
        assertEquals(new BigDecimal("10.50"), mad("10.5000").getAmount());
    }

    @Test
    @DisplayName("of: a missing amount or currency is refused")
    void of_missingParts_areRefused() {
        assertThrows(IllegalArgumentException.class, () -> Money.of((BigDecimal) null, CurrencyCode.MAD));
        assertThrows(IllegalArgumentException.class, () -> Money.of(BigDecimal.ONE, null));
    }

    @Test
    @DisplayName("zero: produces a zero amount in the requested currency")
    void zero_isZero() {
        Money zero = Money.zero(CurrencyCode.EUR);
        assertTrue(zero.isZero());
        assertEquals(CurrencyCode.EUR, zero.getCurrency());
    }

    // ---- Addition and subtraction ----

    @Test
    @DisplayName("add: sums two amounts in the same currency")
    void add_sameCurrency_sums() {
        assertEquals(mad("30.75"), mad("10.25").add(mad("20.50")));
    }

    @Test
    @DisplayName("add: amounts in different currencies cannot be combined")
    void add_differentCurrency_isRefused() {
        Money eur = Money.of("5", CurrencyCode.EUR);
        assertThrows(IllegalArgumentException.class, () -> mad("10").add(eur));
    }

    @Test
    @DisplayName("subtract: takes one amount from another in the same currency")
    void subtract_sameCurrency_differences() {
        assertEquals(mad("5.25"), mad("10.50").subtract(mad("5.25")));
    }

    @Test
    @DisplayName("subtract: a result below zero is refused, since money cannot be negative")
    void subtract_belowZero_isRefused() {
        assertThrows(IllegalArgumentException.class, () -> mad("5").subtract(mad("10")));
    }

    @Test
    @DisplayName("subtract: amounts in different currencies cannot be combined")
    void subtract_differentCurrency_isRefused() {
        Money usd = Money.of("1", CurrencyCode.USD);
        assertThrows(IllegalArgumentException.class, () -> mad("10").subtract(usd));
    }

    // ---- Multiplication ----

    @Test
    @DisplayName("multiply: an integer quantity multiplies exactly, as used for line totals")
    void multiply_byInteger_isExact() {
        assertEquals(mad("30.03"), mad("10.01").multiply(3));
    }

    @Test
    @DisplayName("multiply: a negative factor is refused")
    void multiply_negativeFactor_isRefused() {
        assertThrows(IllegalArgumentException.class, () -> mad("10").multiply(-1));
    }

    @Test
    @Disabled(FINDING_008)
    @DisplayName("multiply: a fractional factor rounds half-up to the currency's decimal places")
    void multiply_byFraction_roundsHalfUp() {
        // 10.01 x 1.5 = 15.015 -> 15.02
        assertEquals(mad("15.02"), mad("10.01").multiply(new BigDecimal("1.5")));
    }

    // ---- Division ----

    @Test
    @DisplayName("divide: rounds half-up to two decimal places")
    void divide_roundsHalfUp() {
        // 10.00 / 3 = 3.333... -> 3.33
        assertEquals(mad("3.33"), mad("10").divide(new BigDecimal("3")));
    }

    @Test
    @DisplayName("divide: a zero or negative divisor is refused")
    void divide_nonPositiveDivisor_isRefused() {
        assertThrows(IllegalArgumentException.class, () -> mad("10").divide(BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class, () -> mad("10").divide(new BigDecimal("-2")));
    }

    // ---- Percentages and tax ----

    @Test
    @DisplayName("applyPercentage: takes a percentage of a round amount")
    void applyPercentage_roundAmount() {
        assertEquals(mad("20.00"), mad("100").applyPercentage(20));
    }

    @Test
    @Disabled(FINDING_008)
    @DisplayName("applyPercentage: rounds half-up when the result has more decimal places than the currency")
    void applyPercentage_unroundAmount_roundsHalfUp() {
        // 10.01 x 20% = 2.002 -> 2.00
        assertEquals(mad("2.00"), mad("10.01").applyPercentage(20));
    }

    @Test
    @DisplayName("addTax: adds VAT to a round amount")
    void addTax_roundAmount() {
        assertEquals(mad("120.00"), mad("100").addTax(20));
    }

    @Test
    @Disabled(FINDING_008)
    @DisplayName("addTax: adds VAT to an ordinary price and rounds half-up")
    void addTax_ordinaryPrice_roundsHalfUp() {
        // 10.01 x 1.20 = 12.012 -> 12.01
        assertEquals(mad("12.01"), mad("10.01").addTax(20));
    }

    // ---- Comparison ----

    @Test
    @DisplayName("comparison: greater and less reflect amount order within one currency")
    void comparison_withinCurrency() {
        assertTrue(mad("10").isGreaterThan(mad("5")));
        assertTrue(mad("5").isLessThan(mad("10")));
        assertTrue(mad("10").isEqualTo(mad("10.00")));
    }

    @Test
    @DisplayName("comparison: ordering across currencies is refused rather than guessed")
    void comparison_acrossCurrencies_isRefused() {
        Money eur = Money.of("5", CurrencyCode.EUR);
        assertThrows(IllegalArgumentException.class, () -> mad("10").isGreaterThan(eur));
        assertThrows(IllegalArgumentException.class, () -> mad("10").isLessThan(eur));
    }

    @Test
    @DisplayName("isEqualTo: amounts in different currencies are never equal")
    void isEqualTo_differentCurrency_isFalse() {
        assertFalse(mad("10").isEqualTo(Money.of("10", CurrencyCode.EUR)));
    }

    // ---- Value semantics ----

    @Test
    @DisplayName("equality: equal amounts in the same currency are equal and hash identically")
    void equality_isValueBased() {
        Money a = mad("10.5");
        Money b = mad("10.50");
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    @DisplayName("immutability: operations return a new value and leave the original untouched")
    void operations_areImmutable() {
        Money original = mad("10");
        original.add(mad("5"));
        assertEquals(mad("10"), original);
    }
}
