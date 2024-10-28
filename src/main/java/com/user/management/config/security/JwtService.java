package com.user.management.config.security;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import com.user.management.control.PortalUserDetailsManager;
import com.user.management.entity.PortalUserDetails;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.CompressionAlgorithm;
import io.jsonwebtoken.security.AeadAlgorithm;
import io.jsonwebtoken.security.SecretKeyAlgorithm;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtService {

    private final UserSecretLocator locator;
    private final PortalUserDetailsManager userDetailsManager;

    private final Instant now = Instant.now();
    private final Instant tokenTimeOut = now.plus(1, ChronoUnit.DAYS);

    private static final SecretKeyAlgorithm KEY_ALG = Jwts.KEY.A256GCMKW;
    private static final AeadAlgorithm ENC_ALG = Jwts.ENC.A256GCM;
    private static final CompressionAlgorithm ZIP_ALG = Jwts.ZIP.GZIP;

    public String generateToken(PortalUserDetails userDetails) {
        return Jwts.builder()
                .claims(Map.of("scope", List.of(userDetails.getPermissions())))
                .subject(userDetails.getUsername())
                .issuedAt(Date.from(now))
                .expiration(Date.from(tokenTimeOut))
                .header().type("JWE").keyId(userDetails.getId().toString()).and()
                .encryptWith(secretKey(userDetails), KEY_ALG, ENC_ALG)
                .compressWith(ZIP_ALG)
                .compact();
    }

    SecretKey secretKey(PortalUserDetails userDetails) {
        SecretKey secretKey = userDetails.getSecretKey();
        if (secretKey == null) {
            secretKey = KEY_ALG.key().build();
            userDetailsManager.updateSecretByUsername(userDetails, secretKey);
        }
        return secretKey;
    }

    public String extractUsername(String token, SecretKey secretKey) {
        return extractClaim(token, secretKey, Claims::getSubject);
    }

    private Date extractExpiration(String token, SecretKey secretKey) {
        return extractClaim(token, secretKey, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, SecretKey secretKey, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token, secretKey);
        return claimsResolver.apply(claims);
    }

    public PortalUserDetails extractUserDetails(String token) {
        Jwts.parser().keyLocator(locator)
                .key().add(KEY_ALG).and()
                .enc().add(ENC_ALG).and()
                .zip().add(ZIP_ALG).and()
                .build()
                .parse(token);

        return locator.getUserDetails();
    }

    private Claims extractAllClaims(String token, SecretKey secretKey) {
        return Jwts.parser()
                .decryptWith(secretKey)
                .build()
                .parseEncryptedClaims(token)
                .getPayload();
    }

    private boolean isTokenExpired(String token, SecretKey secretKey) {
        return extractExpiration(token, secretKey).before(new Date());
    }

    public boolean validateToken(String token, PortalUserDetails userDetails) {
        if (userDetails == null) {
            return false;
        }

        final String username = extractUsername(token, userDetails.getSecretKey());
        return (username.equals(userDetails.getUsername())
                && (userDetails.getSecretKey() != null)
                && !isTokenExpired(token, userDetails.getSecretKey()));
    }
}
