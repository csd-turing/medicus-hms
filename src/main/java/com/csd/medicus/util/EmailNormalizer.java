package com.csd.medicus.util;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Utility to normalize and validate email addresses before persistence.
 *
 * Behavior:
 * - Trims surrounding whitespace and lower-cases the email local-part/domain.
 * - Returns null for empty/whitespace input.
 * - Throws IllegalArgumentException for null input or invalid email formats.
 * - Uses a conservative, widely-used regex for email validation (covers common valid emails,
 *   but intentionally not full RFC5322 complexity).
 *
 * Rationale:
 * - Normalizing to lowercase simplifies uniqueness checks and searching.
 * - Validation catches obvious user input mistakes early (e.g., missing '@', spaces, invalid domain).
 */
public final class EmailNormalizer {

    // A conservative email regex: local@domain.tld (allows + tags and dots in local,
    // disallows starting/ending with dot or consecutive dots in local and domain).
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+" +                 // local-part first segment
            "(?:\\.[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+)*" +         // local-part dot segments
            "@" +
            "[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?" +   // domain label start
            "(?:\\.[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?)*" + // domain labels
            "\\.[A-Za-z]{2,}$"                                  // TLD
    );

    private EmailNormalizer() {
        // utility
    }

    /**
     * Normalize and validate the provided email.
     *
     * @param email input email string; null is NOT allowed and will throw IllegalArgumentException.
     * @return normalized email (trimmed and lowercased) or null if the input is empty/whitespace.
     * @throws IllegalArgumentException if email is null or fails validation.
     */
    public static String normalize(String email) {
        if (email == null) {
            throw new IllegalArgumentException("email must not be null");
        }
        String trimmed = email.trim();
        if (trimmed.isEmpty()) {
            return null;
        }

        // Lowercase entire email for canonical storage (safe in most use-cases,
        // domain-part is case-insensitive per RFC; local-part is typically treated case-insensitively).
        String candidate = trimmed.toLowerCase();

        if (!EMAIL_PATTERN.matcher(candidate).matches()) {
            throw new IllegalArgumentException("invalid email format: " + email);
        }

        // Additional safety: disallow spaces anywhere (even if regex missed)
        if (candidate.contains(" ")) {
            throw new IllegalArgumentException("invalid email (contains spaces): " + email);
        }

        return candidate;
    }
}