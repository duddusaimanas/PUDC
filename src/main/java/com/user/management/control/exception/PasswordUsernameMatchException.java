package com.user.management.control.exception;

public class PasswordUsernameMatchException extends InsecurePasswordException {

    public PasswordUsernameMatchException() {
        super("Password should not match with Username");
    }
}
