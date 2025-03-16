package com.user.management.control.exception;

public class UserSecretNotFoundException extends RuntimeException {

    public UserSecretNotFoundException(String field, Object value) {
        super(String.format("No user secret with %s matching '%s' exists", field, value));
    }
}
