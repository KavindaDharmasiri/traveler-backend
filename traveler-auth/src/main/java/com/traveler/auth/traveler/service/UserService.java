package com.traveler.auth.traveler.service;

import com.traveler.auth.traveler.dto.*;
import com.traveler.auth.traveler.entity.User;
import com.traveler.auth.traveler.repository.UserRepository;
import com.traveler.auth.traveler.dto.ProfileUpdateDTO;
import com.traveler.auth.traveler.utils.UserType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserService implements UserDetailsService {
    
    private final UserRepository userRepository;
    
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(), user.getPassword(), new ArrayList<>());
    }
    
    public UserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setType(user.getType());
        response.setName(user.getName());
        response.setGender(user.getGender());
        response.setContactNumber(user.getContactNumber());
        response.setIsNumberVerified(user.getIsNumberVerified());
        response.setEmail(user.getEmail());
        response.setIsEmailVerified(user.getIsEmailVerified());
        response.setDateOfBirth(user.getDateOfBirth());
        response.setNicNumber(user.getNicNumber());
        response.setNicImageUuid(user.getNicImageUuid());
        response.setTenantId(user.getTenantId());
        response.setProfileImageUuid(user.getProfileImageUuid());
        response.setCountry(user.getCountry());
        response.setGoogleMapsUrl(user.getGoogleMapsUrl());

        if (user.getAddress() != null) {
            AddressDto addressDto = new AddressDto();
            addressDto.setStreet1(user.getAddress().getStreet1());
            addressDto.setStreet2(user.getAddress().getStreet2());
            addressDto.setCity(user.getAddress().getCity());
            addressDto.setState(user.getAddress().getState());
            addressDto.setPostalCode(user.getAddress().getPostalCode());
            response.setAddress(addressDto);
        }
        
        if (user.getBankDetails() != null) {
            BankDetailsDto bankDto = new BankDetailsDto();
            bankDto.setAccountNumber(user.getBankDetails().getAccountNumber());
            bankDto.setHolderName(user.getBankDetails().getHolderName());
            bankDto.setBank(user.getBankDetails().getBank());
            bankDto.setBranch(user.getBankDetails().getBranch());
            response.setBankDetails(bankDto);
        }
        
        return response;
    }

    public UserResponse getUserByTenant(String tenant) {
        try{
            User user = userRepository.findByTenantId(tenant).orElseThrow(() -> new RuntimeException("User not found"));
            UserResponse response = new UserResponse();
            response.setId(user.getId());
            response.setType(user.getType());
            response.setName(user.getName());
            response.setGender(user.getGender());
            response.setContactNumber(user.getContactNumber());
            response.setIsNumberVerified(user.getIsNumberVerified());
            response.setEmail(user.getEmail());
            response.setIsEmailVerified(user.getIsEmailVerified());
            response.setDateOfBirth(user.getDateOfBirth());
            response.setNicNumber(user.getNicNumber());
            response.setNicImageUuid(user.getNicImageUuid());
            response.setTenantId(user.getTenantId());
            response.setGoogleMapsUrl(user.getGoogleMapsUrl());
            return response;
        } catch (Exception e){
            throw new RuntimeException("User not found for tenant: " + tenant);
        }
    }

    public void updatePhoneVerificationStatus(String tenantId, String phoneNumber) {
        try{
         userRepository.findByTenantId(tenantId).ifPresent(user -> {
             String normalizedUserPhone = normalizePhoneNumber(user.getContactNumber());
             String normalizedInputPhone = normalizePhoneNumber(phoneNumber);
             if (normalizedUserPhone.equals(normalizedInputPhone)) {
                 user.setIsNumberVerified(true);
                 userRepository.save(user);
             } else {
                 throw new RuntimeException("Phone number does not match for tenant: " + tenantId);
             }
         });
        }catch (Exception e){
            throw new RuntimeException("Failed to update phone verification status");
        }
    }

    public void updateEmailVerificationStatus(String tenantId, String email) {
        try{
            userRepository.findByTenantId(tenantId).ifPresent(user -> {
                if (user.getEmail().equals(email)) {
                    user.setIsEmailVerified(true);
                    userRepository.save(user);
                } else {
                    throw new RuntimeException("email does not match for tenant: " + tenantId);
                }
            });
        }catch (Exception e){
            throw new RuntimeException("Failed to update email verification status");
        }
    }

    public UserResponse updateProfile(String tenant, ProfileUpdateDTO request) {
        User user = userRepository.findByTenantId(tenant)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Check if email or phone number changed
        boolean emailChanged = request.getEmail() != null && !user.getEmail().equals(request.getEmail());
        boolean phoneChanged = request.getContactNumber() != null && 
                              !normalizePhoneNumber(request.getContactNumber()).equals(normalizePhoneNumber(user.getContactNumber()));

        if (emailChanged){
            userRepository.findByEmail(request.getEmail()).ifPresent(existingUser -> {
                if (!existingUser.getTenantId().equals(tenant)) {
                    throw new RuntimeException("Email already in use");
                }
            });
        }

        if (phoneChanged){
            userRepository.findByContactNumber(request.getContactNumber()).ifPresent(existingUser -> {
                if (!existingUser.getTenantId().equals(tenant)) {
                    throw new RuntimeException("Contact number already in use");
                }
            });
        }
        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }
        if (request.getGoogleMapsUrl() != null) {
            user.setGoogleMapsUrl(request.getGoogleMapsUrl());
        }
        if (request.getContactNumber() != null) {
            user.setContactNumber(request.getContactNumber());
            if (phoneChanged) {
                user.setIsNumberVerified(false);
            }
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
            if (emailChanged) {
                user.setIsEmailVerified(false);
            }
        }
        if (request.getCountry() != null) {
            user.setCountry(request.getCountry());
        }
        if (request.getProfileImageUuid() != null) {
            user.setProfileImageUuid(request.getProfileImageUuid());
        }
        if (request.getDateOfBirth() != null) {
            user.setDateOfBirth(java.time.LocalDate.parse(request.getDateOfBirth()));
        }
        if (request.getNicNumber() != null) {
            user.setNicNumber(request.getNicNumber());
        }
        if (request.getNicImageUuid() != null) {
            user.setNicImageUuid(request.getNicImageUuid());
        }
        
        // Update address if provided
        if (request.getAddress() != null) {
            if (user.getAddress() == null) {
                user.setAddress(new com.traveler.auth.traveler.entity.Address());
            }
            if (request.getAddress().getStreet1() != null) {
                user.getAddress().setStreet1(request.getAddress().getStreet1());
            }
            if (request.getAddress().getStreet2() != null) {
                user.getAddress().setStreet2(request.getAddress().getStreet2());
            }
            if (request.getAddress().getCity() != null) {
                user.getAddress().setCity(request.getAddress().getCity());
            }
            if (request.getAddress().getState() != null) {
                user.getAddress().setState(request.getAddress().getState());
            }
            if (request.getAddress().getPostalCode() != null) {
                user.getAddress().setPostalCode(request.getAddress().getPostalCode());
            }
        }
        
        // Update bank details if provided
        if (request.getBankDetails() != null) {
            if (user.getBankDetails() == null) {
                user.setBankDetails(new com.traveler.auth.traveler.entity.BankDetails());
            }
            if (request.getBankDetails().getAccountNumber() != null) {
                user.getBankDetails().setAccountNumber(request.getBankDetails().getAccountNumber());
            }
            if (request.getBankDetails().getHolderName() != null) {
                user.getBankDetails().setHolderName(request.getBankDetails().getHolderName());
            }
            if (request.getBankDetails().getBank() != null) {
                user.getBankDetails().setBank(request.getBankDetails().getBank());
            }
            if (request.getBankDetails().getBranch() != null) {
                user.getBankDetails().setBranch(request.getBankDetails().getBranch());
            }
        }

        userRepository.save(user);
        return getCurrentUser(user.getEmail());
    }
    
    public List<String> getProviders() {
        List<User> providers = userRepository.findAllByTypeAndIsActive(UserType.SERVICE_PROVIDER, true);
        return providers.stream().map(User::getName).distinct().sorted().toList();
    }
    
    private String normalizePhoneNumber(String phoneNumber) {
        if (phoneNumber == null) return "";
        // Remove all non-digit characters
        String digits = phoneNumber.replaceAll("\\D", "");
        // If starts with country code (94 for Sri Lanka), remove it
        if (digits.startsWith("94") && digits.length() > 10) {
            digits = digits.substring(2);
        }
        return digits;
    }
}
