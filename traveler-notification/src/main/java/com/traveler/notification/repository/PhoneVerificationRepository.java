package com.traveler.notification.repository;

import com.traveler.common.entity.PhoneVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PhoneVerificationRepository extends JpaRepository<PhoneVerification, Long> {
    
    Optional<PhoneVerification> findByUserTenantAndPhoneNumberAndIsVerifiedFalseAndExpiresAtAfter(
            String userTenant, String phoneNumber, LocalDateTime currentTime);
    
    void deleteByUserTenantAndPhoneNumber(String userTenant, String phoneNumber);
}