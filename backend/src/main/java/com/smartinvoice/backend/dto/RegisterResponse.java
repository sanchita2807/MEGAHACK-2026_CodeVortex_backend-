package com.smartinvoice.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegisterResponse {
    private boolean success;
    private String message;
    private String token;
    private String name;
    private String shopName;
    private String role;
    private Boolean passwordSet;
}
