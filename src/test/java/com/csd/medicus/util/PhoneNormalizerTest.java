package com.csd.medicus.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PhoneNormalizer: covers valid international forms, national forms (IN/US),
 * punctuation handling, empty input, alphabetic rejection, length bounds and unsupported country.
 */
class PhoneNormalizerTest {

    @Test
    void testPlusPrefixedInternational() {
        assertEquals("+14155552671", PhoneNormalizer.normalize("+14155552671"));
    }

    @Test
    void testDoubleZeroPrefixedInternational() {
        assertEquals("+14155552671", PhoneNormalizer.normalize("0014155552671"));
    }

    @Test
    void testInMobileWithoutPrefix() {
        assertEquals("+919123456789", PhoneNormalizer.normalize("9123456789"));
    }

    @Test
    void testInMobileWithLeadingZero() {
        assertEquals("+919123456789", PhoneNormalizer.normalize("09123456789"));
    }

    @Test
    void testInWithSpacesAndPunctuation() {
        assertEquals("+919123456789", PhoneNormalizer.normalize(" 91 234-567.89 "));
    }

    @Test
    void testInAlreadyWithCountryCode() {
        assertEquals("+919123456789", PhoneNormalizer.normalize("919123456789"));
    }

    @Test
    void testUsNationalWithDefaultCountry() {
        assertEquals("+14155552671", PhoneNormalizer.normalize("4155552671", "US"));
    }

    @Test
    void testEmptyReturnsNull() {
        assertNull(PhoneNormalizer.normalize("   "));
    }

    @Test
    void testInvalidLettersRaise() {
        assertThrows(IllegalArgumentException.class, () -> PhoneNormalizer.normalize("abc123"));
        assertThrows(IllegalArgumentException.class, () -> PhoneNormalizer.normalize("123-abc-7890"));
        assertThrows(IllegalArgumentException.class, () -> PhoneNormalizer.normalize("+1-800-CALLNOW"));
    }

    @Test
    void testTooShortRejected() {
        assertThrows(IllegalArgumentException.class, () -> PhoneNormalizer.normalize("12", "US"));
    }

    @Test
    void testTooLongRejected() {
        String longNum = "1".repeat(20);
        assertThrows(IllegalArgumentException.class, () -> PhoneNormalizer.normalize(longNum, "US"));
    }

    @Test
    void testUnsupportedDefaultCountry() {
        assertThrows(IllegalArgumentException.class, () -> PhoneNormalizer.normalize("12345678", "ZZ"));
    }
}