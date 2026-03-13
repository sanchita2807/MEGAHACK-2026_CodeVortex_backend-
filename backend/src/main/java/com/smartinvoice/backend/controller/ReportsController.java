package com.smartinvoice.backend.controller;

import com.smartinvoice.backend.dto.ReportsDTO;
import com.smartinvoice.backend.entity.Invoice;
import com.smartinvoice.backend.repository.InvoiceRepository;
import com.smartinvoice.backend.repository.VendorRepository;
import com.smartinvoice.backend.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reports")
public class ReportsController {
    
    @Autowired
    private VendorRepository vendorRepository;
    
    @Autowired
    private InvoiceRepository invoiceRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @GetMapping("/analytics")
    public ResponseEntity<ReportsDTO> getAnalytics() {
        ReportsDTO reports = new ReportsDTO();
        
        // Total vendors
        reports.setTotalVendors(vendorRepository.count());
        
        // Total invoices
        List<Invoice> allInvoices = invoiceRepository.findAll();
        reports.setTotalInvoices((long) allInvoices.size());
        
        // Total products scanned
        reports.setTotalProductsScanned(productRepository.count());
        
        // Average invoice value
        Double avgValue = allInvoices.stream()
            .mapToDouble(Invoice::getAmount)
            .average()
            .orElse(0.0);
        reports.setAverageInvoiceValue(avgValue);
        
        // Invoices per vendor
        Map<String, Long> invoicesPerVendor = allInvoices.stream()
            .collect(Collectors.groupingBy(Invoice::getVendor, Collectors.counting()));
        reports.setInvoicesPerVendor(invoicesPerVendor);
        
        // Most scanned products from database
        List<Map<String, Object>> mostScanned = productRepository.findAll().stream()
            .limit(4)
            .map(product -> {
                Map<String, Object> map = new HashMap<>();
                map.put("name", product.getName());
                map.put("scans", product.getStockLevel());
                return map;
            })
            .collect(Collectors.toList());
        reports.setMostScannedProducts(mostScanned);
        
        // Monthly activity from invoices
        List<Map<String, Object>> monthly = allInvoices.stream()
            .collect(Collectors.groupingBy(
                invoice -> invoice.getScanDate().getMonth().toString(),
                Collectors.counting()
            ))
            .entrySet().stream()
            .map(entry -> {
                Map<String, Object> map = new HashMap<>();
                map.put("month", entry.getKey());
                map.put("invoices", entry.getValue());
                return map;
            })
            .collect(Collectors.toList());
        reports.setMonthlyActivity(monthly);
        
        return ResponseEntity.ok(reports);
    }
    
    @GetMapping("/vendor-summary")
    public ResponseEntity<Map<String, Object>> getVendorSummary() {
        Map<String, Object> summary = new HashMap<>();
        List<Invoice> invoices = invoiceRepository.findAll();
        
        Map<String, Long> vendorInvoices = invoices.stream()
            .collect(Collectors.groupingBy(Invoice::getVendor, Collectors.counting()));
        
        summary.put("vendorInvoices", vendorInvoices);
        summary.put("totalVendors", vendorInvoices.size());
        
        return ResponseEntity.ok(summary);
    }
    
    @GetMapping("/invoice-summary")
    public ResponseEntity<Map<String, Object>> getInvoiceSummary() {
        Map<String, Object> summary = new HashMap<>();
        List<Invoice> invoices = invoiceRepository.findAll();
        
        summary.put("totalInvoices", invoices.size());
        summary.put("totalAmount", invoices.stream().mapToDouble(Invoice::getAmount).sum());
        summary.put("averageAmount", invoices.stream().mapToDouble(Invoice::getAmount).average().orElse(0.0));
        summary.put("totalItems", invoices.stream().mapToInt(Invoice::getItems).sum());
        
        return ResponseEntity.ok(summary);
    }
}
