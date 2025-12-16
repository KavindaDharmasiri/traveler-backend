package com.traveler.core.controller;

import com.traveler.common.dto.CartDTO;
import com.traveler.core.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<List<CartDTO>> getCartItems() {
        return ResponseEntity.ok(cartService.getCartItems());
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getCartCount() {
        return ResponseEntity.ok(cartService.getCartCount());
    }
}
