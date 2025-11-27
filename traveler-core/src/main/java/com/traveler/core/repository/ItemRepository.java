package com.traveler.core.repository;

import com.traveler.common.entity.Trip;
import com.traveler.common.entity.provider.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
}
