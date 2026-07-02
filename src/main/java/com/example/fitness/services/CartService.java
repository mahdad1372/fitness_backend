package com.example.fitness.services;


import com.example.fitness.entitties.Cart;
import com.example.fitness.repositories.CartRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class CartService {

    private final CartRepository cartRepository;

    public CartService(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    public List<Cart> getAllCartItems() {
        List<Cart> cartItems = new ArrayList<>();
        cartRepository.getAllCartItems().forEach(cartItems::add);
        return cartItems;
    }

    public List<Cart> fetchAll() {
        return cartRepository.getAllCartItems();
    }

    public Cart getCartById(Integer cartId) {
        return cartRepository.findCartByCartId(cartId);
    }

    public List<Cart> getCartByUserId(Integer userId) {
        return cartRepository.findCartByUserId(userId);
    }

    public void addCartItem(
            String cart_item,
            Float price,
            Integer coach_id,
            Integer userId
    ) {
        cartRepository.addCartItem(
                cart_item,
                price,
                coach_id,
                userId
        );
    }

    public void deleteCartById(Integer cartId) {
        cartRepository.deleteCartByCartId(cartId);
    }

    public void updateCartItem(
            Integer cartId,
            String cartItem,
            BigDecimal price,
            Integer coachId,
            Integer userId
    ) {

        cartRepository.updateCartItem(
                cartId,
                cartItem,
                price,
                coachId,
                userId
        );
    }

    public Integer countCartItemsByUserId(Integer userId) {
        return cartRepository.countCartItemsByUserId(userId);
    }
}