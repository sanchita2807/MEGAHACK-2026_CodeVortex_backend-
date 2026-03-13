package com.smartinvoice.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RegisterResponse {
    private boolean success;
    private String message;
    private String token;
    private String name;
    private String shopName;
}
