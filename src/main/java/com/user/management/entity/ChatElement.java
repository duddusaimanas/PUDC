package com.user.management.entity;

import java.util.UUID;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class ChatElement {

    private UUID id;
    private String chatter;
    private String chat;
}
