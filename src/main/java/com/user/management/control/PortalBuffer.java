package com.user.management.control;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.user.management.entity.EnrolledDetails;
import com.user.management.entity.LocalPortalDetails;
import com.user.management.entity.PortalUserDetails;
import com.user.management.entity.UserStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PortalBuffer {

    private final RedisTemplate<String, LocalPortalDetails> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String LOCAL_PORTAL_DETAILS = "LocalPortalDetails:";

    Optional<LocalPortalDetails> findById(Long id) {
        var entry = read(redisTemplate.opsForHash().entries(LOCAL_PORTAL_DETAILS + id), LocalPortalDetails.class);
        if (entry == null || entry.getId() == null)
            return Optional.empty();
        return Optional.of(entry);
    }

    Optional<EnrolledDetails> findByUserId(Long id, UUID userId) {
        var portalById = findById(id);
        if (portalById.isPresent()) {
            return portalById.get().getEnrolled().stream()
                    .filter(enrolled -> enrolled.getId().equals(userId))
                    .findFirst();
        }
        return Optional.empty();
    }

    void saveUser(Long id, EnrolledDetails enrolledDetails) {
        LocalPortalDetails portalDetails = read(redisTemplate.opsForHash().entries(LOCAL_PORTAL_DETAILS + id),
                LocalPortalDetails.class);
        List<EnrolledDetails> updatedEnrolled = new ArrayList<>(portalDetails.getEnrolled());
        var containsId = updatedEnrolled.stream()
                .anyMatch(enrolled -> enrolled.getId().equals(enrolledDetails.getId()));
        if (containsId) {
            updatedEnrolled
                    .forEach(enrolled -> {
                        if (enrolled.getId().equals(enrolledDetails.getId())) {
                            enrolled.setStatus(enrolledDetails.getStatus());
                        }
                    });
        } else {
            updatedEnrolled.add(enrolledDetails);
        }
        redisTemplate.opsForHash().put(LOCAL_PORTAL_DETAILS + id, "enrolled", mapList(updatedEnrolled));
    }

    void save(Long id, PortalUserDetails userDetails, long timeToLive, TimeUnit timeUnit) {
        save(new LocalPortalDetails(
                id,
                List.of(EnrolledDetails.builder().id(userDetails.getId()).name(userDetails.getName())
                        .status(UserStatus.ABSENT).build()),
                ZonedDateTime.now().plus(timeToLive, timeUnit.toChronoUnit())), timeToLive, timeUnit);
    }

    void save(LocalPortalDetails portalDetails, long timeToLive, TimeUnit timeUnit) {
        redisTemplate.opsForHash().putAll(LOCAL_PORTAL_DETAILS + portalDetails.getId(), map(portalDetails));
        redisTemplate.expire(LOCAL_PORTAL_DETAILS + portalDetails.getId(), timeToLive, timeUnit);
    }

    void deleteById(Long id) {
        redisTemplate.delete(LOCAL_PORTAL_DETAILS + id);
    }

    private <T> Map<Object, Object> map(T any) {
        return objectMapper.convertValue(any, new TypeReference<Map<Object, Object>>() {
        });
    }

    private <T> List<Map<Object, Object>> mapList(T any) {
        return objectMapper.convertValue(any, new TypeReference<List<Map<Object, Object>>>() {
        });
    }

    private <T> T read(Map<Object, Object> any, Class<T> clazz) {
        return objectMapper.convertValue(any, clazz);
    }
}
