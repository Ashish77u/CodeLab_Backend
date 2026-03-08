package com.codelab.backend.repository;

import com.codelab.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    // For OAuth2 — find user by provider + their ID
    Optional<User> findByProviderAndProviderId(String provider, String providerId);
}
