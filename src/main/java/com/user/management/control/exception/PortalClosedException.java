package com.user.management.control.exception;

public class PortalClosedException extends RuntimeException {

    public PortalClosedException() {
        super("Attendance portal is closed");
    }
}
