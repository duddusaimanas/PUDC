package com.user.management.config.security;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.user.management.entity.KeyDetails;
import com.user.management.entity.LocalKeyDetails;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class KeyBuffer {

    private final RedisTemplate<String, LocalKeyDetails> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String LOCAL_KEY_DETAILS = "LocalKeyDetails:";
    private static final String LOCAL_KEY_DETAILS_TEMP = "LocalKeyDetailsTemp:";

    LocalKeyDetails find() {
        var allKeys = redisTemplate.keys(LOCAL_KEY_DETAILS.replace(":", "*"));
        if (!allKeys.isEmpty()) {
            var randomKey = (String) allKeys.toArray()[((int) Math.random() * (allKeys.size() - 1))];
            if (randomKey != null) {
                return read(redisTemplate.opsForHash().entries(randomKey), LocalKeyDetails.class);
            }
        }
        return null;
    }

    Optional<LocalKeyDetails> findById(UUID keyId) {
        var entry = read(redisTemplate.opsForHash().entries(LOCAL_KEY_DETAILS + keyId.toString()),
                LocalKeyDetails.class);
        if (entry == null || entry.getKeyId() == null)
            return Optional.empty();
        return Optional.of(entry);
    }

    LocalKeyDetails save(KeyDetails keyDetails, long timeToLive, TimeUnit timeUnit) {
        var localKeyDetails = new LocalKeyDetails(
                keyDetails.getKeyId(), keyDetails.getSecretKey(),
                keyDetails.getExpiry());
        redisTemplate.opsForHash().putAll(LOCAL_KEY_DETAILS + keyDetails.getKeyId().toString(),
                map(localKeyDetails));
        redisTemplate.expire(LOCAL_KEY_DETAILS + keyDetails.getKeyId().toString(), timeToLive, timeUnit);
        return localKeyDetails;
    }

    LocalKeyDetails saveSession(KeyDetails keyDetails, long timeToLive, TimeUnit timeUnit) {
        var localKeyDetails = new LocalKeyDetails(
                keyDetails.getKeyId(), keyDetails.getSecretKey(),
                keyDetails.getExpiry());
        redisTemplate.opsForHash().putAll(LOCAL_KEY_DETAILS_TEMP + keyDetails.getKeyId().toString(),
                map(localKeyDetails));
        redisTemplate.expire(LOCAL_KEY_DETAILS_TEMP + keyDetails.getKeyId().toString(), timeToLive, timeUnit);
        return localKeyDetails;
    }

    void deleteByKeyId(UUID keyId) {
        redisTemplate.delete(LOCAL_KEY_DETAILS + keyId.toString());
    }

    void reset() {
        var allKeys = redisTemplate.keys(LOCAL_KEY_DETAILS + "*");
        redisTemplate.delete(allKeys);
    }

    private <T> Map<Object, Object> map(T any) {
        return objectMapper.convertValue(any, new TypeReference<Map<Object, Object>>() {
        });
    }

    private <T> T read(Map<Object, Object> any, Class<T> clazz) {
        return objectMapper.convertValue(any, clazz);
    }
}
