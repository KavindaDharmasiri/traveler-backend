package com.traveler.auth.traveler.service;

import com.traveler.auth.traveler.dto.*;
import com.traveler.auth.traveler.entity.*;
import com.traveler.auth.traveler.exception.AuthException;
import com.traveler.auth.traveler.repository.*;
import com.traveler.auth.traveler.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
public class AuthService {
    
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final SecurityService securityService;
    
    public AuthService(UserRepository userRepository, RefreshTokenRepository refreshTokenRepository, 
                      PasswordEncoder passwordEncoder, JwtUtil jwtUtil, SecurityService securityService) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.securityService = securityService;
    }
    
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registration attempt for email: {}", request.getEmail());
        
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed - email already exists: {}", request.getEmail());
            throw new AuthException("Email already exists");
        }
        
        if (!securityService.isValidPassword(request.getPassword())) {
            throw new AuthException("Password does not meet security requirements");
        }
        
        User user = new User();
        user.setType(request.getType());
        user.setName(request.getName());
        user.setGender(request.getGender());
        user.setContactNumber(request.getContactNumber());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setDateOfBirth(request.getDateOfBirth());
        user.setNicNumber(request.getNicNumber());
        user.setNicImageUuid(request.getNicImageUuid());
        user.setIsEmailVerified(true);
        user.setIsNumberVerified(true);

        Address address = new Address();
        address.setStreet1(request.getAddress().getStreet1());
        address.setStreet2(request.getAddress().getStreet2());
        address.setCity(request.getAddress().getCity());
        address.setState(request.getAddress().getState());
        address.setPostalCode(request.getAddress().getPostalCode());
        user.setAddress(address);
        
        if (request.getBankDetails() != null) {
            BankDetails bankDetails = new BankDetails();
            bankDetails.setAccountNumber(request.getBankDetails().getAccountNumber());
            bankDetails.setHolderName(request.getBankDetails().getHolderName());
            bankDetails.setBank(request.getBankDetails().getBank());
            bankDetails.setBranch(request.getBankDetails().getBranch());
            user.setBankDetails(bankDetails);
        }
        
        user = userRepository.save(user);
        
        String accessToken = jwtUtil.generateToken(user.getEmail(), user.getId());
        String refreshToken = createRefreshToken(user);
        
        AuthResponse response = new AuthResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setUserId(user.getId());
        response.setEmail(user.getEmail());
        response.setName(user.getName());
        
        return response;
    }
    
    @Transactional
    public AuthResponse login(LoginRequest request, String ipAddress) {
        log.info("Login attempt for email: {} from IP: {}", request.getEmail(), ipAddress);
        
        if (securityService.isAccountLocked(request.getEmail())) {
            log.warn("Login blocked - account locked: {}", request.getEmail());
            throw new AuthException("Account temporarily locked due to multiple failed attempts");
        }
        
        if (securityService.isIpBlocked(ipAddress)) {
            log.warn("Login blocked - IP blocked: {}", ipAddress);
            throw new AuthException("IP address temporarily blocked");
        }
        
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    securityService.recordLoginAttempt(request.getEmail(), ipAddress, false);
                    log.warn("Login failed - user not found: {}", request.getEmail());
                    return new AuthException("Invalid credentials");
                });
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            securityService.recordLoginAttempt(request.getEmail(), ipAddress, false);
            log.warn("Login failed - invalid password: {}", request.getEmail());
            throw new AuthException("Invalid credentials");
        }
        
        if (!user.getIsActive()) {
            log.warn("Login failed - user not active: {}", request.getEmail());
            throw new AuthException("Account is not active");
        }
        
        securityService.recordLoginAttempt(request.getEmail(), ipAddress, true);
        log.info("Login successful: {}", user.getEmail());
        
        String accessToken = jwtUtil.generateToken(user.getEmail(), user.getId());
        String refreshToken = createRefreshToken(user);
        
        AuthResponse response = new AuthResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setUserId(user.getId());
        response.setEmail(user.getEmail());
        response.setName(user.getName());
        
        return response;
    }
    
    private String createRefreshToken(User user) {
        refreshTokenRepository.deleteByUserId(user.getId());
        
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(LocalDateTime.now().plusDays(7));
        
        refreshTokenRepository.save(refreshToken);
        log.debug("Refresh token created for user: {}", user.getEmail());
        return refreshToken.getToken();
    }
    
    @Transactional
    public AuthResponse refreshToken(String refreshToken) {
        RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new AuthException("Invalid refresh token"));
        
        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(token);
            throw new AuthException("Refresh token expired");
        }
        
        User user = token.getUser();
        String newAccessToken = jwtUtil.generateToken(user.getEmail(), user.getId());
        String newRefreshToken = createRefreshToken(user);
        
        AuthResponse response = new AuthResponse();
        response.setAccessToken(newAccessToken);
        response.setRefreshToken(newRefreshToken);
        response.setUserId(user.getId());
        response.setEmail(user.getEmail());
        response.setName(user.getName());
        
        return response;
    }
}
