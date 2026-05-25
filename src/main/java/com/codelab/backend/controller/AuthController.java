package com.codelab.backend.controller;


import com.codelab.backend.dto.request.LoginRequest;
import com.codelab.backend.dto.request.RefreshTokenRequest;
import com.codelab.backend.dto.request.RegisterRequest;
import com.codelab.backend.dto.response.AuthResponse;
import com.codelab.backend.entity.User;
import com.codelab.backend.repository.UserRepository;
import com.codelab.backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.misc.LogManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Register, Login, Refresh token")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Login with email and password")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Get a new access token using refresh token")
    public ResponseEntity<AuthResponse> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }

//    @GetMapping("/verify-email")
//    public ResponseEntity<Map<String, String>> verifyEmail(
//            @RequestParam String token) {
//
//
//        User user = userRepository.findByVerificationToken(token)
//                .orElseThrow(() -> new RuntimeException(
//                        "Invalid verification token"));
//
//        // Check expiry
//        if (user.getVerificationTokenExpiry()
//                .isBefore(LocalDateTime.now())) {
//            return ResponseEntity.badRequest()
//                    .body(Map.of("message",
//                            "Verification link has expired. Please request a new one."));
//        }
//
//        // Verify user
//        user.setEmailVerified(true);
//        user.setVerificationToken(null);
//        user.setVerificationTokenExpiry(null);
//        userRepository.save(user);
//
//        return ResponseEntity.ok(
//                Map.of("message", "Email verified successfully! You can now login."));
//    }



    @GetMapping("/verify-email")
    public ResponseEntity<Map<String, String>> verifyEmail(
            @RequestParam String token) {

        System.out.println("TOKEN FROM URL = " + token);

        Optional<User> optionalUser =
                userRepository.findByVerificationToken(token);

        System.out.println("USER FOUND = " + optionalUser.isPresent());

        if (optionalUser.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            "Invalid verification token"));
        }

        User user = optionalUser.get();

        System.out.println("DB TOKEN = " + user.getVerificationToken());

        if (user.getVerificationTokenExpiry()
                .isBefore(LocalDateTime.now())) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            "Verification link expired"));
        }

        user.setEmailVerified(true);
        user.setVerificationToken(null);
        user.setVerificationTokenExpiry(null);

        userRepository.save(user);

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Email verified successfully"));
    }

//    private final AuthService authService;
//
//    @PostMapping("/register")
//    public ResponseEntity<AuthResponse> register(
//            @Valid @RequestBody RegisterRequest request) {
//        return ResponseEntity.status(HttpStatus.CREATED)
//                .body(authService.register(request));
//    }
//
//    @PostMapping("/login")
//    public ResponseEntity<AuthResponse> login(
//            @Valid @RequestBody LoginRequest request) {
//        return ResponseEntity.ok(authService.login(request));
//    }
//
//    @PostMapping("/refresh-token")
//    public ResponseEntity<AuthResponse> refreshToken(
//            @Valid @RequestBody RefreshTokenRequest request) {
//        return ResponseEntity.ok(authService.refreshToken(request));
//    }
//}


}