package com.spring.example.payload.response;

import lombok.Data;

@Data
public class JwtResponse {
    private String tokenType = "Bearer";
    private String accessToken;
    private Long expiresIn;
    private String refreshToken;
    private Long refreshExpiresIn;
}
