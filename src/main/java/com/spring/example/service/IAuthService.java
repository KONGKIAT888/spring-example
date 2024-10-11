package com.spring.example.service;

import com.spring.example.payload.response.JwtResponse;

public interface IAuthService {
    JwtResponse signIn(String usernameOrEmail, String password);

    JwtResponse refreshAccessToken(String refreshToken);

    void changePassword(String usernameOrEmail, String oldPassword, String password);

    void addRole(String usernameOrEmail, String roleName);

    void removeRole(String usernameOrEmail, String roleName);
}
