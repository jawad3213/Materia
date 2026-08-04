package com.materia.backend.contexts.purchaseRequisition.domain.valueObjects;

import java.util.Objects;
import java.util.regex.Pattern;

public class RequisitionCode {

    private static final String PATTERN_STRING = "^REQ-[0-9]{4}$";
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
        if (prefix == null || prefix.trim().isEmpty()) {
            throw new IllegalArgumentException("Prefix is required");
        }
        if (number < 0 || number > 9999) {
            throw new IllegalArgumentException("Number must be between 0 and 9999");
        }
        String id = prefix.trim().toUpperCase() + "-" + String.format("%04d", number);
        return new RequisitionCode(id);
    }

    public static RequisitionCode createDefault() {
        return new RequisitionCode(DEFAULT_PREFIX + "-0001");
    }

    public static RequisitionCode generateNext(String current) {
        RequisitionCode id = RequisitionCode.of(current);
        int number = id.getNumberAsInt() + 1;
        return fromPrefixAndNumber(id.getPrefix(), number);
    }

    public static RequisitionCode generateNext(RequisitionCode current) {
        int number = current.getNumberAsInt() + 1;
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
                            "'. Expected format: REQ-0000 (for example: REQ-0001)"
            );
        }
    }

    public String getPrefix() {
        return value.substring(0, 3);
    }

    public String getNumber() {
        return value.substring(4);
    }

    public int getNumberAsInt() {
        return Integer.parseInt(getNumber());
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
