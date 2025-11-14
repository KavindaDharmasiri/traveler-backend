package com.traveler.auth.traveler.service;

import com.traveler.auth.traveler.entity.LoginAttempt;
import com.traveler.auth.traveler.repository.LoginAttemptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class SecurityService {
    
    private final LoginAttemptRepository loginAttemptRepository;
    
    public SecurityService(LoginAttemptRepository loginAttemptRepository) {
        this.loginAttemptRepository = loginAttemptRepository;
    }
    private static final int MAX_ATTEMPTS = 5;
    private static final int LOCKOUT_MINUTES = 15;
    
    public boolean isAccountLocked(String email) {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(LOCKOUT_MINUTES);
        List<LoginAttempt> attempts = loginAttemptRepository
                .findByEmailAndAttemptTimeAfterAndSuccessfulFalse(email, cutoff);
        return attempts.size() >= MAX_ATTEMPTS;
    }
    
    public boolean isIpBlocked(String ipAddress) {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(LOCKOUT_MINUTES);
        List<LoginAttempt> attempts = loginAttemptRepository
                .findByIpAddressAndAttemptTimeAfterAndSuccessfulFalse(ipAddress, cutoff);
        return attempts.size() >= MAX_ATTEMPTS * 2;
    }
    
    public void recordLoginAttempt(String email, String ipAddress, boolean successful) {
        LoginAttempt attempt = new LoginAttempt();
        attempt.setEmail(email);
        attempt.setIpAddress(ipAddress);
        attempt.setSuccessful(successful);
        loginAttemptRepository.save(attempt);
    }
    
    public boolean isValidPassword(String password) {
        return password.length() >= 8 &&
               password.matches(".*[A-Z].*") &&
               password.matches(".*[a-z].*") &&
               password.matches(".*\\d.*") &&
               password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");
    }
}