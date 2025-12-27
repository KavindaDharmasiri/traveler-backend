package com.traveler.core.repository;

import com.traveler.common.entity.OrderItems;
import com.traveler.common.utils.STATUS;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderItemsRepository extends JpaRepository<OrderItems, Long> {
    List<OrderItems> findByOrderId(Long orderId);
    
    List<OrderItems> findByStatus(STATUS status);

    Optional<OrderItems> findByBagCode(String bagCode);
}
