package com.user.management.config.security;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.user.management.entity.KeyDetails;
import com.user.management.entity.KeyStatus;

@Repository
@Transactional
public interface KeyRepository extends JpaRepository<KeyDetails, UUID> {

    @Modifying
    @Query("UPDATE KeyDetails key SET key.keyStatus = :newStatus WHERE key.keyStatus = :oldStatus")
    void updateKeyStatuses(@Param("newStatus") KeyStatus newStatus, @Param("oldStatus") KeyStatus oldStatus);

    @Modifying
    @Query("UPDATE KeyDetails key SET key.keyStatus = :status WHERE key.keyId = :keyId")
    void updateKeyStatusByKeyId(@Param("status") KeyStatus status, @Param("keyId") UUID keyId);
}
