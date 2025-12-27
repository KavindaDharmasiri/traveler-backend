package com.traveler.core.repository;

import com.traveler.common.entity.Backpack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BackpackRepository extends JpaRepository<Backpack, Long> {
    List<Backpack> findByProviderTenant(String providerTenant);
    List<Backpack> findByItemId(Long itemId);
}
