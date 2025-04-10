package com.user.management.control;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.user.management.entity.PortalUserDetails;

@Repository
public interface PortalUserDetailsRepostory extends JpaRepository<PortalUserDetails, UUID> {

    Optional<PortalUserDetails> findByName(String name);

    Optional<PortalUserDetails> findByUsername(String username);

    @Modifying
    @Query("UPDATE PortalUserDetails user SET user.password = :password WHERE user.username = :username")
    void updatePasswordByUsername(@Param("password") String password, @Param("username") String username);

    @Modifying
    @Query("UPDATE PortalUserDetails user SET user.name = :newName WHERE user.username = :username")
    void updateNameByUsername(@Param("newName") String newName, @Param("username") String username);

    boolean existsByUsername(String username);

    void deleteByUsername(String username);
}
