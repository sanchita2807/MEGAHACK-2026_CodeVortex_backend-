package com.smartinvoice.backend.controller;

import com.smartinvoice.backend.dto.InvoiceDetailDTO;
import com.smartinvoice.backend.entity.Invoice;
import com.smartinvoice.backend.repository.InvoiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {
    
    @Autowired
    private InvoiceRepository invoiceRepository;
    
    @GetMapping
    public ResponseEntity<List<InvoiceDetailDTO>> getAllInvoices() {
        List<Invoice> invoices = invoiceRepository.findAll();
        List<InvoiceDetailDTO> invoiceDTOs = invoices.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(invoiceDTOs);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<InvoiceDetailDTO> getInvoiceById(@PathVariable Long id) {
        return invoiceRepository.findById(id)
            .map(invoice -> ResponseEntity.ok(convertToDTO(invoice)))
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/vendor/{vendorName}")
    public ResponseEntity<List<InvoiceDetailDTO>> getInvoicesByVendor(@PathVariable String vendorName) {
        List<Invoice> invoices = invoiceRepository.findByVendor(vendorName);
        List<InvoiceDetailDTO> invoiceDTOs = invoices.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(invoiceDTOs);
    }
    
    @GetMapping("/date-range")
    public ResponseEntity<List<InvoiceDetailDTO>> getInvoicesByDateRange(
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {
        List<Invoice> invoices = invoiceRepository.findByScanDateBetween(startDate, endDate);
        List<InvoiceDetailDTO> invoiceDTOs = invoices.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(invoiceDTOs);
    }
    
    @PostMapping
    public ResponseEntity<InvoiceDetailDTO> createInvoice(@RequestBody InvoiceDetailDTO invoiceDTO) {
        Invoice invoice = new Invoice();
        invoice.setVendor(invoiceDTO.getVendorName());
        invoice.setScanDate(invoiceDTO.getInvoiceDate());
        invoice.setItems(invoiceDTO.getNumberOfItems());
        invoice.setAmount(invoiceDTO.getTotalAmount());
        invoice.setStatus(invoiceDTO.getStatus() != null ? invoiceDTO.getStatus() : "PENDING");
        
        Invoice savedInvoice = invoiceRepository.save(invoice);
        return ResponseEntity.ok(convertToDTO(savedInvoice));
    }
    
    private InvoiceDetailDTO convertToDTO(Invoice invoice) {
        return new InvoiceDetailDTO(
            invoice.getId(),
            invoice.getVendor(),
            invoice.getScanDate(),
            invoice.getItems(),
            invoice.getAmount(),
            invoice.getStatus()
        );
    }
}
