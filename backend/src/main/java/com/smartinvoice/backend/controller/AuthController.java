package com.smartinvoice.backend.controller;

import com.smartinvoice.backend.dto.*;
import com.smartinvoice.backend.entity.Admin;
import com.smartinvoice.backend.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    
    @Autowired
    private AdminRepository adminRepository;
    
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        var admin = adminRepository.findByEmail(request.getEmail());
        
        if (admin.isEmpty()) {
            return ResponseEntity.ok(new AuthResponse(false, "Admin not found", false, null));
        }
        
        Admin adminUser = admin.get();
        
        if (!adminUser.isPasswordSet()) {
            return ResponseEntity.ok(new AuthResponse(false, "Password not set", false, request.getEmail()));
        }
        
        if (!adminUser.getPassword().equals(request.getPassword())) {
            return ResponseEntity.ok(new AuthResponse(false, "Invalid password", true, null));
        }
        
        return ResponseEntity.ok(new AuthResponse(true, "Login successful", true, request.getEmail()));
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
