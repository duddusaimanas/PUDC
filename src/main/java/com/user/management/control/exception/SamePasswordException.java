package com.user.management.control.exception;

public class SamePasswordException extends InsecurePasswordException {

    public SamePasswordException() {
        super("New password should not be same as Old password");
    }
}
