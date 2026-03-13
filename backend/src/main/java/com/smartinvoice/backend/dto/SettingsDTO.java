package com.smartinvoice.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SettingsDTO {
    private String fullName;
    private String email;
    private String phone;
    private Boolean emailNotifications;
    private Boolean autoSync;
    private Boolean maintenanceMode;
    private String twoFactorAuth;
    private Integer sessionTimeout;
    private Boolean ipWhitelist;
}
