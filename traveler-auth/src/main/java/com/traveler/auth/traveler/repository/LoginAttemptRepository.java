package com.traveler.auth.traveler.repository;

import com.traveler.auth.traveler.entity.LoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, Long> {
    List<LoginAttempt> findByEmailAndAttemptTimeAfterAndSuccessfulFalse(String email, LocalDateTime time);
    List<LoginAttempt> findByIpAddressAndAttemptTimeAfterAndSuccessfulFalse(String ipAddress, LocalDateTime time);
}