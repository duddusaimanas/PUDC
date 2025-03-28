package com.user.management.entity;

import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PortalUserDetailsView {

    private UUID id;
    private String username;
    private String name;
    private UserStatus status;
    private boolean isAdmin;
}
