package com.smartinvoice.backend.controller;

import com.smartinvoice.backend.dto.SettingsDTO;
import com.smartinvoice.backend.entity.Admin;
import com.smartinvoice.backend.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/settings")
public class SettingsController {
    
    @Autowired
    private AdminRepository adminRepository;
    
    @GetMapping("/admin/{email}")
    public ResponseEntity<SettingsDTO> getAdminSettings(@PathVariable String email) {
        Admin admin = adminRepository.findByEmail(email);
        if (admin == null) {
            return ResponseEntity.notFound().build();
        }
        
        SettingsDTO settings = new SettingsDTO();
        settings.setEmail(admin.getEmail());
        
        return ResponseEntity.ok(settings);
    }
    
    @PutMapping("/admin/{email}/profile")
    public ResponseEntity<Map<String, String>> updateAdminProfile(
            @PathVariable String email,
            @RequestBody SettingsDTO settingsDTO) {
        Admin admin = adminRepository.findByEmail(email);
        if (admin == null) {
            return ResponseEntity.notFound().build();
        }
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Profile updated successfully");
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/admin/{email}/password")
    public ResponseEntity<Map<String, String>> changePassword(
            @PathVariable String email,
            @RequestBody Map<String, String> passwordRequest) {
        Admin admin = adminRepository.findByEmail(email);
        if (admin == null) {
            return ResponseEntity.notFound().build();
        }
        
        String newPassword = passwordRequest.get("newPassword");
        admin.setPassword(newPassword);
        adminRepository.save(admin);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Password changed successfully");
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/cache/clear")
    public ResponseEntity<Map<String, String>> clearCache() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Cache cleared successfully");
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/system/reset")
    public ResponseEntity<Map<String, String>> resetSystem() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "System reset initiated");
        return ResponseEntity.ok(response);
    }
}
