package com.example.fitness.repositories;

import com.example.fitness.entitties.Cart;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface CartRepository extends CrudRepository<Cart, Integer> {

    @Query(
            value = "SELECT * FROM cart WHERE cart_id = ?1",
            nativeQuery = true
    )
    Cart findCartByCartId(Integer cartId);

    @Query(
            value = "SELECT * FROM cart WHERE user_id = ?1",
            nativeQuery = true
    )
    List<Cart> findCartByUserId(Integer userId);

    @Query(
            value = "SELECT * FROM cart",
            nativeQuery = true
    )
    List<Cart> getAllCartItems();

    @Transactional
    @Modifying
    @Query(
            value = "DELETE FROM cart WHERE cart_id = ?1",
            nativeQuery = true
    )
    void deleteCartByCartId(Integer cartId);

    @Transactional
    @Modifying
    @Query(
            value =
                    "INSERT INTO cart (" +
                            "cart_item, price, coach_id, user_id" +
                            ") VALUES (" +
                            "?1, ?2, ?3, ?4" +
                            ")",
            nativeQuery = true
    )
    void addCartItem(
            String cart_item,
            Float price,
            Integer coach_id,
            Integer user_id
    );

    @Transactional
    @Modifying
    @Query(
            value =
                    "UPDATE cart SET " +
                            "cart_item = ?2, " +
                            "price = ?3, " +
                            "coach_id = ?4, " +
                            "user_id = ?5 " +
                            "WHERE cart_id = ?1",
            nativeQuery = true
    )
    void updateCartItem(
            Integer cartId,
            String cartItem,
            BigDecimal price,
            Integer coachId,
            Integer userId
    );

    @Query(
            value = "SELECT COUNT(*) FROM cart WHERE user_id = ?1",
            nativeQuery = true
    )
    Integer countCartItemsByUserId(Integer userId);
}