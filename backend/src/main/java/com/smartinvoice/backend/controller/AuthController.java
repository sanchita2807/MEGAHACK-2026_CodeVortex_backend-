package com.smartinvoice.backend.controller;

import com.smartinvoice.backend.dto.*;
import com.smartinvoice.backend.entity.Admin;
import com.smartinvoice.backend.model.User;
import com.smartinvoice.backend.repository.AdminRepository;
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
    private AdminRepository adminRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        // Check if user already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body(new AuthResponse(false, "Email already registered", false, null));
        }
        
        // Create new user
        User user = User.builder()
            .name(request.getName())
            .email(request.getEmail())
            .phone(request.getPhone())
            .shopName(request.getShopName())
            .businessType(request.getBusinessType())
            .password(request.getPassword())
            .build();
        
        userRepository.save(user);
        
        // Generate a simple token (in production, use JWT)
        String token = UUID.randomUUID().toString();
        
        return ResponseEntity.ok(new RegisterResponse(
            true,
            "Registration successful! Please login to continue.",
            token,
            user.getName(),
            user.getShopName()
        ));
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        // Check user repository first
        var userOpt = userRepository.findByEmail(request.getEmail());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getPassword().equals(request.getPassword())) {
                String token = UUID.randomUUID().toString();
                return ResponseEntity.ok(new RegisterResponse(
                    true,
                    "Login successful",
                    token,
                    user.getName(),
                    user.getShopName()
                ));
            } else {
                return ResponseEntity.badRequest().body(new AuthResponse(false, "Invalid password", true, null));
            }
        }
        
        // Fallback to admin login
        var admin = adminRepository.findByEmail(request.getEmail());
        
        if (admin.isEmpty()) {
            return ResponseEntity.badRequest().body(new AuthResponse(false, "Email not found", false, request.getEmail()));
        }
        
        Admin adminUser = admin.get();
        
        if (!adminUser.isPasswordSet()) {
            return ResponseEntity.ok(new AuthResponse(false, "Password not set", false, request.getEmail()));
        }
        
        if (!adminUser.getPassword().equals(request.getPassword())) {
            return ResponseEntity.badRequest().body(new AuthResponse(false, "Invalid password", true, null));
        }
        
        String token = UUID.randomUUID().toString();
        return ResponseEntity.ok(new RegisterResponse(true, "Login successful", token, "Admin", "Admin Panel"));
    }
    
    @PostMapping("/set-password")
    public ResponseEntity<AuthResponse> setPassword(@RequestBody SetPasswordRequest request) {
        var admin = adminRepository.findByEmail(request.getEmail());
        
        if (admin.isEmpty()) {
            Admin newAdmin = new Admin();
            newAdmin.setEmail(request.getEmail());
            newAdmin.setPassword(request.getPassword());
            newAdmin.setPasswordSet(true);
            adminRepository.save(newAdmin);
            return ResponseEntity.ok(new AuthResponse(true, "Password set successfully", true, request.getEmail()));
        }
        
        Admin adminUser = admin.get();
        adminUser.setPassword(request.getPassword());
        adminUser.setPasswordSet(true);
        adminRepository.save(adminUser);
        
        return ResponseEntity.ok(new AuthResponse(true, "Password updated successfully", true, request.getEmail()));
    }
    
    @PostMapping("/check-email")
    public ResponseEntity<AuthResponse> checkEmail(@RequestBody LoginRequest request) {
        var admin = adminRepository.findByEmail(request.getEmail());
        
        if (admin.isEmpty()) {
            return ResponseEntity.ok(new AuthResponse(false, "Email not found", false, request.getEmail()));
        }
        
        return ResponseEntity.ok(new AuthResponse(true, "Email exists", admin.get().isPasswordSet(), request.getEmail()));
    }
}
