package com.user.management.boundary;

import org.springframework.web.bind.annotation.RestController;

import com.user.management.config.security.JwtService;
import com.user.management.control.PortalUserDetailsManager;
import com.user.management.entity.PasswordChangeRequest;
import com.user.management.entity.PortalUserDetails;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequiredArgsConstructor
public class RegistrationController {

    private final PortalUserDetailsManager userDetailsManager;
    private final JwtService jwtService;

    @GetMapping
    public String index(HttpServletRequest request, @AuthenticationPrincipal PortalUserDetails userDetails) {
        return "Welcome " + userDetails.getName() + " @session:" + request.getSession().getId();
    }

    @PostMapping(value = "/register")
    public ResponseEntity<String> registerUser(@RequestBody PortalUserDetails userDetails) {
        userDetailsManager.createUser(userDetails);
        return ResponseEntity.accepted().body(String.format("Welcome new user %s!!!", userDetails.getName()));
    }

    @GetMapping(value = "/authenticate")
    public ResponseEntity<String> authenticateUser(@AuthenticationPrincipal PortalUserDetails userDetails) {
        return ResponseEntity.ok().body(jwtService.generateToken(userDetails));
    }

    @GetMapping(value = "/exists")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<String> userExists(@RequestParam("username") String username) {
        if (!StringUtils.hasText(username))
            return ResponseEntity.badRequest().build();
        return ResponseEntity.ok().body(String.format("User '%s' %s", username.trim(),
                userDetailsManager.userExists(username.trim()) ? "exists" : "does not exist"));
    }

    @PostMapping(value = "/updatePassword")
    public ResponseEntity<Void> updatePassword(@RequestBody PasswordChangeRequest userDetails) {
        if (!StringUtils.hasText(userDetails.getNewPassword()) || !StringUtils.hasText(userDetails.getUsername())
                || !StringUtils.hasText(userDetails.getPassword()))
            return ResponseEntity.badRequest().build();
        userDetailsManager.changePassword(userDetails, userDetails.getNewPassword().trim());
        return ResponseEntity.noContent().build();
    }

    @PutMapping(value = "/changeName")
    public ResponseEntity<Void> changeName(@AuthenticationPrincipal PortalUserDetails userDetails,
            @RequestParam("oldName") String oldName,
            @RequestParam("newName") String newName) {
        if (!StringUtils.hasText(oldName) || !StringUtils.hasText(newName))
            return ResponseEntity.badRequest().build();
        userDetailsManager.changeName(userDetails, oldName.trim(), newName.trim());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping(value = "/delete")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<Void> deleteUser(@RequestParam("username") String username) {
        if (!StringUtils.hasText(username))
            return ResponseEntity.badRequest().build();
        userDetailsManager.deleteUser(username.trim());
        return ResponseEntity.noContent().build();
    }

}
