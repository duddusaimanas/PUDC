package com.user.management.entity;

import org.springframework.security.core.GrantedAuthority;

import lombok.ToString;

@ToString
public enum PortalPermissions implements GrantedAuthority {
    USER, ADMIN;

    @Override
    public String getAuthority() {
        return this.name();
    }
}
