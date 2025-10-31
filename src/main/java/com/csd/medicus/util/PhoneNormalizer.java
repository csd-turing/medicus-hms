package com.csd.medicus.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Utility to normalize phone numbers into E.164 format.
 *
 * Behavior:
 * - Returns phone number in E.164 format (e.g. +919123456789) or null for empty/whitespace input.
 * - Accepts international numbers that start with '+' or '00'.
 * - Accepts national numbers and prepends the default country's dialing code.
 * - Strips spaces, dashes, parentheses, and dots.
 * - Rejects input containing alphabetic characters.
 * - Validates total digits after normalization are between 8 and 15 (inclusive).
 *
 * Minimal in-repo dialing code map is provided and extensible by replacing the map copy
 * returned from getDialingCodes() if callers wish to manage it differently.
 */
public final class PhoneNormalizer {

    private static final Map<String, String> DEFAULT_DIALING_CODES;
    private static final Map<String, Integer> NATIONAL_NUMBER_LENGTH;

    static {
        Map<String, String> m = new HashMap<>();
        m.put("IN", "91");
        m.put("US", "1");
        DEFAULT_DIALING_CODES = Collections.unmodifiableMap(m);

        Map<String, Integer> l = new HashMap<>();
        l.put("IN", 10); // Indian national (subscriber) number length (typical mobile)
        l.put("US", 10); // US national (subscriber) number length (typical)
        NATIONAL_NUMBER_LENGTH = Collections.unmodifiableMap(l);
    }

    private static final Pattern ALPHA = Pattern.compile(".*[A-Za-z].*");
    private static final Pattern DIGITS_ONLY = Pattern.compile("\\d+");
    private static final Pattern STRIP = Pattern.compile("[\\s\\-\\.\\(\\)]+");

    private static final int MIN_DIGITS = 8;
    private static final int MAX_DIGITS = 15;

    private PhoneNormalizer() {
        // utility
    }

    /**
     * Normalize a phone number to E.164.
     *
     * @param phone input phone string (may contain punctuation). Null is treated as IllegalArgumentException.
     * @param defaultCountry two-letter country code (case-insensitive) to use for numbers without international prefix.
     *                       If null or empty, "IN" is assumed.
     * @return normalized E.164 string (leading '+') or null if input is empty/whitespace
     * @throws IllegalArgumentException on invalid input (alphabetic chars, unsupported country, invalid length)
     */
    public static String normalize(String phone, String defaultCountry) {
        if (phone == null) {
            throw new IllegalArgumentException("phone must not be null");
        }
        String orig = phone;
        String trimmed = phone.trim();
        if (trimmed.isEmpty()) {
            return null;
        }

        // Reject alphabetic characters
        if (ALPHA.matcher(trimmed).matches()) {
            throw new IllegalArgumentException("phone contains alphabetic characters: " + orig);
        }

        // International: + prefix
        if (trimmed.startsWith("+")) {
            String after = stripPunctuation(trimmed.substring(1));
            ensureDigitsOnly(after, orig);
            ensureValidLength(after, orig);
            return "+" + after;
        }

        // International: 00 prefix
        if (trimmed.startsWith("00")) {
            String after = stripPunctuation(trimmed.substring(2));
            ensureDigitsOnly(after, orig);
            ensureValidLength(after, orig);
            return "+" + after;
        }

        // National/local number: strip punctuation
        String digits = stripPunctuation(trimmed);

        // If a single leading zero trunk exists, remove it (common in many countries)
        if (digits.startsWith("0") && digits.length() > 1) {
            digits = digits.substring(1);
        }

        ensureDigitsOnly(digits, orig);

        String country = (defaultCountry == null || defaultCountry.trim().isEmpty())
                ? "IN"
                : defaultCountry.trim().toUpperCase();

        if (!DEFAULT_DIALING_CODES.containsKey(country)) {
            throw new IllegalArgumentException("unsupported default country: " + defaultCountry);
        }

        String dialing = DEFAULT_DIALING_CODES.get(country);

        // Decide whether to prefix the country code:
        // - If digits already look like countryCode + nationalNumber (based on known national length) and start with country code,
        //   treat as already including country code.
        // - Otherwise, prefix the country code.
        String full;
        Integer nationalLen = NATIONAL_NUMBER_LENGTH.get(country);
        if (nationalLen != null) {
            int expectedFullLen = dialing.length() + nationalLen;
            if (digits.length() == expectedFullLen && digits.startsWith(dialing)) {
                full = digits; // already in the form countryCode + nationalNumber
            } else {
                full = dialing + digits;
            }
        } else {
            // No national length info: be conservative and prefix unless digits already starts with dialing and total length within bounds
            if (digits.startsWith(dialing) && digits.length() >= MIN_DIGITS && digits.length() <= MAX_DIGITS) {
                full = digits;
            } else {
                full = dialing + digits;
            }
        }

        ensureValidLength(full, orig);

        return "+" + full;
    }

    /**
     * Convenience overload: defaultCountry = "IN".
     */
    public static String normalize(String phone) {
        return normalize(phone, "IN");
    }

    private static String stripPunctuation(String s) {
        if (s == null) return "";
        return STRIP.matcher(s).replaceAll("");
    }

    private static void ensureDigitsOnly(String s, String original) {
        if (!DIGITS_ONLY.matcher(s).matches()) {
            throw new IllegalArgumentException("invalid characters in phone: " + original);
        }
    }

    private static void ensureValidLength(String digits, String original) {
        int len = digits == null ? 0 : digits.length();
        if (len < MIN_DIGITS || len > MAX_DIGITS) {
            throw new IllegalArgumentException("phone number digits out of range (8-15): " + original);
        }
    }

    /**
     * Expose a read-only view of the embedded dialing codes.
     */
    public static Map<String, String> getDialingCodes() {
        return DEFAULT_DIALING_CODES;
    }
}