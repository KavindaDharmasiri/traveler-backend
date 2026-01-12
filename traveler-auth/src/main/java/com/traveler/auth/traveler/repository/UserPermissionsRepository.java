package com.traveler.auth.traveler.repository;

import com.traveler.auth.traveler.entity.UserPermissions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserPermissionsRepository extends JpaRepository<UserPermissions, Long> {
    Optional<UserPermissions> findByUserId(Long userId);
    void deleteByUserId(Long userId);
}