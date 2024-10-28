package com.user.management.entity;

import org.springframework.security.core.GrantedAuthority;

public enum PortalPermissions implements GrantedAuthority {
    USER, ADMIN;

    @Override
    public String getAuthority() {
        return this.name();
    }
}
