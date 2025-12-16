package com.traveler.notification.repository;

import com.traveler.common.entity.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {
    
    Optional<EmailVerification> findByUserTenantAndEmailAndIsVerifiedFalseAndExpiresAtAfter(
            String userTenant, String email, LocalDateTime currentTime);
    
    void deleteByUserTenantAndEmail(String userTenant, String email);
}