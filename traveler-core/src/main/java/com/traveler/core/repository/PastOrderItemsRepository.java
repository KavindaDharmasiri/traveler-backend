package com.traveler.core.repository;

import com.traveler.common.entity.OrderItems;
import com.traveler.common.entity.PastOrderItems;
import com.traveler.common.utils.STATUS;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PastOrderItemsRepository extends JpaRepository<PastOrderItems, Long> {
    List<PastOrderItems> findByOrderId(Long orderId);
    
    List<PastOrderItems> findByStatus(STATUS status);

    Optional<PastOrderItems> findByBagCode(String bagCode);

    void deleteByOrderId(Long order_id);

    void deleteByBagCode(String bagCode);
}
