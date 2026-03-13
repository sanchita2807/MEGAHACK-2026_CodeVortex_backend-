package com.smartinvoice.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportsDTO {
    private Long totalVendors;
    private Long totalInvoices;
    private Long totalProductsScanned;
    private Double averageInvoiceValue;
    private Map<String, Long> invoicesPerVendor;
    private List<Map<String, Object>> mostScannedProducts;
    private List<Map<String, Object>> monthlyActivity;
}
