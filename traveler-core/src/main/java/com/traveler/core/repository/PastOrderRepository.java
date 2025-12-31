package com.traveler.core.repository;

import com.traveler.common.entity.Order;
import com.traveler.common.entity.PastOrder;
import com.traveler.common.utils.STATUS;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PastOrderRepository extends JpaRepository<PastOrder, Long> {
    Optional<PastOrder> findByOrderCode(String orderCode);

    List<PastOrder> findAllByStatusNot(STATUS status);
    
    List<PastOrder> findAllByStatus(STATUS status);
    
    List<PastOrder> findByClientTenant(String clientTenant);

    List<PastOrder> findAllByOrderByCreatedAtDesc();

    void deleteByOrderCode(String orderId);
}
