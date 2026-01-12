package com.traveler.auth.traveler.repository;

import com.traveler.common.entity.Order;
import com.traveler.common.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByTransactionCode(String transactionCode);
    
    Page<Transaction> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);
    
    Page<Transaction> findByCustomerNameContainingIgnoreCase(String customerName, Pageable pageable);
    
    Page<Transaction> findByCreatedAtBetweenAndCustomerNameContainingIgnoreCase(LocalDateTime start, LocalDateTime end, String customerName, Pageable pageable);
}
