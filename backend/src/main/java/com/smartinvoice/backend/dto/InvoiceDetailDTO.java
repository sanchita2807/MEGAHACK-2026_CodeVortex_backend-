package com.smartinvoice.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceDetailDTO {
    private Long id;
    private String vendorName;
    private LocalDateTime invoiceDate;
    private Integer numberOfItems;
    private Double totalAmount;
    private String status;
}
