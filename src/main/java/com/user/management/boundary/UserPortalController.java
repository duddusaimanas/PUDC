package com.user.management.boundary;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RestController;

import com.user.management.control.UserPortalService;
import com.user.management.entity.PortalUserDetails;
import com.user.management.entity.PortalUserDetailsView;
import com.user.management.entity.UserStatus;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequiredArgsConstructor
public class UserPortalController {

    private final UserPortalService userPortalService;

    @PostMapping(value = "{id}/mark")
    public ResponseEntity<Void> mark(@PathVariable(name = "id", required = true) Long id,
            @AuthenticationPrincipal PortalUserDetails userDetails,
            @RequestParam(name = "status", required = true) UserStatus status) {
        userPortalService.mark(id, userDetails, status);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "status")
    public ResponseEntity<PortalUserDetailsView> status(@RequestParam(name = "id", required = false) Long id,
            @AuthenticationPrincipal PortalUserDetails userDetails) {
        return ResponseEntity.ok(userPortalService.getStatus(id, userDetails));
    }

    @PostMapping(value = "{id}/enroll")
    public ResponseEntity<Void> enroll(@PathVariable(name = "id", required = true) Long id,
            @AuthenticationPrincipal PortalUserDetails userDetails) {
        userPortalService.enroll(id, userDetails);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "{id}")
    public ResponseEntity<Boolean> portal(@PathVariable(name = "id", required = true) Long id) {
        return ResponseEntity.ok(userPortalService.portal(id));
    }

    @GetMapping(value = "{id}/status")
    public ResponseEntity<List<PortalUserDetailsView>> getStatusByPortal(
            @PathVariable(value = "id", required = true) Long id) {
        return ResponseEntity.ok().body(userPortalService.getStatusByPortal(id));
    }

    @GetMapping(value = "statuses")
    public ResponseEntity<List<PortalUserDetailsView>> getStatuses() {
        return ResponseEntity.ok().body(userPortalService.getStatuses());
    }
}
