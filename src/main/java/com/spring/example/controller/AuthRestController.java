package com.spring.example.controller;

import com.spring.example.payload.response.JwtResponse;
import com.spring.example.security.JwtTokenProvider;
import com.spring.example.service.implement.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/auth")
public class AuthRestController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthRestController(AuthService authService, JwtTokenProvider jwtTokenProvider) {
        this.authService = authService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping(value = "/sign-in", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<?> login(@Valid @RequestParam("username_or_email") String usernameOrEmail,
                                   @Valid @RequestParam("password") String password) {
        JwtResponse jwtResponse = authService.signIn(usernameOrEmail, password);

        return ResponseEntity.ok(jwtResponse);
    }

    @PostMapping(value = "/refresh-token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<?> refreshAccessToken(@Valid @RequestParam("refresh_token") String refreshToken) {
        if (jwtTokenProvider.isRefreshTokenRevoked(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh Token has been revoked");
        }

        JwtResponse jwtResponse = authService.refreshAccessToken(refreshToken);

        return ResponseEntity.ok(jwtResponse);
    }

    @PostMapping(value = "/sign-out", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<String> logout(@Valid @RequestParam("access_token") String accessToken,
                                         @Valid @RequestParam("refresh_token") String refreshToken) {
        if (jwtTokenProvider.isTokenRevoked(accessToken) || jwtTokenProvider.isRefreshTokenRevoked(refreshToken)) {
            return ResponseEntity.badRequest().body("Tokens are already revoked.");
        }

        jwtTokenProvider.revokeToken(accessToken);
        jwtTokenProvider.revokeRefreshToken(refreshToken);

        return ResponseEntity.ok("Sign out successfully.");
    }

    @PostMapping(value = "/revoke-access-token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<String> revokeAccessToken(@Valid @RequestParam("access_token") String accessToken) {
        if (jwtTokenProvider.isTokenRevoked(accessToken)) {
            return ResponseEntity.badRequest().body("Access token is already revoked.");
        }

        jwtTokenProvider.revokeToken(accessToken);

        return ResponseEntity.ok("Access Token Revoked Successfully.");
    }

    @PostMapping(value = "/revoke-refresh-token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<String> revokeRefreshToken(@Valid @RequestParam("refresh_token") String refreshToken) {
        if (jwtTokenProvider.isRefreshTokenRevoked(refreshToken)) {
            return ResponseEntity.badRequest().body("Refresh token is already revoked.");
        }

        jwtTokenProvider.revokeRefreshToken(refreshToken);

        return ResponseEntity.ok("Refresh Token Revoked Successfully.");
    }

    @PostMapping(value = "/change-password", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<String> changePassword(@Valid @RequestParam("username_or_email") String usernameOrEmail,
                                                 @Valid @RequestParam("old_password") String oldPassword,
                                                 @Valid @RequestParam("password") String password) {
        if (oldPassword.equals(password)) {
            throw new IllegalArgumentException("New password must be different from the old password.");
        }

        if (password.length() < 5 || password.length() > 24) {
            throw new IllegalArgumentException("Password should contain at least 5 characters, but no more than 24 characters.");
        }

        authService.changePassword(usernameOrEmail, oldPassword, password);

        return ResponseEntity.ok("Password changed successfully.");
    }

    @PostMapping(value = "/add-role", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<String> addRole(@Valid @RequestParam("username_or_email") String usernameOrEmail,
                                          @Valid @RequestParam("role_name") String roleName) {
        authService.addRole(usernameOrEmail, roleName);

        return ResponseEntity.ok("Role added successfully.");
    }

    @PostMapping(value = "/remove-role", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<String> removeRole(@Valid @RequestParam("username_or_email") String usernameOrEmail,
                                             @Valid @RequestParam("role_name") String roleName) {
        authService.removeRole(usernameOrEmail, roleName);

        return ResponseEntity.ok("Role removed successfully.");
    }
}
