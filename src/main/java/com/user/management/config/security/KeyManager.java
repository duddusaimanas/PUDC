package com.user.management.config.security;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.user.management.entity.KeyDetails;
import com.user.management.entity.KeyStatus;
import com.user.management.entity.LocalKeyDetails;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class KeyManager {

        private final KeyRepository keyRepository;
        private final KeyBuffer keyBuffer;

        @Value("${key.session.time-to-live}")
        private long timeToLiveForKeyPerSession;

        @Value("${key.session.time-unit}")
        private TimeUnit timeUnitForKeyPerSession;

        @Value("${key.overall.time-to-live}")
        private long timeToLiveForKeyOverall;

        @Value("${key.overall.temporal-unit}")
        private ChronoUnit temporalUnitForKeyOverall;

        LocalKeyDetails fetchKey() {
                return keyBuffer.find();
        }

        LocalKeyDetails generateKey(SecretKey secretKey) {
                var keyExpiry = ZonedDateTime.now().plus(timeToLiveForKeyOverall, temporalUnitForKeyOverall);
                var repoFind = new KeyDetails(UUID.randomUUID(),
                                secretKey,
                                KeyStatus.ACTIVE,
                                keyExpiry);
                keyRepository.save(repoFind);
                return keyBuffer.saveSession(repoFind, timeToLiveForKeyPerSession, timeUnitForKeyPerSession);
        }

        Optional<LocalKeyDetails> findById(UUID keyId) {
                var bufferFind = keyBuffer.findById(keyId).orElse(null);
                if (bufferFind == null) {
                        var repoFind = keyRepository.findById(keyId).orElse(null);
                        if (repoFind == null)
                                return Optional.empty();
                        bufferFind = keyBuffer.saveSession(repoFind, timeToLiveForKeyPerSession,
                                        timeUnitForKeyPerSession);
                }
                if (bufferFind.getExpiry().isBefore(ZonedDateTime.now())) {
                        keyBuffer.deleteByKeyId(keyId);
                        return Optional.empty();
                }
                return Optional.of(bufferFind);
        }
}
