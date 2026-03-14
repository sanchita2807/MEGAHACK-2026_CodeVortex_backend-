package com.smartinvoice.backend.controller;

import com.smartinvoice.backend.entity.Product;
import com.smartinvoice.backend.service.FuzzyMatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/fuzzy-match")
@CrossOrigin(origins = "*")
public class FuzzyMatchController {
    
    @Autowired
    private FuzzyMatchService fuzzyMatchService;
    
    /**
     * Find the best matching product for an OCR product name
     * GET /api/fuzzy-match/find?productName=Cupcake%20Assortment
     * 
     * Response:
     * {
     *   "success": true,
     *   "matched": true,
     *   "product": {
     *     "id": 1,
     *     "name": "Cupcake Assortment",
     *     "stockLevel": 10,
     *     "threshold": 5,
     *     "price": 30.00
     *   },
     *   "similarity": 0.95,
     *   "similarityPercentage": "95.00%",
     *   "message": "Found matching product"
     * }
     */
    @GetMapping("/find")
    public ResponseEntity<Map<String, Object>> findMatchingProduct(
            @RequestParam(value = "productName") String ocrProductName) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Product matchedProduct = fuzzyMatchService.findBestMatchingProduct(ocrProductName);
            
            if (matchedProduct != null) {
                response.put("success", true);
                response.put("matched", true);
                response.put("product", matchedProduct);
                response.put("message", "Found matching product");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", true);
                response.put("matched", false);
                response.put("product", null);
                response.put("message", "No matching product found (similarity < 70%)");
                return ResponseEntity.ok(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * Get all matching products with similarity scores
     * GET /api/fuzzy-match/find-all?productName=Cupcake
     * 
     * Response:
     * {
     *   "success": true,
     *   "matches": [
     *     {
     *       "product": {...},
     *       "similarity": 0.95,
     *       "similarityPercentage": "95.00%"
     *     },
     *     {
     *       "product": {...},
     *       "similarity": 0.75,
     *       "similarityPercentage": "75.00%"
     *     }
     *   ],
     *   "totalMatches": 2
     * }
     */
    @GetMapping("/find-all")
    public ResponseEntity<Map<String, Object>> findAllMatches(
            @RequestParam(value = "productName") String ocrProductName) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<FuzzyMatchService.ProductMatchResult> matches = 
                fuzzyMatchService.findAllMatches(ocrProductName);
            
            response.put("success", true);
            response.put("matches", matches);
            response.put("totalMatches", matches.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
