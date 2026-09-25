package com.materia.backend.contexts.purchaseOrder.domain.valueObjects;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Minimal purchase order business code value object.
 */
public final class OrderCode {

    private static final Pattern PATTERN = Pattern.compile("^(?i)PO-(?:[0-9]{4}-)?[0-9]{4}$");
    private static final String DEFAULT_PREFIX = "PO";

    private final String value;

    private OrderCode(String value) {
        validate(value);
        this.value = value.trim();
    }

    public static OrderCode of(String value) {
        return new OrderCode(value);
    }

    public static OrderCode fromPrefixAndNumber(String prefix, int number) {
        return fromPrefixYearAndNumber(prefix, java.time.Year.now().getValue(), number);
    }

    public static OrderCode fromPrefixYearAndNumber(String prefix, int year, int number) {
        if (prefix == null || prefix.trim().isEmpty()) {
            throw new IllegalArgumentException("Prefix is required");
        }
        if (number < 0 || number > 9999) {
            throw new IllegalArgumentException("Number must be between 0 and 9999");
        }
        return new OrderCode(prefix.trim().toUpperCase() + "-" + year + "-" + String.format("%04d", number));
    }

    public static OrderCode createDefault() {
        return fromPrefixAndNumber(DEFAULT_PREFIX, 1);
    }

    public static boolean isValid(String value) {
        return value != null && PATTERN.matcher(value.trim()).matches();
    }

    private void validate(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Order code is required");
        }
        if (!PATTERN.matcher(value.trim()).matches()) {
            throw new IllegalArgumentException(
                    "Invalid order code format: '" + value + "'. Expected format: PO-YYYY-0001 (e.g. PO-2026-0001) or legacy PO-0001"
            );
        }
    }

    public String getValue() {
        return value;
    }

    public String getPrefix() {
        int hyphenIndex = value.indexOf('-');
        return hyphenIndex != -1 ? value.substring(0, hyphenIndex) : value;
    }

    public String getNumber() {
        int hyphenIndex = value.lastIndexOf('-');
        return hyphenIndex != -1 ? value.substring(hyphenIndex + 1) : value;
    }

    public int getNumberAsInt() {
        return Integer.parseInt(getNumber());
    }

    public int getYear() {
        String[] parts = value.split("-");
        if (parts.length == 3) {
            try {
                return Integer.parseInt(parts[1]);
            } catch (NumberFormatException e) {
                return -1;
            }
        }
        return -1;
    }

    public OrderCode increment() {
        int year = getYear();
        if (year != -1) {
            return fromPrefixYearAndNumber(getPrefix(), year, getNumberAsInt() + 1);
        }
        return fromPrefixAndNumber(getPrefix(), getNumberAsInt() + 1);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderCode orderCode)) return false;
        return Objects.equals(value, orderCode.value);
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
