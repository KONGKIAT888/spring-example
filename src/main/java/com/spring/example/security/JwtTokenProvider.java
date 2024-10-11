package com.spring.example.security;

import com.spring.example.entity.Role;
import com.spring.example.entity.User;
import com.spring.example.exception.NotFoundException;
import com.spring.example.repository.UserRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.*;

@Component
public class JwtTokenProvider {

    private final UserRepository userRepository;
    private final Set<String> revokedTokens = new HashSet<>();
    private final Set<String> revokedRefreshTokens = new HashSet<>();

    @Value("${app.jwt.secret}")
    private String jwtSecret;
    @Value("${app.access.token.expiration.milliseconds}")
    private long expiresIn;
    @Value("${app.refresh.token.expiration.milliseconds}")
    private long refreshExpiresIn;

    public JwtTokenProvider(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    public String generateToken(UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new NotFoundException("User", "username", userDetails.getUsername()));

        Date currentDate = new Date();
        Date expireDate = new Date(currentDate.getTime() + expiresIn);

        List<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .toList();

        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .claim("user_id", user.getId())
                .claim("username", user.getUsername())
                .claim("roles", roleNames)
                .claim("email", user.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(expireDate)
                .signWith(key())
                .compact();
    }

    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", userDetails.getUsername());
        claims.put("iat", new Date());
        claims.put("exp", new Date(System.currentTimeMillis() + refreshExpiresIn));
        return Jwts.builder()
                .setClaims(claims)
                .signWith(key())
                .compact();
    }

    public String getUsername(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody();
        if (isRefreshTokenRevoked(token)) {
            throw new JwtException("JWT token is revoked");
        }
        return claims.getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key())
                    .build()
                    .parse(token);
            return true;
        } catch (MalformedJwtException ex) {
            throw new MalformedJwtException("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            throw new ExpiredJwtException(null, null, "JWT token is expired");
        } catch (UnsupportedJwtException ex) {
            throw new UnsupportedJwtException("JWT token is unsupported");
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("JWT claims string is empty");
        } catch (JwtException ex) {
            if (ex instanceof SignatureException && isRefreshTokenRevoked(token)) {
                throw new JwtException("JWT token is revoked");
            } else {
                throw new JwtException("JWT token is invalid");
            }
        }
    }

    public void revokeToken(String accessToken) {
        revokedTokens.add(accessToken);
    }

    public boolean isTokenRevoked(String accessToken) {
        return revokedTokens.contains(accessToken);
    }

    public void revokeRefreshToken(String refreshToken) {
        revokedRefreshTokens.add(refreshToken);
    }

    public boolean isRefreshTokenRevoked(String refreshToken) {
        return revokedRefreshTokens.contains(refreshToken);
    }
}
