package com.smartinvoice.backend.controller;

import com.smartinvoice.backend.dto.*;
import com.smartinvoice.backend.entity.Invoice;
import com.smartinvoice.backend.entity.Product;
import com.smartinvoice.backend.repository.InvoiceRepository;
import com.smartinvoice.backend.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
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
        Long totalItems = productRepository.findAll().stream()
            .mapToLong(p -> p.getStockLevel().longValue())
            .sum();
        List<Product> lowStock = productRepository.findByStockLevelLessThanEqual(10);
        Long invoicesCount = invoiceRepository.count();
        
        Double totalValue = productRepository.findAll().stream()
            .mapToDouble(p -> p.getStockLevel() * p.getPrice().doubleValue())
            .sum();
        
        return ResponseEntity.ok(new DashboardStats(
            totalItems,
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
        List<Product> products = productRepository.findByStockLevelLessThanEqual(10);
        
        List<ProductDTO> dtos = products.stream()
            .map(p -> new ProductDTO(p.getId(), p.getName(), p.getStockLevel(), p.getThreshold(), p.getPrice()))
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
        try {
            List<Product> products = productRepository.findAll();
            
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Inventory");
            
            // Header
            Row headerRow = sheet.createRow(0);
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            
            String[] headers = {"ID", "Name", "Stock Level", "Threshold", "Price"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Data
            int rowNum = 1;
            for (Product p : products) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(p.getId());
                row.createCell(1).setCellValue(p.getName());
                row.createCell(2).setCellValue(p.getStockLevel());
                row.createCell(3).setCellValue(p.getThreshold());
                row.createCell(4).setCellValue(p.getPrice().doubleValue());
            }
            
            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();
            
            HttpHeaders headersResponse = new HttpHeaders();
            headersResponse.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headersResponse.setContentDispositionFormData("attachment", "inventory.xlsx");
            
            return ResponseEntity.ok()
                .headers(headersResponse)
                .body(outputStream.toByteArray());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping("/sync/tally")
    public ResponseEntity<SyncResponse> syncWithTally() {
        // Simulate Tally sync
        return ResponseEntity.ok(new SyncResponse(true, "Successfully synced with Tally. " + 
            productRepository.count() + " products updated."));
    }
    
    @GetMapping("/export/report")
    public ResponseEntity<byte[]> exportReport() {
        try {
            List<Invoice> invoices = invoiceRepository.findAll();
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);
            
            // Title
            document.add(new Paragraph("DASHBOARD REPORT")
                .setFontSize(20)
                .setBold());
            document.add(new Paragraph("\n"));
            
            // Summary
            document.add(new Paragraph("Total Invoices: " + invoices.size()));
            document.add(new Paragraph("Total Products: " + productRepository.count()));
            document.add(new Paragraph("\n"));
            
            // Recent Invoices
            document.add(new Paragraph("Recent Invoices:").setBold());
            
            Table table = new Table(UnitValue.createPercentArray(new float[]{2, 1, 2}));
            table.setWidth(UnitValue.createPercentValue(100));
            
            table.addHeaderCell("Vendor");
            table.addHeaderCell("Items");
            table.addHeaderCell("Amount");
            
            for (Invoice inv : invoices) {
                table.addCell(inv.getVendor());
                table.addCell(String.valueOf(inv.getItems()));
                table.addCell("Rs. " + String.format("%.2f", inv.getAmount()));
            }
            
            document.add(table);
            document.close();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "report.pdf");
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(baos.toByteArray());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/reorder-list")
    public ResponseEntity<byte[]> generateReorderList() {
        try {
            List<Product> lowStock = productRepository.findByStockLevelLessThanEqual(10);
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);
            
            // Title
            document.add(new Paragraph("REORDER LIST")
                .setFontSize(20)
                .setBold());
            document.add(new Paragraph("\n"));
            
            document.add(new Paragraph("Products Below Minimum Stock:").setBold());
            document.add(new Paragraph("\n"));
            
            Table table = new Table(UnitValue.createPercentArray(new float[]{3, 1, 1, 1}));
            table.setWidth(UnitValue.createPercentValue(100));
            
            table.addHeaderCell("Product Name");
            table.addHeaderCell("Current");
            table.addHeaderCell("Min");
            table.addHeaderCell("Order");
            
            for (Product p : lowStock) {
                int needed = p.getThreshold() - p.getStockLevel();
                table.addCell(p.getName());
                table.addCell(String.valueOf(p.getStockLevel()));
                table.addCell(String.valueOf(p.getThreshold()));
                table.addCell(needed + " units");
            }
            
            document.add(table);
            document.close();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "reorder-list.pdf");
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(baos.toByteArray());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
