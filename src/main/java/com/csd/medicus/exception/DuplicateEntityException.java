package com.csd.medicus.exception;

/**
 * Thrown when an attempt is made to create an entity that would violate a uniqueness constraint
 * (for example, a patient with an email or phone that already exists).
 */
public class DuplicateEntityException extends RuntimeException {
    public DuplicateEntityException(String message) {
        super(message);
    }

    public DuplicateEntityException(String message, Throwable cause) {
        super(message, cause);
    }
}