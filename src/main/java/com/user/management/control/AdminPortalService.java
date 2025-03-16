package com.user.management.control;

import java.security.SecureRandom;
import java.time.ZonedDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.user.management.entity.PortalUserDetailsView;
import com.user.management.entity.UserStatus;

import com.user.management.entity.LocalPortalDetails;
import com.user.management.entity.PortalUserDetails;

@Service
public class AdminPortalService extends UserPortalService {

    public AdminPortalService(PortalUserDetailsManager userDetailsManager, PortalBuffer portalBuffer) {
        super(userDetailsManager, portalBuffer);
    }

    @Value("${portal.time-to-live}")
    private long timeToLiveForPortal;

    @Value("${portal.temporal-unit}")
    private TimeUnit timeUnitForPortal;

    public String createPortal(PortalUserDetails userDetails) {
        var randomize = new SecureRandom();
        long min = 1_000_000_000_000L;
        long max = 999_999_999_999_99L;
        var random = min + (long) (randomize.nextDouble() * (max - min + 1));
        portalBuffer.save(random, userDetails, timeToLiveForPortal, timeUnitForPortal);
        return String.valueOf(random);
    }

    public void resetPortal(Long id) {
        var portalDetailsById = portalBuffer.findById(id);
        if (portalDetailsById.isPresent()) {
            LocalPortalDetails portalDetails = portalDetailsById.get();
            portalDetails.getEnrolled().forEach(e -> e.setStatus(UserStatus.ABSENT));
            portalDetails.setTimeout(ZonedDateTime.now().plus(timeToLiveForPortal, timeUnitForPortal.toChronoUnit()));
            portalBuffer.save(portalDetails, timeToLiveForPortal, timeUnitForPortal);
        }
    }

    public void dropPortal(Long id) {
        portalBuffer.deleteById(id);
    }

    public void markById(Long id, UUID userId, UserStatus status) {
        var enrolledDetailsById = portalBuffer.findByUserId(id, userId);
        if (enrolledDetailsById.isPresent()) {
            var updatedEnrolledDetails = enrolledDetailsById.get();
            updatedEnrolledDetails.setStatus(status);
            portalBuffer.saveUser(id, updatedEnrolledDetails);
        }
    }

    public PortalUserDetailsView getStatusById(Long id, UUID userId) {
        PortalUserDetails userDetails = userDetailsManager.findUserById(userId);
        return getStatus(id, userDetails);
    }
}
