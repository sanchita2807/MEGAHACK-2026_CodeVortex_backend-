package com.smartinvoice.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "vendors")
@Data
public class Vendor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String vendorName;
    
    @Column(nullable = false)
    private String shopName;
    
    @Column(nullable = false)
    private String phoneNumber;
    
    @Column(unique = true)
    private String email;
    
    @Column(nullable = false)
    private String userType = "VENDOR";
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VendorStatus status = VendorStatus.PENDING;
    
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    public enum VendorStatus {
        PENDING, APPROVED, BLOCKED
    }
}
