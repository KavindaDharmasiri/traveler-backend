package com.traveler.core.repository;

import com.traveler.common.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUserTenantOrderByCreatedAtDesc(String userTenant);
    Transaction findByTransactionCode(String transactionCode);
}