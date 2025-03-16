package com.user.management.config.security;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.user.management.entity.KeyDetails;
import com.user.management.entity.KeyStatus;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@EnableScheduling
@Component
@RequiredArgsConstructor
public class KeyOrchestrator {

    private final KeyRepository keyRepository;
    private final KeyBuffer keyBuffer;

    @Value("${key.orchestration.test.mode}")
    private boolean isTestModeEnabled;

    @Value("${key.orchestration.immediate.time-to-live}")
    private long timeToLiveForKeyImmediate;

    @Value("${key.orchestration.immediate.time-unit}")
    private TimeUnit timeUnitForKeyImmediate;

    private final JwtService jwtService;

    @Value("${jwt.refresh.token.time-to-live}")
    private long timeToLiveForRefreshToken;

    @Value("${key.overall.time-to-live}")
    private long timeToLiveForKeyOverall;

    @Scheduled(cron = "${key.clean.cron}")
    public void cleanExpiredKeys() {
        keyRepository.findAll().stream()
                .filter(key -> key.getExpiry().isBefore(ZonedDateTime.now()))
                .forEach(key -> {
                    keyRepository.deleteById(key.getKeyId());
                    keyRepository.save(
                            new KeyDetails(UUID.randomUUID(),
                                    jwtService.secretKey(),
                                    KeyStatus.INACTIVE,
                                    ZonedDateTime.now().plus(30, ChronoUnit.DAYS)));
                });
    }

    @Scheduled(cron = "${key.load.cron}")
    public void loadKeys() {
        var random = (int) (Math.random() * (timeToLiveForKeyOverall - timeToLiveForRefreshToken - 1)) + 1;
        keyRepository.updateKeyStatuses(KeyStatus.INACTIVE, KeyStatus.ACTIVE);
        keyRepository.findAll().stream()
                .filter(key -> {
                    var expiryMinus = key.getExpiry().minus(random, ChronoUnit.DAYS).getDayOfMonth();
                    var now = ZonedDateTime.now().getDayOfMonth();
                    return expiryMinus == now;
                })
                .forEach(key -> {
                    keyRepository.updateKeyStatusByKeyId(KeyStatus.ACTIVE, key.getKeyId());
                    keyBuffer.save(key, timeToLiveForKeyImmediate, timeUnitForKeyImmediate);
                });
    }

    @PostConstruct
    public void initKeys() {
        keyBuffer.reset();
        if (isTestModeEnabled || keyRepository.count() == 0) {
            IntStream.range(0, 31).forEach(i -> IntStream.range(0, 10).forEach(j -> keyRepository.save(
                    new KeyDetails(UUID.randomUUID(),
                            jwtService.secretKey(),
                            KeyStatus.INACTIVE,
                            ZonedDateTime.now().plus(30 - i + 1, ChronoUnit.DAYS)))));
        }
        this.loadKeys();
    }

}
