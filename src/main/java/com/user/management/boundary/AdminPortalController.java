package com.user.management.boundary;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.user.management.control.AdminPortalService;
import com.user.management.entity.PortalUserDetails;
import com.user.management.entity.PortalUserDetailsView;
import com.user.management.entity.UserStatus;

import lombok.RequiredArgsConstructor;

@CrossOrigin(origins = "http://localhost:5173", methods = {
        RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE })
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
@PreAuthorize("hasAnyAuthority('ADMIN')")
public class AdminPortalController {

    private final AdminPortalService adminPortalService;

    @PostMapping(value = "/create")
    public ResponseEntity<String> createPortal(@AuthenticationPrincipal PortalUserDetails userDetails) {
        return ResponseEntity.ok(adminPortalService.createPortal(userDetails));
    }

    @PostMapping(value = "{id}/reset")
    public ResponseEntity<Void> resetPortal(@PathVariable(value = "id", required = true) Long id) {
        adminPortalService.resetPortal(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping(value = "{id}/drop")
    public ResponseEntity<Void> dropPortal(@PathVariable(value = "id", required = true) Long id) {
        adminPortalService.dropPortal(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "{id}/mark/{userId}")
    public ResponseEntity<Void> markById(@PathVariable(value = "id", required = true) Long id,
            @PathVariable(value = "userId", required = true) UUID userId,
            @RequestParam(name = "status", required = true) UserStatus status) {
        adminPortalService.markById(id, userId, status);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "{id}/status/{userId}")
    public ResponseEntity<PortalUserDetailsView> getStatusById(@PathVariable(value = "id", required = true) Long id,
            @PathVariable(value = "userId", required = true) UUID userId) {
        return ResponseEntity.ok().body(adminPortalService.getStatusById(id, userId));
    }
}
