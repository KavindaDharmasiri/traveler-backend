package com.traveler.core.repository;

import com.traveler.common.entity.Cart;
import com.traveler.common.entity.CartItems;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartItemsRepository extends JpaRepository<CartItems, Long> {

    List<CartItems> findByCart(Cart cart);

    void deleteByCart(Cart cart);
}
