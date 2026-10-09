package com.materia.backend.contexts.settings.domain;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * 🔹 APPLICATION SETTINGS
 *
 * Every setting the Settings page manages: its key, group, label, kind and validation. A setting with no stored value
 * uses its default; the invoice price tolerance defaults to app.invoice.price-tolerance-percent.
 */
public enum SettingKey {

    COMPANY_NAME("company.name", "Company", "Company name", "Shown on documents sent to suppliers.", Kind.TEXT, 200, "Materia"),
    COMPANY_ADDRESS("company.address", "Company", "Address", "Postal address printed on purchase orders.", Kind.TEXT, 500, ""),
    COMPANY_TAX_ID("company.taxId", "Company", "ICE", "Moroccan common company identifier (15 digits).", Kind.TAX_ID, 15, ""),
    COMPANY_EMAIL("company.email", "Company", "E-mail", "Contact address for suppliers.", Kind.EMAIL, 200, ""),
    COMPANY_PHONE("company.phone", "Company", "Phone", "Contact number for suppliers.", Kind.PHONE, 50, ""),
    INVOICE_PRICE_TOLERANCE("invoice.priceTolerancePercent", "Invoice matching", "Price tolerance (%)",
            "How far an invoiced unit price may be from the order price before verification is blocked (0 to 50).",
            Kind.PERCENT, 10, null);

    public enum Kind { TEXT, EMAIL, PHONE, TAX_ID, PERCENT }

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final Pattern PHONE = Pattern.compile("^[+0-9 ()./-]+$");
    private static final Pattern TAX_ID = Pattern.compile("^\\d{15}$");
    private static final BigDecimal MAX_PERCENT = new BigDecimal("50");

    private final String key;
    private final String group;
    private final String label;
    private final String description;
    private final Kind kind;
    private final int maxLength;
    private final String defaultValue;

    SettingKey(String key, String group, String label, String description, Kind kind, int maxLength, String defaultValue) {
        this.key = key;
        this.group = group;
        this.label = label;
        this.description = description;
        this.kind = kind;
        this.maxLength = maxLength;
        this.defaultValue = defaultValue;
    }

    public static Optional<SettingKey> of(String key) {
        return Arrays.stream(values()).filter(s -> s.key.equals(key)).findFirst();
    }

    /** The value to store (trimmed, empty as ""), or an exception saying why it is refused. */
    public String normalize(String raw) {
        String value = raw == null ? "" : raw.trim();
        if (value.length() > maxLength) {
            throw new IllegalArgumentException(label + " must be at most " + maxLength + " characters");
        }
        if (value.isEmpty()) {
            if (kind == Kind.PERCENT) throw new IllegalArgumentException(label + " is required");
            return "";
        }
        switch (kind) {
            case EMAIL -> {
                if (!EMAIL.matcher(value).matches()) throw new IllegalArgumentException(label + " is not a valid e-mail address");
            }
            case PHONE -> {
                if (!PHONE.matcher(value).matches()) throw new IllegalArgumentException(label + " may only contain digits, spaces and + ( ) . / -");
            }
            case TAX_ID -> {
                if (!TAX_ID.matcher(value).matches()) throw new IllegalArgumentException(label + " must be 15 digits");
            }
            case PERCENT -> {
                BigDecimal percent;
                try {
                    percent = new BigDecimal(value.replace(',', '.'));
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(label + " must be a number");
                }
                if (percent.signum() < 0 || percent.compareTo(MAX_PERCENT) > 0) {
                    throw new IllegalArgumentException(label + " must be between 0 and 50");
                }
                return percent.stripTrailingZeros().toPlainString();
            }
            default -> {
            }
        }
        return value;
    }

    public String key() { return key; }
    public String group() { return group; }
    public String label() { return label; }
    public String description() { return description; }
    public Kind kind() { return kind; }
    public int maxLength() { return maxLength; }
    public String defaultValue() { return defaultValue; }
}
