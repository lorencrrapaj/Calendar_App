// src/main/java/com/example/calendar/controller/AuthController.java
package com.example.calendar.controller;

import com.example.calendar.dto.*;
import com.example.calendar.model.User;
import com.example.calendar.repository.UserRepository;
import com.example.calendar.security.TokenStore;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepo;
    private final PasswordEncoder encoder;
    private final TokenStore tokenStore;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterDTO dto) {
        if (userRepo.existsByEmail(dto.getEmail())) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("Email already in use");
        }
        User u = User.builder()
                .email(dto.getEmail())
                .passwordHash(encoder.encode(dto.getPassword()))
                .build();
        userRepo.save(u);
        return ResponseEntity.ok("User registered");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO dto) {
        var opt = userRepo.findByEmail(dto.getEmail());
        if (opt.isPresent()
                && encoder.matches(dto.getPassword(), opt.get().getPasswordHash())) {

            // now generate a true random token and remember it
            String jwt = tokenStore.create(opt.get().getEmail());
            return ResponseEntity.ok(new LoginResponse(jwt));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Invalid credentials");
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(value = "Authorization", required = false) String auth) {
        if (auth != null && auth.startsWith("Bearer ")) {
            tokenStore.invalidate(auth.substring(7));
        }
        return ResponseEntity.ok("Logged out");
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String,String>> me(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        // now Principal.getName() matches the email from the tokenStore
        return ResponseEntity.ok(Map.of("email", principal.getName()));
    }

    @PutMapping("/password")
    public ResponseEntity<?> changePassword(
            Principal principal,
            @RequestBody ChangePasswordDTO dto
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("User not authenticated");
        }
        String email = principal.getName();
        var opt = userRepo.findByEmail(email);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("User not found");
        }
        User u = opt.get();
        if (!encoder.matches(dto.getOldPassword(), u.getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid current password");
        }
        u.setPasswordHash(encoder.encode(dto.getNewPassword()));
        userRepo.save(u);
        return ResponseEntity.ok("Password changed");
    }
}
