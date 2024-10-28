package com.user.management.control.exception;

public class InvalidPasswordException extends InsecurePasswordException {

    public InvalidPasswordException() {
        super("Password should have atleast 8 characters, a number, a symbol, a capital letter, a small letter");
    }
}
