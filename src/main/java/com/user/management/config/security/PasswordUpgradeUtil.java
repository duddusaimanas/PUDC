package com.user.management.config.security;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.StandardPasswordEncoder;

public final class PasswordUpgradeUtil {

    private PasswordUpgradeUtil() {
    }

    @SuppressWarnings("deprecation")
    public static PasswordEncoder dynamicPasswordEncoder() {
        String encodingId = "bcrypt-12";
        Map<String, PasswordEncoder> encoders = new HashMap<>();
        encoders.put(encodingId, new BCryptPasswordEncoder(12));
        encoders.put("sha256", new StandardPasswordEncoder());
        return new DelegatingPasswordEncoder(encodingId, encoders);
    }
}
