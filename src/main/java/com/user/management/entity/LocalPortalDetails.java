package com.user.management.entity;

import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@RedisHash("LocalPortalDetails")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocalPortalDetails {

    @Id
    private Long id;

    private List<EnrolledDetails> enrolled;
    private ZonedDateTime timeout;

}
