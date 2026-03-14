package com.smartinvoice.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendorDTO {
    private Long id;
    private String name;
    private String shopName;
    private String phone;
    private String email;
    private String userType;
    private String status;
    
    // Getters for backward compatibility with old field names
    public String getVendorName() {
        return name;
    }
    
    public String getPhoneNumber() {
        return phone;
    }
}
