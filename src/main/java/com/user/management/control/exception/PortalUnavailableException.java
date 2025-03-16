package com.user.management.control.exception;

public class PortalUnavailableException extends RuntimeException {

    public PortalUnavailableException() {
        super("Attendance portal is closed");
    }
}
