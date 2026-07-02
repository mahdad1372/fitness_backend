package com.example.fitness.controllers;

import com.example.fitness.entitties.Cart;
import com.example.fitness.entitties.Foods;
import com.example.fitness.services.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RequestMapping("/cart")
@RestController
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("/all")
    public ResponseEntity<List<Cart>> allCartItems() {
        List<Cart> cartItems = cartService.getAllCartItems();
        return ResponseEntity.ok(cartItems);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cart> getCartById(
            @PathVariable("id") Integer id
    ) {
        return ResponseEntity.ok(cartService.getCartById(id));
    }

    @GetMapping("/findbyuserid/{id}")
    public ResponseEntity<List<Cart>> getCartByUserId(
            @PathVariable("id") Integer id
    ) {
        return ResponseEntity.ok(cartService.getCartByUserId(id));
    }

    @GetMapping("/count/{userId}")
    public ResponseEntity<Integer> countCartItems(
            @PathVariable Integer userId
    ) {
        return ResponseEntity.ok(
                cartService.countCartItemsByUserId(userId)
        );
    }

    @PostMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("POST WORKS");
    }

    @PostMapping("/add")
    public ResponseEntity<String> addCartItem(@RequestBody Cart cart) {
        System.out.println("cartItem = " + cart.getCart_item());
        System.out.println("price = " + cart.getPrice());
        System.out.println("coachId = " + cart.getCoach_id());
        System.out.println("userId = " + cart.getUser_id());
        cartService.addCartItem(
                cart.getCart_item(),
                cart.getPrice(),
                cart.getCoach_id(),
                cart.getUser_id()
        );

        return ResponseEntity.ok("Cart item added successfully");
    }

    @PutMapping("/updatecart/{id}")
    public ResponseEntity<String> updateCartItem(
            @PathVariable Integer id,
            @RequestParam String cartItem,
            @RequestParam BigDecimal price,
            @RequestParam(required = false) Integer coachId,
            @RequestParam Integer userId
    ) {

        cartService.updateCartItem(
                id,
                cartItem,
                price,
                coachId,
                userId
        );
        System.out.println("Caio comestai");
        return ResponseEntity.ok("Cart item updated successfully");
    }

    @DeleteMapping("/deletecart/{id}")
    public void deleteCartById(
            @PathVariable("id") Integer id
    ) {
        cartService.deleteCartById(id);
    }
}