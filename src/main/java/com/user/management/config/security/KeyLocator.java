package com.user.management.config.security;

import java.security.Key;
import java.util.UUID;
import java.util.function.Supplier;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.user.management.control.exception.UserSecretNotFoundException;

import io.jsonwebtoken.Header;
import io.jsonwebtoken.JweHeader;
import io.jsonwebtoken.Locator;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class KeyLocator implements Locator<Key> {

    private final KeyManager keyManager;

    @Override
    public SecretKey locate(Header header) {
        if (header instanceof JweHeader) {
            var kid = ((JweHeader) header).getKeyId();
            if (StringUtils.hasText(kid)) {
                UUID keyId = UUID.fromString(kid);
                return keyManager.findById(keyId).orElseThrow(throwIfKeyIdNotFound("keyId", keyId))
                        .getSecret();
            }
        }
        return null;
    }

    private Supplier<UserSecretNotFoundException> throwIfKeyIdNotFound(String field, Object value) {
        return () -> new UserSecretNotFoundException(field, value);
    }
}
