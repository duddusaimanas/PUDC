package com.user.management.entity;

import java.util.List;
import java.util.UUID;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatDetails {

    @Id
    private UUID conversationId;

    private String title;

    @ElementCollection
    private List<ChatElement> chatDetails;
}
