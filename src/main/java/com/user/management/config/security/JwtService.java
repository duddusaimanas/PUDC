package com.user.management.config.security;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import com.user.management.entity.LocalKeyDetails;
import com.user.management.entity.PortalUserDetails;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.CompressionAlgorithm;
import io.jsonwebtoken.security.AeadAlgorithm;
import io.jsonwebtoken.security.SecretKeyAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtService {

    private static final SecretKeyAlgorithm KEY_ALG = Jwts.KEY.A256GCMKW;
    private static final AeadAlgorithm ENC_ALG = Jwts.ENC.A256GCM;
    private static final CompressionAlgorithm ZIP_ALG = Jwts.ZIP.GZIP;

    private final KeyLocator keyLocator;
    private final KeyManager keyManager;

    public String generateToken(PortalUserDetails userDetails, long timeToLiveForToken,
            ChronoUnit temporalUnitForToken) {
        var now = Instant.now();
        var tokenExpiry = now.plus(timeToLiveForToken, temporalUnitForToken);
        var secretDetails = getSecretDetails();
        return Jwts.builder()
                .claims(Map.of("scope", List.of(userDetails.getPermissions())))
                .subject(userDetails.getUsername())
                .issuedAt(Date.from(now))
                .expiration(Date.from(tokenExpiry))
                .header().type("JWE").keyId(secretDetails.getKeyId().toString()).and()
                .encryptWith(secretDetails.getSecret(), KEY_ALG, ENC_ALG)
                .compressWith(ZIP_ALG)
                .compact();
    }

    private LocalKeyDetails getSecretDetails() {
        return Optional.ofNullable(keyManager.fetchKey()).orElse(keyManager.generateKey(secretKey()));
    }

    protected SecretKey secretKey() {
        return KEY_ALG.key().build();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .keyLocator(keyLocator)
                .key().add(KEY_ALG).and()
                .enc().add(ENC_ALG).and()
                .zip().add(ZIP_ALG).and()
                .build()
                .parseEncryptedClaims(token)
                .getPayload();
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public boolean validateToken(String token, PortalUserDetails userDetails) {
        return !isTokenExpired(token);
    }
}
