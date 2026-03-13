package com.smartinvoice.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SyncResponse {
    private boolean success;
    private String message;
}
