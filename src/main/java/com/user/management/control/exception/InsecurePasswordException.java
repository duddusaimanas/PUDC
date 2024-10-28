package com.user.management.control.exception;

public class InsecurePasswordException extends RuntimeException {

    public InsecurePasswordException(String cause) {
        super(cause);
    }
}
