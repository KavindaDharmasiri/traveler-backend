package com.traveler.core.repository;

import com.traveler.common.entity.Trip;
import com.traveler.common.entity.provider.Item;
import com.traveler.common.utils.STATUS;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findAllByStatus(STATUS status);
}
