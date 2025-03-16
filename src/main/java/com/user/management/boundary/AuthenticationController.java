package com.user.management.boundary;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import com.user.management.config.security.JwtService;
import com.user.management.control.PortalUserDetailsManager;
import com.user.management.entity.PortalUserDetails;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;

@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RestController
@RequiredArgsConstructor
public class AuthenticationController {

        private final JwtService jwtService;
        private final PortalUserDetailsManager userDetailsManager;

        @Value("${jwt.refresh.token.time-to-live}")
        private long timeToLiveForRefreshToken;

        @Value("${jwt.refresh.temporal-unit}")
        private ChronoUnit temporalUnitForRefreshToken;

        @Value("${jwt.access.token.time-to-live}")
        private long timeToLiveForAccessToken;

        @Value("${jwt.access.temporal-unit}")
        private ChronoUnit temporalUnitForAccessToken;

        @PostMapping(value = "/authenticate")
        public ResponseEntity<Void> authenticateUser(@AuthenticationPrincipal PortalUserDetails userDetails,
                        HttpServletResponse httpServletResponse) {
                var refreshToken = jwtService.generateToken(userDetails, timeToLiveForRefreshToken,
                                temporalUnitForRefreshToken);
                var accessToken = jwtService.generateToken(userDetails, timeToLiveForAccessToken,
                                temporalUnitForAccessToken);
                Cookie cookie = buildRefreshCookie(refreshToken);
                httpServletResponse.addCookie(cookie);
                Cookie cookie2 = buildAccessCookie(accessToken);
                httpServletResponse.addCookie(cookie2);
                return ResponseEntity.noContent().build();
        }

        @PostMapping(value = "/reAuthenticate")
        public ResponseEntity<Void> reAuthenticate(@CookieValue(name = "REFRESH_TOKEN", required = false) String token,
                        HttpServletResponse httpServletResponse) {
                if (StringUtils.hasText(token)) {
                        String username = jwtService.extractUsername(token);
                        var userDetails = userDetailsManager.loadUserByUsername(username);
                        if (jwtService.validateToken(token, userDetails)) {
                                var accessToken = jwtService.generateToken(userDetails, timeToLiveForAccessToken,
                                                temporalUnitForAccessToken);
                                Cookie cookie = buildAccessCookie(accessToken);
                                httpServletResponse.addCookie(cookie);
                        }
                }
                return ResponseEntity.noContent().build();
        }

        private Cookie buildRefreshCookie(String refreshToken) {
                Cookie cookie = new Cookie("REFRESH_TOKEN", refreshToken);
                cookie.setHttpOnly(true);
                cookie.setPath("/");
                cookie.setSecure(true);
                cookie.setAttribute("SameSite", "Strict");
                cookie.setMaxAge((int) Duration.of(timeToLiveForRefreshToken, temporalUnitForRefreshToken).toSeconds());
                return cookie;
        }

        private Cookie buildAccessCookie(String accessToken) {
                Cookie cookie = new Cookie("ACCESS_TOKEN", accessToken);
                cookie.setPath("/");
                cookie.setSecure(true);
                cookie.setAttribute("SameSite", "Strict");
                cookie.setMaxAge((int) Duration.of(timeToLiveForAccessToken, temporalUnitForAccessToken).toSeconds());
                return cookie;
        }
}
