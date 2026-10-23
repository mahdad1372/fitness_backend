package com.example.fitness.services;

import com.example.fitness.dto.CoachSummaryDto;
import com.example.fitness.dto.MyCoachDto;
import com.example.fitness.entitties.Cart;
import com.example.fitness.entitties.User;
import com.example.fitness.repositories.CartRepository;
import com.example.fitness.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * One-coach-per-student: reads/writes go through the EXISTING Cart table
 * (cart_item = "PRIVATE COACH", coach_id, price, user_id) - the same table
 * the sidebar (hasPrivateCoach) and Chat already use to know which coach a
 * student has. There is deliberately no new column on User for this: this
 * app has no fixed-price coach catalog yet, prices are set per Cart row at
 * subscribe time, and reusing Cart means zero database migration is needed.
 */
@Service
@RequiredArgsConstructor
public class CoachSubscriptionService {

    private static final String PRIVATE_COACH = "PRIVATE COACH";

    private final CartRepository cartRepository;
    private final UserRepository userRepository;

    public List<CoachSummaryDto> listCoaches() {
        return userRepository.findByRole("COACH").stream()
                .map(this::toSummary)
                .collect(Collectors.toList());
    }

    public MyCoachDto getMyCoach(Integer studentId) {
        MyCoachDto dto = new MyCoachDto();
        Cart coachCart = findCoachCart(studentId);
        if (coachCart == null) {
            dto.setAssigned(false);
            return dto;
        }
        dto.setAssigned(true);
        dto.setCartId(coachCart.getCartId());
        dto.setCoachId(coachCart.getCoach_id());
        dto.setMonthlyPrice(coachCart.getPrice());
        nameOf(coachCart.getCoach_id()).ifPresent(dto::setName);
        return dto;
    }

    // Used by SessionRequestService.create() to auto-fill the coach on a
    // new booking - returns null if the student has no active coach yet.
    public Integer getAssignedCoachId(Integer studentId) {
        Cart coachCart = findCoachCart(studentId);
        return coachCart != null ? coachCart.getCoach_id() : null;
    }

    private Cart findCoachCart(Integer studentId) {
        List<Cart> carts = cartRepository.findCartByUserId(studentId);
        if (carts == null) return null;
        return carts.stream()
                .filter(c -> PRIVATE_COACH.equals(c.getCart_item()))
                .findFirst()
                .orElse(null);
    }

    private java.util.Optional<String> nameOf(Integer userId) {
        List<User> matches = userRepository.findByUser_id(userId);
        if (matches.isEmpty()) return java.util.Optional.empty();
        User u = matches.get(0);
        return java.util.Optional.of(u.getFirstname() + " " + u.getLastname());
    }

    private CoachSummaryDto toSummary(User coach) {
        CoachSummaryDto dto = new CoachSummaryDto();
        dto.setCoachId(coach.getUser_id());
        dto.setName(coach.getFirstname() + " " + coach.getLastname());
        return dto;
    }
}