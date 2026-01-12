package com.traveler.auth.traveler.repository;

import com.traveler.auth.traveler.entity.User;
import com.traveler.auth.traveler.utils.UserType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByTenantId(String tenantId);
    boolean existsByEmail(String email);

    List<User> findAllByTypeAndIsActive(UserType type, Boolean isActive);
    List<User> findAllByType(UserType type);
    List<User> findAllByIsActiveOrderByCreatedAtDesc(Boolean isActive);
    List<User> findByIsActiveFalseAndCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime startDate, LocalDateTime endDate);

    Optional<User> findByContactNumber(String contactNumber);

    List<User> findAllByIsActive(boolean b);
}
