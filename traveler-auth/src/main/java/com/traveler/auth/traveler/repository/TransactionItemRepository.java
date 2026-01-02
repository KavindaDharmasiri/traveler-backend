package com.traveler.auth.traveler.repository;

import com.traveler.common.entity.Transaction;
import com.traveler.common.entity.TransactionItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TransactionItemRepository extends JpaRepository<TransactionItem, Long> {

}
