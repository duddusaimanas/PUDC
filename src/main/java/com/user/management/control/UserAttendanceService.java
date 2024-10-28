package com.user.management.control;

import org.springframework.stereotype.Service;

import com.user.management.control.exception.PortalClosedException;
import com.user.management.entity.PortalStatus;
import com.user.management.entity.PortalUserDetails;
import com.user.management.entity.PortalUserDetailsView;
import com.user.management.entity.UserStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserAttendanceService {

    protected final PortalUserDetailsManager userDetailsManager;
    protected final PortalRepository portalRepository;

    public void markAttendance(PortalUserDetails userDetails) {
        throwIfPortalIsClosed();
        userDetailsManager.updateUserStatusById(userDetails.getId(), UserStatus.PRESENT);
    }

    public PortalUserDetailsView getAttendanceStatus(PortalUserDetails userDetails) {
        return PortalUserDetailsView.builder()
                .id(userDetails.getId())
                .name(userDetails.getName())
                .status(userDetails.getStatus())
                .build();
    }

    private void throwIfPortalIsClosed() {
        if (portalRepository.findById(PortalStatus.OPEN).isEmpty())
            throw new PortalClosedException();
    }
}
