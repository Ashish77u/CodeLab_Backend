package com.codelab.backend.service;


import com.codelab.backend.dto.request.LoginRequest;
import com.codelab.backend.dto.request.RefreshTokenRequest;
import com.codelab.backend.dto.request.RegisterRequest;
import com.codelab.backend.dto.response.AuthResponse;
import com.codelab.backend.dto.response.UserSummaryResponse;
import com.codelab.backend.entity.Role;
import com.codelab.backend.entity.User;
import com.codelab.backend.exception.AppException;
import com.codelab.backend.exception.EmailAlreadyExistsException;
import com.codelab.backend.exception.InvalidCredentialsException;
import com.codelab.backend.exception.UsernameAlreadyExistsException;
import com.codelab.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    // ── Register ──────────────────────────────────────────────────

    @Transactional
    public AuthResponse register(RegisterRequest request) {

        // Guard: email uniqueness
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(
                    "Email '" + request.email() + "' is already registered");
        }

        // Guard: username uniqueness
        if (userRepository.existsByUsername(request.username())) {
            throw new UsernameAlreadyExistsException(
                    "Username '" + request.username() + "' is already taken");
        }

        // Build user (password must be BCrypt-hashed)
        User user = User.builder()
                .email(request.email())
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.USER)
                .provider("local")
                .enabled(true)
                .build();

        User savedUser = userRepository.save(user);

        // Generate tokens immediately — user is logged in after registering
        String accessToken  = jwtService.generateToken(savedUser);
        String refreshToken = jwtService.generateRefreshToken(savedUser);

//        return new AuthResponse(accessToken, refreshToken, toSummary(savedUser));
        return new AuthResponse(accessToken, refreshToken, "Bearer", toSummary(savedUser));    }


    // ── Login ─────────────────────────────────────────────────────

    public AuthResponse login(LoginRequest request) {

        String identifier = request.email().trim();

        // Try email first, then username (case-insensitive)
        User user = userRepository.findByEmail(identifier)
                .or(() -> userRepository.findByEmail(identifier.toLowerCase()))
                .or(() -> userRepository.findByUsername(identifier))
                .orElseThrow(() ->
                        new InvalidCredentialsException("Invalid email or password"));

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            user.getEmail(),
                            request.password()
                    )
            );
        } catch (BadCredentialsException e) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        String accessToken  = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return new AuthResponse(accessToken, refreshToken, "Bearer", toSummary(user));
    }

    // ── Refresh Token ─────────────────────────────────────────────

public AuthResponse refreshToken(RefreshTokenRequest request) {
    String username = jwtService.extractUsername(request.refreshToken()); // ← was extractEmail

    User user = userRepository.findByUsername(username)   // ← was findByEmail
            .orElseThrow(() ->
                    new UsernameNotFoundException("User not found"));

    if (!jwtService.isTokenValid(request.refreshToken(), user)) {
        throw new InvalidCredentialsException("Refresh token is invalid or expired");
    }

    String newAccessToken  = jwtService.generateToken(user);
    String newRefreshToken = jwtService.generateRefreshToken(user);

    return new AuthResponse(newAccessToken, newRefreshToken, "Bearer", toSummary(user));
}


    // ── Helper ────────────────────────────────────────────────────

    private UserSummaryResponse toSummary(User user) {
        return new UserSummaryResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getProfileImageUrl(),   // ← profileImageUrl is 4th
                user.getRole().name(),       // ← role is 5th
                user.getCreatedAt()          // ← createdAt is 6th
        );


    }
}



