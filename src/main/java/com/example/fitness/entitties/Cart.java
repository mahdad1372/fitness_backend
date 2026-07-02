package com.example.fitness.entitties;

import jakarta.persistence.*;

@Entity
@Table(name = "cart")
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer cart_id;
    @Column(nullable = false)
    private String cart_item;
    @Column(nullable = false)
    private Float price;

    @Column(nullable = false)
    private Integer coach_id;

    @Column(nullable = false)
    private Integer user_id;

    public Float getPrice() {
        return price;
    }

    public Integer getCartId() {
        return cart_id;
    }

    public Integer getCoach_id() {
        return coach_id;
    }

    public String getCart_item() {
        return cart_item;
    }

    public Integer getUser_id() {
        return user_id;
    }
}
