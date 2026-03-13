package com.smartinvoice.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceDTO {
    private Long id;
    private String vendor;
    private String date;
    private String status;
    private Integer items;
    private String amount;
}
