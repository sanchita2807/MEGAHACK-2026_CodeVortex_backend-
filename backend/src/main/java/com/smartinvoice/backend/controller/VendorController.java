package com.smartinvoice.backend.controller;

import com.smartinvoice.backend.dto.VendorDTO;
import com.smartinvoice.backend.entity.Vendor;
import com.smartinvoice.backend.repository.VendorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/vendors")
public class VendorController {
    
    @Autowired
    private VendorRepository vendorRepository;
    
    @GetMapping
    public ResponseEntity<List<VendorDTO>> getAllVendors() {
        List<Vendor> vendors = vendorRepository.findAll();
        List<VendorDTO> vendorDTOs = vendors.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(vendorDTOs);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<VendorDTO> getVendorById(@PathVariable Long id) {
        return vendorRepository.findById(id)
            .map(vendor -> ResponseEntity.ok(convertToDTO(vendor)))
            .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<VendorDTO> createVendor(@RequestBody VendorDTO vendorDTO) {
        Vendor vendor = new Vendor();
        vendor.setVendorName(vendorDTO.getVendorName());
        vendor.setShopName(vendorDTO.getShopName());
        vendor.setPhoneNumber(vendorDTO.getPhoneNumber());
        vendor.setEmail(vendorDTO.getEmail());
        vendor.setUserType(vendorDTO.getUserType() != null ? vendorDTO.getUserType() : "VENDOR");
        vendor.setStatus(Vendor.VendorStatus.PENDING);
        
        Vendor savedVendor = vendorRepository.save(vendor);
        return ResponseEntity.ok(convertToDTO(savedVendor));
    }
    
    @PutMapping("/{id}/approve")
    public ResponseEntity<VendorDTO> approveVendor(@PathVariable Long id) {
        return vendorRepository.findById(id)
            .map(vendor -> {
                vendor.setStatus(Vendor.VendorStatus.APPROVED);
                Vendor updated = vendorRepository.save(vendor);
                return ResponseEntity.ok(convertToDTO(updated));
            })
            .orElse(ResponseEntity.notFound().build());
    }
    
    @PutMapping("/{id}/block")
    public ResponseEntity<VendorDTO> blockVendor(@PathVariable Long id) {
        return vendorRepository.findById(id)
            .map(vendor -> {
                vendor.setStatus(Vendor.VendorStatus.BLOCKED);
                Vendor updated = vendorRepository.save(vendor);
                return ResponseEntity.ok(convertToDTO(updated));
            })
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<List<VendorDTO>> getVendorsByStatus(@PathVariable String status) {
        try {
            Vendor.VendorStatus vendorStatus = Vendor.VendorStatus.valueOf(status.toUpperCase());
            List<Vendor> vendors = vendorRepository.findByStatus(vendorStatus);
            List<VendorDTO> vendorDTOs = vendors.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
            return ResponseEntity.ok(vendorDTOs);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    private VendorDTO convertToDTO(Vendor vendor) {
        return new VendorDTO(
            vendor.getId(),
            vendor.getVendorName(),
            vendor.getShopName(),
            vendor.getPhoneNumber(),
            vendor.getEmail(),
            vendor.getUserType(),
            vendor.getStatus().toString()
        );
    }
}
