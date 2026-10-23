package com.example.fitness.dto;

public class MyCoachDto {

    private boolean assigned;
    private Integer cartId;
    private Integer coachId;
    private String name;
    private Float monthlyPrice;

    public boolean isAssigned() { return assigned; }
    public void setAssigned(boolean assigned) { this.assigned = assigned; }

    public Integer getCartId() { return cartId; }
    public void setCartId(Integer cartId) { this.cartId = cartId; }

    public Integer getCoachId() { return coachId; }
    public void setCoachId(Integer coachId) { this.coachId = coachId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Float getMonthlyPrice() { return monthlyPrice; }
    public void setMonthlyPrice(Float monthlyPrice) { this.monthlyPrice = monthlyPrice; }
}