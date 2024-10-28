package com.user.management.boundary;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.user.management.control.AdminAttendanceService;
import com.user.management.entity.PortalUserDetailsView;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
@PreAuthorize("hasAnyAuthority('ADMIN')")
public class AdminAttendanceController {

    private final AdminAttendanceService adminAttendanceService;

    @PostMapping(value = "/close")
    public ResponseEntity<Void> closeAttendance() {
        adminAttendanceService.closeAttendance();
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/open")
    public ResponseEntity<Void> openAttendance() {
        adminAttendanceService.openAttendance();
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "mark/{id}")
    public ResponseEntity<Void> markAttendanceById(@PathVariable(value = "id", required = true) UUID id) {
        adminAttendanceService.markAttendanceById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "markByName/{name}")
    public ResponseEntity<Void> markAttendanceByName(
            @PathVariable(value = "name", required = true) @Valid @NotBlank String name) {
        adminAttendanceService.markAttendanceByName(name.trim());
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "status/{id}")
    public ResponseEntity<PortalUserDetailsView> getAttendanceStatusById(@PathVariable(value = "id") UUID id) {
        if (id == null)
            return ResponseEntity.badRequest().build();
        return ResponseEntity.ok().body(adminAttendanceService.getAttendanceStatusById(id));
    }

    @GetMapping(value = "statusByName/{name}")
    public ResponseEntity<PortalUserDetailsView> getAttendanceStatusByName(@PathVariable(value = "name") String name) {
        if (!StringUtils.hasText(name))
            return ResponseEntity.badRequest().build();
        return ResponseEntity.ok().body(adminAttendanceService.getAttendanceStatusByName(name.trim()));
    }

    @GetMapping(value = "/status")
    public ResponseEntity<List<PortalUserDetailsView>> getAllAttendanceStatus() {
        return ResponseEntity.ok().body(adminAttendanceService.getAllAttendanceStatus());
    }
}
