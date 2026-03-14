package com.smartinvoice.backend.controller;

import com.smartinvoice.backend.entity.Invoice;
import com.smartinvoice.backend.entity.Product;
import com.smartinvoice.backend.repository.InvoiceRepository;
import com.smartinvoice.backend.repository.ProductRepository;
import com.smartinvoice.backend.service.OcrService;
import com.smartinvoice.backend.service.OcrValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/ocr")
@CrossOrigin(origins = "*")
public class OcrController {
    
    @Autowired
    private OcrService ocrService;
    
    @Autowired
    private InvoiceRepository invoiceRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    private final OcrValidationService validationService = new OcrValidationService();
    
    @PostMapping("/process")
    public ResponseEntity<Map<String, Object>> processInvoice(@RequestParam("image") MultipartFile imageFile) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (imageFile.isEmpty()) {
                response.put("success", false);
                response.put("message", "Image file is empty");
                return ResponseEntity.badRequest().body(response);
            }
            
            System.out.println("Processing invoice image: " + imageFile.getOriginalFilename());
            System.out.println("File size: " + imageFile.getSize() + " bytes");
            
            byte[] imageData = imageFile.getBytes();
            String extractedText = ocrService.extractTextFromImage(imageData);
            
            Map<String, Object> invoiceData = ocrService.parseInvoiceData(extractedText);
            
            // Validate extracted data
            OcrValidationService.ValidationResult validation = validationService.validateInvoiceData(invoiceData);
            
            response.put("validation", new HashMap<String, Object>() {{
                put("isValid", validation.isValid);
                put("errors", validation.errors);
                put("warnings", validation.warnings);
            }});
            
            if (!validation.isValid) {
                response.put("success", false);
                response.put("message", "Data validation failed. Please review errors.");
                response.put("vendor", invoiceData.get("vendor"));
                response.put("invoiceDate", invoiceData.get("invoiceDate"));
                response.put("items", invoiceData.get("items"));
                response.put("totalAmount", invoiceData.get("totalAmount"));
                response.put("tax", invoiceData.get("tax"));
                response.put("fee", invoiceData.get("fee"));
                response.put("rawText", invoiceData.get("rawText"));
                return ResponseEntity.ok(response);
            }
            
            // Save invoice
            Invoice invoice = new Invoice();
            invoice.setVendor((String) invoiceData.get("vendor"));
            invoice.setScanDate(LocalDateTime.now());
            invoice.setItems((Integer) invoiceData.get("numberOfItems"));
            invoice.setAmount(((Number) invoiceData.get("totalAmount")).doubleValue());
            invoice.setStatus("PROCESSED");
            
            Invoice savedInvoice = invoiceRepository.save(invoice);
            System.out.println("Invoice saved with ID: " + savedInvoice.getId());
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> items = (List<Map<String, Object>>) invoiceData.get("items");
            
            int itemsAdded = 0;
            for (Map<String, Object> item : items) {
                String productName = (String) item.get("name");
                Integer quantity = (Integer) item.get("quantity");
                Double price = (Double) item.get("price");
                
                System.out.println("Processing item: " + productName + ", qty: " + quantity + ", price: " + price);
                
                Optional<Product> existingProduct = productRepository.findByName(productName);
                
                if (existingProduct.isPresent()) {
                    Product product = existingProduct.get();
                    product.setStockLevel(product.getStockLevel() + quantity);
                    productRepository.save(product);
                    System.out.println("Updated product: " + productName);
                } else {
                    Product newProduct = Product.builder()
                        .name(productName)
                        .stockLevel(quantity)
                        .threshold(5)
                        .price(BigDecimal.valueOf(price != null ? price : 0))
                        .build();
                    productRepository.save(newProduct);
                    System.out.println("Created new product: " + productName);
                }
                itemsAdded++;
            }
            
            response.put("success", true);
            response.put("message", "Invoice processed successfully");
            response.put("invoiceId", savedInvoice.getId());
            response.put("vendor", invoiceData.get("vendor"));
            response.put("invoiceDate", invoiceData.get("invoiceDate"));
            response.put("items", items);
            response.put("itemsAdded", itemsAdded);
            response.put("totalAmount", invoiceData.get("totalAmount"));
            response.put("tax", invoiceData.get("tax"));
            response.put("fee", invoiceData.get("fee"));
            response.put("rawText", invoiceData.get("rawText"));
            
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            System.err.println("IO Error: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "File processing error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
