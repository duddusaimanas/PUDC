package com.user.management.control;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.user.management.entity.Portal;
import com.user.management.entity.PortalUserDetailsView;
import com.user.management.entity.UserStatus;

import com.user.management.entity.PortalStatus;
import com.user.management.entity.PortalUserDetails;

@Service
public class AdminAttendanceService extends UserAttendanceService {

    public AdminAttendanceService(PortalUserDetailsManager userDetailsManager, PortalRepository portalRepository) {
        super(userDetailsManager, portalRepository);
    }

    public void closeAttendance() {
        portalRepository.save(Portal.builder().portalStatus(PortalStatus.CLOSED).build());
    }

    public void openAttendance() {
        portalRepository.deleteAll();
        portalRepository.save(Portal.builder().portalStatus(PortalStatus.OPEN).build());
    }

    public void markAttendanceById(UUID id) {
        userDetailsManager.updateUserStatusById(id, UserStatus.PRESENT);
    }

    public void markAttendanceByName(String name) {
        userDetailsManager.updateUserStatusByName(name, UserStatus.PRESENT);
    }

    public PortalUserDetailsView getAttendanceStatusById(UUID id) {
        PortalUserDetails userDetails = userDetailsManager.findUserById(id);
        return getAttendanceStatus(userDetails);
    }

    public PortalUserDetailsView getAttendanceStatusByName(String name) {
        PortalUserDetails userDetails = userDetailsManager.findUserByName(name);
        return getAttendanceStatus(userDetails);
    }

    public List<PortalUserDetailsView> getAllAttendanceStatus() {
        return userDetailsManager.findAllUsers().stream()
                .map(this::getAttendanceStatus)
                .toList();
    }
}
