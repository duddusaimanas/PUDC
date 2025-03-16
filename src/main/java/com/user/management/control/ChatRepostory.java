package com.user.management.control;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.user.management.entity.ChatDetails;

@Repository
public interface ChatRepostory extends JpaRepository<ChatDetails, UUID> {
    
}
