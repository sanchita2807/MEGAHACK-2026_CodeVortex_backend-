package com.smartinvoice.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendorDTO {
    private Long id;
    private String vendorName;
    private String shopName;
    private String phoneNumber;
    private String email;
    private String userType;
    private String status;
}
