package com.user.management.boundary;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RestController;

import com.user.management.control.UserAttendanceService;
import com.user.management.entity.PortalUserDetails;
import com.user.management.entity.PortalUserDetailsView;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequiredArgsConstructor
public class UserAttendanceController {

    private final UserAttendanceService userAttendanceService;

    @PostMapping(value = "/mark")
    public ResponseEntity<Void> markAttendanceById(@AuthenticationPrincipal PortalUserDetails userDetails) {
        userAttendanceService.markAttendance(userDetails);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "/status")
    public ResponseEntity<PortalUserDetailsView> getAttendanceStatus(
            @AuthenticationPrincipal PortalUserDetails userDetails) {
        return ResponseEntity.ok().body(userAttendanceService.getAttendanceStatus(userDetails));
    }
}
