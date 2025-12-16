package com.traveler.auth.traveler.service;

import com.traveler.auth.traveler.dto.*;
import com.traveler.auth.traveler.entity.User;
import com.traveler.auth.traveler.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.ArrayList;

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
            return response;
        } catch (Exception e){
            throw new RuntimeException("User not found for tenant: " + tenant);
        }
    }

    public void updatePhoneVerificationStatus(String tenantId, String phoneNumber) {
        try{
         userRepository.findByTenantId(tenantId).ifPresent(user -> {
             if (user.getContactNumber().equals(phoneNumber)) {
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
}
