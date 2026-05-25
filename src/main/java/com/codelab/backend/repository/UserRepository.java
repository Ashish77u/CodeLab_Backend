package com.codelab.backend.repository;

import com.codelab.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u WHERE LOWER(u.username) = LOWER(:username)")
    Optional<User> findByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    // For OAuth2 — find user by provider + their ID
    Optional<User> findByProviderAndProviderId(String provider, String providerId);


    Optional<User> findByVerificationToken(String token);

    // Deactivate accounts where:
// - email not verified
// - verification token expired
// - account still enabled
    @Modifying
    @Query("""
    UPDATE User u SET u.enabled = false
    WHERE u.emailVerified = false
    AND u.verificationTokenExpiry < :now
    AND u.enabled = true
    """)
    int deactivateExpiredUnverifiedAccounts(
            @Param("now") LocalDateTime now);
}
