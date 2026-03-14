package com.smartinvoice.backend.controller;

import com.smartinvoice.backend.dto.VendorDTO;
import com.smartinvoice.backend.model.User;
import com.smartinvoice.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/vendors")
@CrossOrigin(origins = "*")
public class VendorController {
    
    @Autowired
    private UserRepository userRepository;
    
    @GetMapping
    public ResponseEntity<List<VendorDTO>> getAllVendors() {
        List<User> users = userRepository.findAll();
        List<VendorDTO> vendorDTOs = users.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(vendorDTOs);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<VendorDTO> getVendorById(@PathVariable Long id) {
        return userRepository.findById(id)
            .map(user -> ResponseEntity.ok(convertToDTO(user)))
            .orElse(ResponseEntity.notFound().build());
    }
    
    @PutMapping("/{id}/approve")
    public ResponseEntity<VendorDTO> approveVendor(@PathVariable Long id) {
        return userRepository.findById(id)
            .map(user -> {
                user.setUserType(1);
                User updated = userRepository.save(user);
                return ResponseEntity.ok(convertToDTO(updated));
            })
            .orElse(ResponseEntity.notFound().build());
    }
    
    @PutMapping("/{id}/block")
    public ResponseEntity<VendorDTO> blockVendor(@PathVariable Long id) {
        return userRepository.findById(id)
            .map(user -> {
                user.setUserType(2);
                User updated = userRepository.save(user);
                return ResponseEntity.ok(convertToDTO(updated));
            })
            .orElse(ResponseEntity.notFound().build());
    }
    
    private VendorDTO convertToDTO(User user) {
        String status = "pending";
        if (user.getUserType() == 1) {
            status = "approved";
        } else if (user.getUserType() == 2) {
            status = "blocked";
        }
        
        return new VendorDTO(
            user.getId(),
            user.getName(),
            user.getShopName(),
            user.getPhone(),
            user.getEmail(),
            user.getUserType().toString(),
            status
        );
    }
}
