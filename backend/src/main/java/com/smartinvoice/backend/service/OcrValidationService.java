package com.smartinvoice.backend.service;

import java.util.*;
import java.util.regex.*;

public class OcrValidationService {
    
    public static class ValidationResult {
        public boolean isValid;
        public List<String> errors;
        public List<String> warnings;
        public Map<String, Object> correctedData;
        
        public ValidationResult() {
            this.errors = new ArrayList<>();
            this.warnings = new ArrayList<>();
            this.correctedData = new HashMap<>();
            this.isValid = true;
        }
    }
    
    public ValidationResult validateInvoiceData(Map<String, Object> invoiceData) {
        ValidationResult result = new ValidationResult();
        
        // Validate vendor
        String vendor = (String) invoiceData.get("vendor");
        if (vendor == null || vendor.trim().isEmpty() || "Unknown Vendor".equals(vendor)) {
            result.warnings.add("Vendor name not clearly detected. Please verify manually.");
        }
        
        // Validate date
        String date = (String) invoiceData.get("invoiceDate");
        if (!isValidDate(date)) {
            result.warnings.add("Invoice date format unclear. Using current date.");
        }
        
        // Validate items
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) invoiceData.get("items");
        if (items == null || items.isEmpty()) {
            result.errors.add("No items detected in invoice. Please check image quality.");
            result.isValid = false;
        } else {
            validateItems(items, result);
        }
        
        // Validate totals
        Double totalAmount = (Double) invoiceData.get("totalAmount");
        Double tax = (Double) invoiceData.get("tax");
        Double fee = (Double) invoiceData.get("fee");
        
        validateTotals(items, totalAmount, tax, fee, result);
        
        return result;
    }
    
    private void validateItems(List<Map<String, Object>> items, ValidationResult result) {
        for (int i = 0; i < items.size(); i++) {
            Map<String, Object> item = items.get(i);
            
            String name = (String) item.get("name");
            Integer qty = (Integer) item.get("quantity");
            Double price = (Double) item.get("price");
            
            // Check item name
            if (name == null || name.trim().isEmpty() || name.length() < 2) {
                result.errors.add("Item " + (i + 1) + ": Invalid product name");
                result.isValid = false;
            }
            
            // Check quantity
            if (qty == null || qty <= 0) {
                result.errors.add("Item " + (i + 1) + ": Invalid quantity");
                result.isValid = false;
            }
            
            // Check price
            if (price == null || price < 0) {
                result.errors.add("Item " + (i + 1) + ": Invalid price");
                result.isValid = false;
            }
            
            // Check for suspicious patterns
            if (price > 1000000) {
                result.warnings.add("Item " + (i + 1) + ": Price seems unusually high (₹" + price + ")");
            }
            
            if (qty > 10000) {
                result.warnings.add("Item " + (i + 1) + ": Quantity seems unusually high (" + qty + ")");
            }
        }
    }
    
    private void validateTotals(List<Map<String, Object>> items, Double totalAmount, 
                               Double tax, Double fee, ValidationResult result) {
        if (items == null || items.isEmpty()) {
            return;
        }
        
        // Calculate expected total from items
        double calculatedTotal = 0;
        for (Map<String, Object> item : items) {
            Double price = (Double) item.get("price");
            Integer qty = (Integer) item.get("quantity");
            if (price != null && qty != null) {
                calculatedTotal += price * qty;
            }
        }
        
        // Validate total amount
        if (totalAmount == null || totalAmount <= 0) {
            result.warnings.add("Total amount not detected. Calculated from items: ₹" + calculatedTotal);
        } else {
            // Allow 5% variance for tax/fees
            double variance = Math.abs(totalAmount - calculatedTotal) / calculatedTotal * 100;
            if (variance > 10) {
                result.warnings.add("Total amount (₹" + totalAmount + ") differs significantly from calculated total (₹" + calculatedTotal + "). Variance: " + String.format("%.1f", variance) + "%");
            }
        }
        
        // Validate tax
        if (tax != null && tax < 0) {
            result.errors.add("Tax cannot be negative");
            result.isValid = false;
        }
        
        if (tax != null && tax > calculatedTotal * 0.5) {
            result.warnings.add("Tax (₹" + tax + ") seems unusually high compared to total");
        }
        
        // Validate fee
        if (fee != null && fee < 0) {
            result.errors.add("Fee cannot be negative");
            result.isValid = false;
        }
        
        if (fee != null && fee > calculatedTotal * 0.3) {
            result.warnings.add("Fee (₹" + fee + ") seems unusually high compared to total");
        }
    }
    
    private boolean isValidDate(String date) {
        if (date == null || date.isEmpty()) {
            return false;
        }
        
        // Check for date patterns
        Pattern datePattern = Pattern.compile("\\d{1,2}[-/]\\d{1,2}[-/]\\d{2,4}");
        return datePattern.matcher(date).find();
    }
    
    public double calculateItemTotal(List<Map<String, Object>> items) {
        double total = 0;
        if (items != null) {
            for (Map<String, Object> item : items) {
                Double price = (Double) item.get("price");
                Integer qty = (Integer) item.get("quantity");
                if (price != null && qty != null) {
                    total += price * qty;
                }
            }
        }
        return total;
    }
}
