package com.user.management.entity;

import java.time.ZonedDateTime;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@RedisHash("LocalKeyDetails")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocalKeyDetails {

    @Id
    private UUID keyId;

    private SecretKey secret;
    private ZonedDateTime expiry;

}
