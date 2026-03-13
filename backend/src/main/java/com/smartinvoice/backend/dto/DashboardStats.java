package com.smartinvoice.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DashboardStats {
    private Long totalItems;
    private Integer lowStockAlerts;
    private Long invoicesScanned;
    private Double totalInventoryValue;
}
