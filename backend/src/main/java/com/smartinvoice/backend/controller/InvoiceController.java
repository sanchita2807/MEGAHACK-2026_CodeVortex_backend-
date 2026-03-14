package com.smartinvoice.backend.controller;

import com.smartinvoice.backend.entity.Invoice;
import com.smartinvoice.backend.entity.Product;
import com.smartinvoice.backend.repository.InvoiceRepository;
import com.smartinvoice.backend.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/invoice")
@CrossOrigin(origins = "*")
public class InvoiceController {
    
    @Autowired
    private InvoiceRepository invoiceRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    /**
     * Save invoice with manually entered data
     * POST /api/invoice/save
     * 
     * Request body:
     * {
     *   "vendor": "Bakery Shop",
     *   "invoiceDate": "04/15/2024",
     *   "totalAmount": 150.50,
     *   "tax": 10.0,
     *   "fee": 5.0,
     *   "items": [
     *     {
     *       "name": "Cupcake Assortment",
     *       "quantity": 2,
     *       "price": 30.0
     *     },
     *     {
     *       "name": "Custom Birthday Cake",
     *       "quantity": 1,
     *       "price": 85.0
     *     }
     *   ]
     * }
     */
    @PostMapping("/save")
    public ResponseEntity<Map<String, Object>> saveInvoice(@RequestBody Map<String, Object> invoiceData) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String vendor = (String) invoiceData.get("vendor");
            String invoiceDate = (String) invoiceData.get("invoiceDate");
            Double totalAmount = ((Number) invoiceData.get("totalAmount")).doubleValue();
            Double tax = invoiceData.get("tax") != null ? ((Number) invoiceData.get("tax")).doubleValue() : 0.0;
            Double fee = invoiceData.get("fee") != null ? ((Number) invoiceData.get("fee")).doubleValue() : 0.0;
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> items = (List<Map<String, Object>>) invoiceData.get("items");
            
            if (items == null || items.isEmpty()) {
                response.put("success", false);
                response.put("message", "No items provided");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Save invoice
            Invoice invoice = new Invoice();
            invoice.setVendor(vendor);
            invoice.setScanDate(LocalDateTime.now());
            invoice.setItems(items.size());
            invoice.setAmount(totalAmount);
            invoice.setStatus("PROCESSED");
            
            Invoice savedInvoice = invoiceRepository.save(invoice);
            System.out.println("Invoice saved with ID: " + savedInvoice.getId());
            
            // Add items to inventory
            int itemsAdded = 0;
            for (Map<String, Object> item : items) {
                String productName = (String) item.get("name");
                Integer quantity = ((Number) item.get("quantity")).intValue();
                Double price = ((Number) item.get("price")).doubleValue();
                
                System.out.println("Processing item: " + productName + ", qty: " + quantity + ", price: " + price);
                
                // Check if product exists
                Optional<Product> existingProduct = productRepository.findByName(productName);
                
                if (existingProduct.isPresent()) {
                    // Update existing product
                    Product product = existingProduct.get();
                    product.setStockLevel(product.getStockLevel() + quantity);
                    productRepository.save(product);
                    System.out.println("Updated product: " + productName);
                } else {
                    // Create new product
                    Product newProduct = Product.builder()
                        .name(productName)
                        .stockLevel(quantity)
                        .threshold(5)
                        .price(BigDecimal.valueOf(price))
                        .build();
                    productRepository.save(newProduct);
                    System.out.println("Created new product: " + productName);
                }
                itemsAdded++;
            }
            
            response.put("success", true);
            response.put("message", "Invoice saved successfully");
            response.put("invoiceId", savedInvoice.getId());
            response.put("itemsAdded", itemsAdded);
            response.put("totalAmount", totalAmount);
            response.put("tax", tax);
            response.put("fee", fee);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Error saving invoice: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
