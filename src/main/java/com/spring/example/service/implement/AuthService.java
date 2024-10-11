package com.spring.example.service.implement;

import com.spring.example.exception.NotFoundException;
import com.spring.example.payload.response.JwtResponse;
import com.spring.example.repository.RoleRepository;
import com.spring.example.repository.UserRepository;
import com.spring.example.security.JwtTokenProvider;
import com.spring.example.service.IAuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService implements IAuthService {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.access.token.expiration.milliseconds}")
    private long expiresIn;

    @Value("${app.refresh.token.expiration.milliseconds}")
    private long refreshExpiresIn;

    public AuthService(AuthenticationManager authenticationManager, UserDetailsService userDetailsService, JwtTokenProvider jwtTokenProvider, UserRepository userRepository, PasswordEncoder passwordEncoder, RoleRepository roleRepository, PasswordEncoder passwordEncoder1) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder1;
    }

    @Override
    public JwtResponse signIn(String usernameOrEmail, String password) {
        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(usernameOrEmail, password));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetails userDetails = userDetailsService.loadUserByUsername(usernameOrEmail);
            String token = jwtTokenProvider.generateToken(userDetails);
            String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);
            return createAccessToken(token, refreshToken);
        } catch (AuthenticationException e) {
            throw new RuntimeException("Invalid username or password", e);
        }
    }

    @Override
    public JwtResponse refreshAccessToken(String refreshToken) {
        if (jwtTokenProvider.isRefreshTokenRevoked(refreshToken)) {
            throw new IllegalArgumentException("Refresh token is revoked");
        }
        try {
            String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            String newAccessToken = jwtTokenProvider.generateToken(userDetails);
            String newRefreshToken = jwtTokenProvider.generateRefreshToken(userDetails);
            return createAccessToken(newAccessToken, newRefreshToken);
        } catch (AuthenticationException e) {
            throw new RuntimeException("Invalid refresh token", e);
        }
    }

    @Override
    public void changePassword(String usernameOrEmail, String oldPassword, String password) {
        userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .ifPresent(user -> {
                    if (passwordEncoder.matches(oldPassword, user.getPassword())) {
                        user.setPassword(passwordEncoder.encode(password));
                        userRepository.save(user);
                    } else {
                        throw new IllegalArgumentException("Old password is incorrect");
                    }
                });
    }

    @Override
    public void addRole(String usernameOrEmail, String roleName) {
        userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail).ifPresent(user -> {
            user.getRoles().add(roleRepository.findByName(roleName)
                    .orElseThrow(() -> new NotFoundException("Role", "name", roleName)));

            userRepository.save(user);
        });
    }

    @Override
    public void removeRole(String usernameOrEmail, String roleName) {
        userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail).ifPresent(user -> {
            user.getRoles().remove(roleRepository.findByName(roleName)
                    .orElseThrow(() -> new NotFoundException("Role", "name", roleName)));

            userRepository.save(user);
        });
    }

    private JwtResponse createAccessToken(String accessToken, String refreshToken) {
        JwtResponse jwtResponse = new JwtResponse();
        jwtResponse.setAccessToken(accessToken);
        jwtResponse.setExpiresIn(expiresIn / 1000);
        jwtResponse.setRefreshToken(refreshToken);
        jwtResponse.setRefreshExpiresIn(refreshExpiresIn / 1000);
        return jwtResponse;
    }
}
