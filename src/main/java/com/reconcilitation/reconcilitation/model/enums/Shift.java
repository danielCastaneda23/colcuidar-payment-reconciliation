package com.reconcilitation.reconcilitation.model.enums;

import java.text.Normalizer;
import java.util.Locale;

/**
 * 
 * Shift enum is in charge of parsing the raw shift which comes from csv rows
 * it is useful to have an enum as unique souce of truth and create a
 * standarized value.
 */
public enum Shift {
    DAY,
    NIGHT;

    public static Shift parse(String raw) {
        if (raw == null || raw.isBlank()) {
            // throw runtime exception
            throw new IllegalArgumentException("Shift text is empty");
        }
        // this snippet eliminates whitespaces, separate any accent character,
        // replace any accent mark for empty string, convert it into uppercase
        String cleaned = Normalizer.normalize(raw.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT);
        if (cleaned.startsWith("D")) {
            return DAY;
        }
        if (cleaned.startsWith("N")) {
            return NIGHT;
        }
        // throw runtime exception

        throw new IllegalArgumentException("Unknown shift: " + raw);
    }
}
