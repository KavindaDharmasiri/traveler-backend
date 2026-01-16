package com.traveler.notification.service;

import com.traveler.common.entity.EmailVerification;
import com.traveler.notification.repository.EmailVerificationRepository;
import com.traveler.notification.client.AuthClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import jakarta.mail.internet.MimeMessage;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;
import java.nio.charset.StandardCharsets;
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
        sendVerificationEmail(email, verificationCode);
    }

    private void sendVerificationEmail(String email, String verificationCode) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setFrom("travlerofficial2025@gmail.com");
            helper.setTo(email.trim());
            helper.setSubject("Traveler - Email Verification");
            
            String htmlContent = buildVerificationEmailHtml(verificationCode);
            helper.setText(htmlContent, true);
            
            mailSender.send(mimeMessage);
            log.info("Email verification sent successfully to {}", email);
        } catch (Exception e) {
            log.error("Error sending email verification to {}: {}", email, e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to send email verification", e);
        }
    }

    private String buildVerificationEmailHtml(String verificationCode) {
        return "<html><body style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;'>" +
               "<div style='background: linear-gradient(135deg, #0f766e, #134e4a); padding: 30px; text-align: center; color: white;'>" +
               "<h1>🧳 Traveler</h1><p>Email Verification</p></div>" +
               "<div style='padding: 30px;'>" +
               "<h2>Verify Your Email Address</h2>" +
               "<p>Thank you for joining Traveler! Please use the verification code below:</p>" +
               "<div style='background: #f0fdfa; border: 2px solid #10b981; border-radius: 12px; padding: 20px; text-align: center; margin: 20px 0;'>" +
               "<p style='color: #065f46; font-weight: bold; margin: 0;'>YOUR VERIFICATION CODE</p>" +
               "<div style='font-size: 32px; font-weight: bold; color: #0f766e; letter-spacing: 4px; font-family: monospace; margin: 10px 0;'>" + verificationCode + "</div>" +
               "<p style='color: #059669; font-size: 12px; margin: 0;'>Valid for 5 minutes</p>" +
               "</div>" +
               "<p style='color: #92400e; background: #fef3c7; padding: 15px; border-left: 4px solid #f59e0b;'>" +
               "⚠️ This code will expire in 5 minutes for your security.</p>" +
               "<p>If you didn't request this verification, please ignore this email.</p>" +
               "</div>" +
               "<div style='background: #f9fafb; padding: 20px; text-align: center; border-top: 1px solid #e5e7eb;'>" +
               "<p style='color: #6b7280; font-size: 14px;'>Need help? Contact us at support@traveler.com</p>" +
               "<p style='color: #9ca3af; font-size: 12px;'>© 2026 Traveler. All rights reserved.</p>" +
               "</div></body></html>";
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
