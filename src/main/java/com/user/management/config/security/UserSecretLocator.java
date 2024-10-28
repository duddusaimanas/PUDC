package com.user.management.config.security;

import java.security.Key;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.user.management.control.PortalUserDetailsManager;
import com.user.management.entity.PortalUserDetails;

import io.jsonwebtoken.Header;
import io.jsonwebtoken.Locator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserSecretLocator implements Locator<Key> {

    private final PortalUserDetailsManager userDetailsManager;

    @Getter(lombok.AccessLevel.PACKAGE)
    private PortalUserDetails userDetails;

    @Override
    public SecretKey locate(Header header) {
        String kid = (String) header.get("kid");
        if (!StringUtils.hasText(kid))
            return null;

        userDetails = userDetailsManager.findUserById(UUID.fromString(kid));
        return userDetails.getSecretKey();
    }
}
