package com.traveler.notification.service;

import com.traveler.common.entity.EmailVerification;
import com.traveler.notification.repository.EmailVerificationRepository;
import com.traveler.notification.client.AuthClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final EmailVerificationRepository emailVerificationRepository;
    private final AuthClient authClient;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Transactional
    public void sendVerificationCode(String userTenant, String email) {
        // Clean up any existing verification for this user/email
        emailVerificationRepository.deleteByUserTenantAndEmail(userTenant, email);

        // Generate 6-digit verification code
        String verificationCode = String.format("%06d", new Random().nextInt(1000000));

        // Save verification record
        EmailVerification verification = new EmailVerification();
        verification.setUserId(userTenant);
        verification.setUserTenant(userTenant);
        verification.setEmail(email);
        verification.setVerificationCode(verificationCode);
        emailVerificationRepository.save(verification);
        System.out.println(verificationCode);

        // Send email
//        sendVerificationEmail(email, verificationCode);
    }

    private void sendVerificationEmail(String email, String verificationCode) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setSubject("Traveler - Email Verification");
            message.setText("Your email verification code is: " + verificationCode + 
                          "\n\nThis code will expire in 5 minutes.\n\nIf you didn't request this, please ignore this email.");
            
            mailSender.send(message);
            log.info("Email verification sent successfully to {}", email);
        } catch (Exception e) {
            log.error("Error sending email verification to {}: {}", email, e.getMessage());
            throw new RuntimeException("Failed to send email verification", e);
        }
    }

    @Transactional
    public boolean verifyCode(String userTenant, String email, String verificationCode) {
        Optional<EmailVerification> verificationOpt = emailVerificationRepository
                .findByUserTenantAndEmailAndIsVerifiedFalseAndExpiresAtAfter(
                        userTenant, email, LocalDateTime.now());

        if (verificationOpt.isEmpty()) {
            return false;
        }

        EmailVerification verification = verificationOpt.get();
        if (verification.getVerificationCode().equals(verificationCode)) {
            verification.setIsVerified(true);
            verification.setVerifiedAt(LocalDateTime.now());
            emailVerificationRepository.save(verification);
            
            // Update email verification status in auth service
            try {
                authClient.updateEmailVerificationStatus(userTenant, email);
            } catch (Exception e) {
                log.error("Failed to update email verification status in auth service: {}", e.getMessage());
            }
            
            return true;
        }

        return false;
    }
}
