package com.materia.backend.contexts.purchaseRequisition.domain.valueObjects;

import java.util.Objects;
import java.util.regex.Pattern;

public class RequisitionCode {

    private static final String PATTERN_STRING = "^(?i)(?:REQ|PR)-(?:[0-9]{4}-)?[0-9]{4}$";
    private static final Pattern PATTERN = Pattern.compile(PATTERN_STRING);
    private static final String DEFAULT_PREFIX = "REQ";

    private final String value;

    private RequisitionCode(String value) {
        validate(value);
        this.value = value.trim();
    }

    public static RequisitionCode of(String value) {
        return new RequisitionCode(value);
    }

    public static RequisitionCode fromPrefixAndNumber(String prefix, int number) {
        return fromPrefixYearAndNumber(prefix, java.time.Year.now().getValue(), number);
    }

    public static RequisitionCode fromPrefixYearAndNumber(String prefix, int year, int number) {
        if (prefix == null || prefix.trim().isEmpty()) {
            throw new IllegalArgumentException("Prefix is required");
        }
        if (number < 0 || number > 9999) {
            throw new IllegalArgumentException("Number must be between 0 and 9999");
        }
        String id = prefix.trim().toUpperCase() + "-" + year + "-" + String.format("%04d", number);
        return new RequisitionCode(id);
    }

    public static RequisitionCode createDefault() {
        return fromPrefixAndNumber(DEFAULT_PREFIX, 1);
    }

    public static RequisitionCode generateNext(String current) {
        return generateNext(RequisitionCode.of(current));
    }

    public static RequisitionCode generateNext(RequisitionCode current) {
        int number = current.getNumberAsInt() + 1;
        int year = current.getYear();
        if (year != -1) {
            return fromPrefixYearAndNumber(current.getPrefix(), year, number);
        }
        return fromPrefixAndNumber(current.getPrefix(), number);
    }

    public static boolean isValid(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        return PATTERN.matcher(value.trim()).matches();
    }

    private void validate(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Requisition code is required");
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Requisition code cannot be empty");
        }
        if (!PATTERN.matcher(trimmed).matches()) {
            throw new IllegalArgumentException(
                    "Invalid requisition code format: '" + value +
                            "'. Expected format: REQ-YYYY-0001 (e.g. REQ-2026-0001) or legacy REQ-0001"
            );
        }
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

    public RequisitionCode increment() {
        return generateNext(this);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RequisitionCode that = (RequisitionCode) o;
        return Objects.equals(value, that.value);
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
