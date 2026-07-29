package com.materia.backend.contexts.masterdata.domain.valueObjects;

import com.materia.backend.contexts.masterdata.domain.enums.CurrencyCode;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Money Value Object
 * ReprÃ©sente un montant monÃ©taire avec sa devise
 *
 * CaractÃ©ristiques:
 * - Immuable
 * - Validation automatique
 * - OpÃ©rations sÃ©curisÃ©es (addition, soustraction)
 * - Formatage avancÃ©
 * - Type-safe
 *
 */
public class Money {

    // ============================================================
    // CONSTANTES
    // ============================================================

    public static final int DEFAULT_SCALE = 2;
    public static final RoundingMode DEFAULT_ROUNDING = RoundingMode.HALF_UP;

    // ============================================================
    // ATTRIBUTS
    // ============================================================

    private final BigDecimal amount;
    private final CurrencyCode currency;

    // ============================================================
    // CONSTRUCTEUR PRIVÃ‰
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
     * CrÃ©e un Money Ã  partir d'un montant et d'une devise
     */
    public static Money of(BigDecimal amount, CurrencyCode currency) {
        return new Money(amount, currency);
    }

    /**
     * CrÃ©e un Money Ã  partir d'un montant (String) et d'une devise
     */
    public static Money of(String amount, CurrencyCode currency) {
        return new Money(new BigDecimal(amount), currency);
    }

    /**
     * CrÃ©e un Money Ã  partir d'un montant (double) et d'une devise
     */
    public static Money of(double amount, CurrencyCode currency) {
        return new Money(BigDecimal.valueOf(amount), currency);
    }

    /**
     * CrÃ©e un Money en MAD
     */
    public static Money ofMAD(BigDecimal amount) {
        return new Money(amount, CurrencyCode.MAD);
    }

    /**
     * CrÃ©e un Money en EUR
     */
    public static Money ofEUR(BigDecimal amount) {
        return new Money(amount, CurrencyCode.EUR);
    }

    /**
     * CrÃ©e un Money en USD
     */
    public static Money ofUSD(BigDecimal amount) {
        return new Money(amount, CurrencyCode.USD);
    }

    /**
     * CrÃ©e un Money Ã  zÃ©ro dans une devise donnÃ©e
     */
    public static Money zero(CurrencyCode currency) {
        return new Money(BigDecimal.ZERO, currency);
    }

    // ============================================================
    // VALIDATION
    // ============================================================

    private void validate(BigDecimal amount, CurrencyCode currency) {
        if (amount == null) {
            throw new IllegalArgumentException("Le montant est obligatoire");
        }
        if (currency == null) {
            throw new IllegalArgumentException("La devise est obligatoire");
        }
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Le montant ne peut pas Ãªtre nÃ©gatif: " + amount
            );
        }
        // VÃ©rification des dÃ©cimales
        if (amount.scale() > currency.getDecimalPlaces()) {
            throw new IllegalArgumentException(
                    "Le montant ne peut pas avoir plus de " +
                            currency.getDecimalPlaces() + " dÃ©cimales pour la devise " +
                            currency.getCode()
            );
        }
    }

    // ============================================================
    // OPÃ‰RATIONS MÃ‰TIER
    // ============================================================

    /**
     * Additionne deux montants (mÃªme devise)
     */
    public Money add(Money other) {
        if (other == null) {
            throw new IllegalArgumentException("L'autre montant est obligatoire");
        }
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                    "Impossible d'ajouter des montants de devises diffÃ©rentes: " +
                            this.currency.getCode() + " vs " + other.currency.getCode()
            );
        }
        return new Money(this.amount.add(other.amount), this.currency);
    }

    /**
     * Soustrait deux montants (mÃªme devise)
     */
    public Money subtract(Money other) {
        if (other == null) {
            throw new IllegalArgumentException("L'autre montant est obligatoire");
        }
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                    "Impossible de soustraire des montants de devises diffÃ©rentes: " +
                            this.currency.getCode() + " vs " + other.currency.getCode()
            );
        }
        return new Money(this.amount.subtract(other.amount), this.currency);
    }

    /**
     * Multiplie par un facteur
     */
    public Money multiply(BigDecimal factor) {
        if (factor == null) {
            throw new IllegalArgumentException("Le facteur est obligatoire");
        }
        if (factor.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Le facteur ne peut pas Ãªtre nÃ©gatif");
        }
        return new Money(this.amount.multiply(factor), this.currency);
    }

    /**
     * Multiplie par un facteur (double)
     */
    public Money multiply(double factor) {
        return multiply(BigDecimal.valueOf(factor));
    }

    /**
     * Divise par un facteur
     */
    public Money divide(BigDecimal divisor) {
        if (divisor == null) {
            throw new IllegalArgumentException("Le diviseur est obligatoire");
        }
        if (divisor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Le diviseur doit Ãªtre positif");
        }
        BigDecimal result = this.amount.divide(divisor, DEFAULT_SCALE, DEFAULT_ROUNDING);
        return new Money(result, this.currency);
    }

    /**
     * Divise par un facteur (double)
     */
    public Money divide(double divisor) {
        return divide(BigDecimal.valueOf(divisor));
    }

    /**
     * Applique un pourcentage
     */
    public Money applyPercentage(BigDecimal percentage) {
        if (percentage == null) {
            throw new IllegalArgumentException("Le pourcentage est obligatoire");
        }
        BigDecimal factor = percentage.divide(new BigDecimal("100"), DEFAULT_SCALE, DEFAULT_ROUNDING);
        return multiply(factor);
    }

    /**
     * Applique un pourcentage (double)
     */
    public Money applyPercentage(double percentage) {
        return applyPercentage(BigDecimal.valueOf(percentage));
    }

    /**
     * Ajoute la TVA
     */
    public Money addTax(BigDecimal taxRate) {
        return applyPercentage(new BigDecimal("100").add(taxRate));
    }

    /**
     * Ajoute la TVA (double)
     */
    public Money addTax(double taxRate) {
        return addTax(BigDecimal.valueOf(taxRate));
    }

    // ============================================================
    // MÃ‰THODES DE COMPARAISON
    // ============================================================

    /**
     * VÃ©rifie si le montant est Ã©gal Ã  un autre (mÃªme devise)
     */
    public boolean isEqualTo(Money other) {
        if (other == null) return false;
        if (!this.currency.equals(other.currency)) return false;
        return this.amount.compareTo(other.amount) == 0;
    }

    /**
     * VÃ©rifie si le montant est supÃ©rieur Ã  un autre (mÃªme devise)
     */
    public boolean isGreaterThan(Money other) {
        if (other == null) {
            throw new IllegalArgumentException("L'autre montant est obligatoire");
        }
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Devises diffÃ©rentes");
        }
        return this.amount.compareTo(other.amount) > 0;
    }

    /**
     * VÃ©rifie si le montant est infÃ©rieur Ã  un autre (mÃªme devise)
     */
    public boolean isLessThan(Money other) {
        if (other == null) {
            throw new IllegalArgumentException("L'autre montant est obligatoire");
        }
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Devises diffÃ©rentes");
        }
        return this.amount.compareTo(other.amount) < 0;
    }

    /**
     * VÃ©rifie si le montant est zÃ©ro
     */
    public boolean isZero() {
        return amount.compareTo(BigDecimal.ZERO) == 0;
    }

    /**
     * VÃ©rifie si le montant est positif
     */
    public boolean isPositive() {
        return amount.compareTo(BigDecimal.ZERO) > 0;
    }

    // ============================================================
    // FORMATAGE
    // ============================================================

    /**
     * Formate le montant avec le symbole
     * Exemple: DH 100.50
     */
    public String format() {
        return currency.format(amount);
    }

    /**
     * Formate le montant avec le code
     * Exemple: MAD 100.50
     */
    public String formatWithCode() {
        return currency.formatWithCode(amount);
    }

    /**
     * Formate le montant sans symbole
     * Exemple: 100.50
     */
    public String formatPlain() {
        return currency.formatPlain(amount);
    }

    /**
     * Formate avec un pattern personnalisÃ©
     */
    public String format(String pattern) {
        return String.format(pattern, amount, currency.getSymbol(), currency.getCode());
    }

    // ============================================================
    // GETTERS
    // ============================================================

    public BigDecimal getAmount() {
        return amount;
    }

    public CurrencyCode getCurrency() {
        return currency;
    }

    public String getCurrencyCode() {
        return currency.getCode();
    }

    public String getCurrencySymbol() {
        return currency.getSymbol();
    }

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
    public int hashCode() {
        return Objects.hash(amount, currency);
    }

    // ============================================================
    // TOSTRING
    // ============================================================

    @Override
    public String toString() {
        return format();
    }
}
