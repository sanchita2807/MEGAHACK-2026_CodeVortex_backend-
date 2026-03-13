package com.smartinvoice.backend.controller;

import com.smartinvoice.backend.dto.*;
import com.smartinvoice.backend.entity.Invoice;
import com.smartinvoice.backend.entity.Product;
import com.smartinvoice.backend.repository.InvoiceRepository;
import com.smartinvoice.backend.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {
    
    @Autowired
    private InvoiceRepository invoiceRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @GetMapping("/stats")
    public ResponseEntity<DashboardStats> getStats() {
        Long totalItems = productRepository.sumTotalStock();
        List<Product> lowStock = productRepository.findLowStockProducts();
        Long invoicesCount = invoiceRepository.count();
        
        Double totalValue = productRepository.findAll().stream()
            .mapToDouble(p -> p.getStockLeft() * 100.0)
            .sum();
        
        return ResponseEntity.ok(new DashboardStats(
            totalItems != null ? totalItems : 0L,
            lowStock.size(),
            invoicesCount,
            totalValue
        ));
    }
    
    @GetMapping("/invoices")
    public ResponseEntity<List<InvoiceDTO>> getRecentInvoices() {
        List<Invoice> invoices = invoiceRepository.findTop10ByOrderByScanDateDesc();
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, hh:mm a");
        
        List<InvoiceDTO> dtos = invoices.stream()
            .map(inv -> new InvoiceDTO(
                inv.getId(),
                inv.getVendor(),
                inv.getScanDate().format(formatter),
                inv.getStatus(),
                inv.getItems(),
                String.format("%.0f", inv.getAmount())
            ))
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(dtos);
    }
    
    @GetMapping("/low-stock")
    public ResponseEntity<List<ProductDTO>> getLowStockProducts() {
        List<Product> products = productRepository.findLowStockProducts();
        
        List<ProductDTO> dtos = products.stream()
            .map(p -> new ProductDTO(p.getId(), p.getName(), p.getStockLeft(), p.getMinStock()))
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(dtos);
    }
    
    @PostMapping("/scan")
    public ResponseEntity<InvoiceDTO> simulateScan(@RequestBody InvoiceDTO request) {
        Invoice invoice = new Invoice();
        invoice.setVendor(request.getVendor());
        invoice.setScanDate(LocalDateTime.now());
        invoice.setStatus("Auto-Synced");
        invoice.setItems(request.getItems());
        invoice.setAmount(Double.parseDouble(request.getAmount()));
        
        Invoice saved = invoiceRepository.save(invoice);
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, hh:mm a");
        
        return ResponseEntity.ok(new InvoiceDTO(
            saved.getId(),
            saved.getVendor(),
            saved.getScanDate().format(formatter),
            saved.getStatus(),
            saved.getItems(),
            String.format("%.0f", saved.getAmount())
        ));
    }
    
    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportToExcel() {
        List<Product> products = productRepository.findAll();
        
        StringBuilder csv = new StringBuilder();
        csv.append("ID,Name,Stock Left,Min Stock\n");
        
        for (Product p : products) {
            csv.append(p.getId()).append(",")
               .append(p.getName()).append(",")
               .append(p.getStockLeft()).append(",")
               .append(p.getMinStock()).append("\n");
        }
        
        return ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=inventory.csv")
            .header("Content-Type", "text/csv")
            .body(csv.toString().getBytes());
    }
    
    @PostMapping("/sync/tally")
    public ResponseEntity<SyncResponse> syncWithTally() {
        // Simulate Tally sync
        return ResponseEntity.ok(new SyncResponse(true, "Successfully synced with Tally. " + 
            productRepository.count() + " products updated."));
    }
    
    @GetMapping("/export/report")
    public ResponseEntity<byte[]> exportReport() {
        List<Invoice> invoices = invoiceRepository.findAll();
        
        StringBuilder report = new StringBuilder();
        report.append("DASHBOARD REPORT\n\n");
        report.append("Total Invoices: ").append(invoices.size()).append("\n");
        report.append("Total Products: ").append(productRepository.count()).append("\n\n");
        report.append("Recent Invoices:\n");
        
        for (Invoice inv : invoices) {
            report.append(inv.getVendor()).append(" - ")
                  .append(inv.getItems()).append(" items - Rs.")
                  .append(inv.getAmount()).append("\n");
        }
        
        return ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=report.txt")
            .header("Content-Type", "text/plain")
            .body(report.toString().getBytes());
    }
    
    @GetMapping("/reorder-list")
    public ResponseEntity<byte[]> generateReorderList() {
        List<Product> lowStock = productRepository.findLowStockProducts();
        
        StringBuilder list = new StringBuilder();
        list.append("REORDER LIST\n\n");
        list.append("Products Below Minimum Stock:\n\n");
        
        for (Product p : lowStock) {
            int needed = p.getMinStock() - p.getStockLeft();
            list.append(p.getName()).append(" - ")
                .append("Current: ").append(p.getStockLeft())
                .append(", Min: ").append(p.getMinStock())
                .append(", Order: ").append(needed).append(" units\n");
        }
        
        return ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=reorder-list.txt")
            .header("Content-Type", "text/plain")
            .body(list.toString().getBytes());
    }
}
