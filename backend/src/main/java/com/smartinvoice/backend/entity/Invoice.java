package com.smartinvoice.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "invoices")
@Data
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String vendor;
    private LocalDateTime scanDate;
    private String status;
    private Integer items;
    private Double amount;
}
