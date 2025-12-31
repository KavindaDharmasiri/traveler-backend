package com.traveler.auth.traveler.repository;

import com.traveler.common.entity.Order;
import com.traveler.common.entity.OrderItems;
import com.traveler.common.utils.STATUS;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderItemsRepository extends JpaRepository<OrderItems, Long> {
    List<OrderItems> findByOrderId(Long orderId);

    Optional<OrderItems> findByBagCode(String bagCode);

    int countByOrderAndStatusNot(Order order, STATUS status);
}
