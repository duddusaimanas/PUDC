package com.user.management.control;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.user.management.control.exception.PortalUnavailableException;
import com.user.management.entity.EnrolledDetails;
import com.user.management.entity.PortalPermissions;
import com.user.management.entity.PortalUserDetails;
import com.user.management.entity.PortalUserDetailsView;
import com.user.management.entity.UserStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserPortalService {

    protected final PortalUserDetailsManager userDetailsManager;
    protected final PortalBuffer portalBuffer;

    public void mark(Long id, PortalUserDetails userDetails, UserStatus status) {
        throwIfPortalIsNotAvailable(id);
        var enrolledDetailsByPortalId = portalBuffer.findByUserId(id, userDetails.getId());
        if (enrolledDetailsByPortalId.isPresent()) {
            var updatedEnrolledDetails = enrolledDetailsByPortalId.get();
            updatedEnrolledDetails.setStatus(status);
            portalBuffer.saveUser(id, updatedEnrolledDetails);
        }
    }

    public PortalUserDetailsView getStatus(Long id, PortalUserDetails userDetails) {
        Optional<EnrolledDetails> enrolledDetailsById = Optional.empty();
        if (id != null) {
            enrolledDetailsById = portalBuffer.findByUserId(id, userDetails.getId());
        }
        return PortalUserDetailsView.builder().id(userDetails.getId()).username(userDetails.getUsername())
                .name(userDetails.getName())
                .status(enrolledDetailsById.isPresent() ? enrolledDetailsById.get().getStatus() : null)
                .isAdmin(userDetails.getPermissions().equals(PortalPermissions.ADMIN))
                .build();
    }

    public void enroll(Long id, PortalUserDetails userDetails) {
        throwIfPortalIsNotAvailable(id);
        var newEnrolledDetails = EnrolledDetails.builder()
                .id(userDetails.getId())
                .name(userDetails.getName())
                .status(UserStatus.ABSENT)
                .build();
        portalBuffer.saveUser(id, newEnrolledDetails);
    }

    public boolean portal(Long id) {
        return portalBuffer.findById(id).isPresent();
    }

    public List<PortalUserDetailsView> getStatuses() {
        return userDetailsManager.findAllUsers().stream()
                .map(userDetails -> PortalUserDetailsView.builder()
                        .id(userDetails.getId())
                        .name(userDetails.getName())
                        .isAdmin(userDetails.getPermissions().equals(PortalPermissions.ADMIN))
                        .build())
                .toList();
    }

    public List<PortalUserDetailsView> getStatusByPortal(Long id) {
        var portalDetailsById = portalBuffer.findById(id);
        var allPortalUsers = userDetailsManager.findAllUsers();
        if (portalDetailsById.isPresent()) {
            var enrolledIds = portalDetailsById.get().getEnrolled().stream().map(EnrolledDetails::getId).toList();
            return allPortalUsers.stream()
                    .filter(userDetails -> enrolledIds.contains(userDetails.getId()))
                    .map(userDetails -> PortalUserDetailsView.builder()
                            .id(userDetails.getId())
                            .username(userDetails.getUsername())
                            .name(userDetails.getName())
                            .status(portalDetailsById.get().getEnrolled().stream()
                                    .filter(enrolled -> Objects.equals(enrolled.getId(), userDetails.getId()))
                                    .map(EnrolledDetails::getStatus)
                                    .filter(Objects::nonNull)
                                    .findFirst().orElse(null))
                            .isAdmin(userDetails.getPermissions().equals(PortalPermissions.ADMIN))
                            .build())
                    .toList();
        }
        return Collections.emptyList();
    }

    private void throwIfPortalIsNotAvailable(Long id) {
        if (!portal(id))
            throw new PortalUnavailableException();
    }
}
