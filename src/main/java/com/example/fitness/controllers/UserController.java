package com.example.fitness.controllers;

import com.example.fitness.entitties.User;
import com.example.fitness.configs.JwtUtil;
import com.example.fitness.services.Complexlogic;
import com.example.fitness.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequestMapping("/users")
@RestController
public class UserController {

    private final UserService userService;
    private final Complexlogic complexlogic;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserService userService,
                          Complexlogic complexlogic,
                          AuthenticationManager authenticationManager,
                          JwtUtil jwtUtil,
                          PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.complexlogic = complexlogic;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    // =========================
    // AUTHENTICATED ENDPOINTS
    // =========================

    @GetMapping("/me")
    public ResponseEntity<User> authenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal(); // assumes User implements UserDetails
        return ResponseEntity.ok(user);
    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<List<User>> allUsers() {
        return ResponseEntity.ok(userService.fetchAll());
    }
    @GetMapping("/demo")
    public ResponseEntity<String> userdemo() {
        return ResponseEntity.ok("bravooo");
    }
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public ResponseEntity<List<User>> getUserById(@PathVariable("id") Integer id) {
        return ResponseEntity.ok(userService.finduserbyid(id));
    }
    @GetMapping("/bmi/{id}")
    public Map<String, Object> getBmiById(@PathVariable Integer id) {
        return complexlogic.bmi(id);
    }
    // =========================
    // PUBLIC ENDPOINTS
    // =========================


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginRequest) {

        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();

        // 1️⃣ Validate input
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email and password are required"));
        }

        // 2️⃣ Check user credentials
        Optional<User> optionalUser = userService.getUserByEmail(email);
        if (optionalUser.isEmpty() || !password.equals(optionalUser.get().getPassword())) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid email or password"));
        }

        User user = optionalUser.get();

        // 3️⃣ Generate JWT token
        String jwtToken = jwtUtil.generateToken(user.getEmail());

        // 4️⃣ Return response
        return ResponseEntity.ok(Map.of(
                "token", jwtToken,
                "type", "Bearer",
                "user", Map.of(
                        "id", user.getUser_id(),
                        "email", user.getEmail(),
                        "firstname", user.getFirstname(),
                        "lastname", user.getLastname(),
                        "role",user.getRole()
                )
        ));
    }

    @PostMapping("/adduser")
    public ResponseEntity<?> addUser(@RequestBody User user) {
        // Hash password before saving
//        user.setPassword(passwordEncoder.encode(user.getPassword()));

        userService.adduser(
                user.getEmail(),
                user.getFirstname(),
                user.getGender(),
                user.getHeight(),
                user.getLastname(),
                user.getPassword(),
                user.getWeight(),
                user.getRole(),
                user.getAge(),
                user.getSmoke()
        );
        return ResponseEntity.ok(Map.of("message", "User added successfully"));
    }
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void deleteworksoutById(@PathVariable("id") Integer id) {
        userService.deleteuserbyId(id);
    }
}
