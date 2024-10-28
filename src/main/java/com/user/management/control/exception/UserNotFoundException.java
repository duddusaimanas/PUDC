package com.user.management.control.exception;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String field, Object value) {
        super(String.format("No user with %s matching '%s' exists", field, value));
    }
}
