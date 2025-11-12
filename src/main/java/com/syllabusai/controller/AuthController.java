// AuthController.java - SIMPLE VERSION WITHOUT ROLE
package com.syllabusai.controller;

import com.syllabusai.model.User;
import com.syllabusai.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginData) {
        try {
            String email = loginData.get("email");
            String password = loginData.get("password");

            System.out.println("Login attempt for: " + email);

            Optional<User> userOpt = userRepository.findByEmail(email);

            if (userOpt.isPresent() && userOpt.get().getPassword().equals(password)) {
                User user = userOpt.get();

                // Create simple token
                String token = "demo-token-" + user.getId() + "-" + System.currentTimeMillis();

                // Build response WITHOUT ROLE
                Map<String, Object> response = new HashMap<>();
                response.put("token", token);
                response.put("user", Map.of(
                        "id", user.getId(),
                        "email", user.getEmail(),
                        "firstName", user.getFirstName(),
                        "lastName", user.getLastName()
                        // NO ROLE FIELD
                ));

                System.out.println("Login successful for: " + email);
                return ResponseEntity.ok(response);
            } else {
                System.out.println("Login failed for: " + email);
                return ResponseEntity.status(401).body("Invalid credentials");
            }

        } catch (Exception e) {
            System.err.println("Login error: " + e.getMessage());
            return ResponseEntity.badRequest().body("Login failed");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> userData) {
        try {
            String email = userData.get("email");
            String password = userData.get("password");
            String firstName = userData.get("firstName");
            String lastName = userData.get("lastName");

            // Check if user exists
            if (userRepository.findByEmail(email).isPresent()) {
                return ResponseEntity.badRequest().body("Email already registered");
            }

            // Create new user WITHOUT ROLE
            User user = User.builder()
                    .email(email)
                    .password(password)
                    .firstName(firstName)
                    .lastName(lastName)
                    .build();

            userRepository.save(user);

            // Generate token
            String token = "demo-token-" + user.getId() + "-" + System.currentTimeMillis();

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("user", Map.of(
                    "id", user.getId(),
                    "email", user.getEmail(),
                    "firstName", user.getFirstName(),
                    "lastName", user.getLastName()
                    // NO ROLE FIELD
            ));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Registration failed");
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body("Not authenticated");
            }

            String token = authHeader.substring(7);
            System.out.println("Token received: " + token);

            // Simple token validation
            if (token.startsWith("demo-token-")) {
                String[] parts = token.split("-");
                if (parts.length >= 3) {
                    Long userId = Long.parseLong(parts[2]);
                    Optional<User> user = userRepository.findById(userId);

                    if (user.isPresent()) {
                        return ResponseEntity.ok(Map.of(
                                "id", user.get().getId(),
                                "email", user.get().getEmail(),
                                "firstName", user.get().getFirstName(),
                                "lastName", user.get().getLastName()
                                // NO ROLE FIELD
                        ));
                    }
                }
            }

            return ResponseEntity.status(401).body("Invalid token");

        } catch (Exception e) {
            return ResponseEntity.status(401).body("Authentication failed");
        }
    }
}