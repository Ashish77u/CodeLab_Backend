package com.codelab.backend.service;

import com.codelab.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountCleanUpService {

    private final UserRepository userRepository;

    // Runs every hour — deactivates unverified accounts
    // whose token has expired (24 hours)
    @Scheduled(fixedRate = 3600000) // every 1 hour
    @Transactional
    public void deactivateUnverifiedAccounts() {
        int count = userRepository
                .deactivateExpiredUnverifiedAccounts(
                        LocalDateTime.now());
        if (count > 0) {
            log.info("Deactivated {} unverified accounts", count);
        }
    }
}