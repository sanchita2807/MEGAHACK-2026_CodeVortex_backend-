package com.smartinvoice.backend.controller;

import com.smartinvoice.backend.dto.*;
import com.smartinvoice.backend.model.User;
import com.smartinvoice.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    
    @Autowired
    private UserRepository userRepository;
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body(new AuthResponse(false, "Email already registered", false, null));
        }
        
        User user = User.builder()
            .name(request.getName())
            .email(request.getEmail())
            .phone(request.getPhone())
            .shopName(request.getShopName())
            .businessType(request.getBusinessType())
            .password(request.getPassword())
            .userType(0)
            .passwordSet(true)
            .build();
        
        userRepository.save(user);
        
        String token = UUID.randomUUID().toString();
        
        RegisterResponse response = new RegisterResponse();
        response.setSuccess(true);
        response.setMessage("Registration successful! Please login to continue.");
        response.setToken(token);
        response.setName(user.getName());
        response.setShopName(user.getShopName());
        response.setRole("User");
        response.setPasswordSet(true);
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        System.out.println("Admin login attempt for email: " + request.getEmail());
        
        var userOpt = userRepository.findByEmail(request.getEmail());
        
        if (userOpt.isEmpty()) {
            System.out.println("User not found: " + request.getEmail());
            return ResponseEntity.badRequest().body(new AuthResponse(false, "Email not found", false, request.getEmail()));
        }
        
        User user = userOpt.get();
        System.out.println("User found - Email: " + user.getEmail() + ", UserType: " + user.getUserType() + ", Password Match: " + user.getPassword().equals(request.getPassword()));
        
        if (!user.getPassword().equals(request.getPassword())) {
            System.out.println("Password mismatch for: " + request.getEmail());
            return ResponseEntity.badRequest().body(new AuthResponse(false, "Invalid password", true, null));
        }
        
        // Check if user is admin (userType must be 1)
        System.out.println("Checking userType: " + user.getUserType() + " (should be 1 for admin)");
        if (user.getUserType() != 1) {
            System.out.println("Access denied - userType is " + user.getUserType() + ", not 1");
            return ResponseEntity.status(403).body(new AuthResponse(false, "Only admin users can login here", false, null));
        }
        
        System.out.println("Admin login successful for: " + request.getEmail());
        String token = UUID.randomUUID().toString();
        
        RegisterResponse response = new RegisterResponse();
        response.setSuccess(true);
        response.setMessage("Login successful");
        response.setToken(token);
        response.setName(user.getName());
        response.setShopName(user.getShopName());
        response.setRole("Admin");
        response.setPasswordSet(user.isPasswordSet());
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/login-user")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest request) {
        System.out.println("User login attempt for email: " + request.getEmail());
        
        var userOpt = userRepository.findByEmail(request.getEmail());
        
        if (userOpt.isEmpty()) {
            System.out.println("User not found: " + request.getEmail());
            return ResponseEntity.badRequest().body(new AuthResponse(false, "Email not found", false, request.getEmail()));
        }
        
        User user = userOpt.get();
        System.out.println("User found - Email: " + user.getEmail() + ", UserType: " + user.getUserType() + ", Password Match: " + user.getPassword().equals(request.getPassword()));
        
        if (!user.getPassword().equals(request.getPassword())) {
            System.out.println("Password mismatch for: " + request.getEmail());
            return ResponseEntity.badRequest().body(new AuthResponse(false, "Invalid password", true, null));
        }
        
        // Check if user is regular user (userType must be 0)
        System.out.println("Checking userType: " + user.getUserType() + " (should be 0 for regular user)");
        if (user.getUserType() != 0) {
            System.out.println("Access denied - userType is " + user.getUserType() + ", not 0");
            return ResponseEntity.status(403).body(new AuthResponse(false, "Only regular users can login here", false, null));
        }
        
        System.out.println("User login successful for: " + request.getEmail());
        String token = UUID.randomUUID().toString();
        
        RegisterResponse response = new RegisterResponse();
        response.setSuccess(true);
        response.setMessage("Login successful");
        response.setToken(token);
        response.setName(user.getName());
        response.setShopName(user.getShopName());
        response.setRole("User");
        response.setPasswordSet(user.isPasswordSet());
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/set-password")
    public ResponseEntity<AuthResponse> setPassword(@RequestBody SetPasswordRequest request) {
        var userOpt = userRepository.findByEmail(request.getEmail());
        
        if (userOpt.isEmpty()) {
            User newUser = User.builder()
                .email(request.getEmail())
                .password(request.getPassword())
                .userType(1)
                .passwordSet(true)
                .name("Admin")
                .phone("")
                .shopName("Admin Panel")
                .businessType("Admin")
                .build();
            userRepository.save(newUser);
            System.out.println("New admin user created: " + request.getEmail());
            return ResponseEntity.ok(new AuthResponse(true, "Password set successfully", true, request.getEmail()));
        }
        
        User user = userOpt.get();
        user.setPassword(request.getPassword());
        user.setPasswordSet(true);
        userRepository.save(user);
        System.out.println("Password updated for: " + request.getEmail());
        
        return ResponseEntity.ok(new AuthResponse(true, "Password updated successfully", true, request.getEmail()));
    }
    
    @PostMapping("/check-email")
    public ResponseEntity<AuthResponse> checkEmail(@RequestBody LoginRequest request) {
        var userOpt = userRepository.findByEmail(request.getEmail());
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.ok(new AuthResponse(false, "Email not found", false, request.getEmail()));
        }
        
        User user = userOpt.get();
        return ResponseEntity.ok(new AuthResponse(true, "Email exists", user.isPasswordSet(), request.getEmail()));
    }
}
