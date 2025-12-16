package com.traveler.payment.repository;

import com.traveler.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    Optional<Payment> findByPaymentId(String paymentId);
    
    Optional<Payment> findByPayHerePaymentId(String payHerePaymentId);
    
    List<Payment> findByUserIdOrderByCreatedAtDesc(String userId);
    
    List<Payment> findByOrderId(String orderId);
    
    List<Payment> findByStatus(Payment.PaymentStatus status);
    
    @Query("SELECT p FROM Payment p WHERE p.userId = :userId AND p.status = :status")
    List<Payment> findByUserIdAndStatus(@Param("userId") String userId, @Param("status") Payment.PaymentStatus status);
    
    boolean existsByOrderIdAndStatus(String orderId, Payment.PaymentStatus status);
}