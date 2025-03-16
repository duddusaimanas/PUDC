package com.user.management.boundary;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.password.CompromisedPasswordException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.user.management.control.exception.InsecurePasswordException;
import com.user.management.control.exception.PortalUnavailableException;
import com.user.management.control.exception.UserNotFoundException;
import com.user.management.control.exception.UserSecretNotFoundException;
import com.user.management.control.exception.UsernameAlreadyExistsException;

@ControllerAdvice
public class UserManagementExceptionHandler {

    @ExceptionHandler(value = UserNotFoundException.class)
    public ResponseEntity<String> handleException(UserNotFoundException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(value = UserSecretNotFoundException.class)
    public ResponseEntity<String> handleException(UserSecretNotFoundException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(value = UsernameAlreadyExistsException.class)
    public ResponseEntity<String> handleException(UsernameAlreadyExistsException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(value = PortalUnavailableException.class)
    public ResponseEntity<String> handleException(PortalUnavailableException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(value = AccessDeniedException.class)
    public ResponseEntity<String> handleException(AccessDeniedException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(value = CompromisedPasswordException.class)
    public ResponseEntity<String> handleException(CompromisedPasswordException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(value = InsecurePasswordException.class)
    public ResponseEntity<String> handleException(InsecurePasswordException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}