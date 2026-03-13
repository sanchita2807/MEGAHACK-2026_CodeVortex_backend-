package com.smartinvoice.backend.dto;

import lombok.Data;

@Data
public class SetPasswordRequest {
    private String email;
    private String password;
}
