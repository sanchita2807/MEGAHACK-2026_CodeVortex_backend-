package com.smartinvoice.backend.service;

import com.smartinvoice.backend.entity.Product;
import com.smartinvoice.backend.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class FuzzyMatchService {
    
    @Autowired
    private ProductRepository productRepository;
    
    private static final double SIMILARITY_THRESHOLD = 0.70; // 70% match
    
    /**
     * Find the best matching product from database using fuzzy matching
     * @param ocrProductName - Product name extracted from OCR
     * @return Matched Product if similarity >= 70%, otherwise null
     */
    public Product findBestMatchingProduct(String ocrProductName) {
        if (ocrProductName == null || ocrProductName.trim().isEmpty()) {
            return null;
        }
        
        List<Product> allProducts = productRepository.findAll();
        Product bestMatch = null;
        double bestSimilarity = 0;
        
        for (Product product : allProducts) {
            double similarity = calculateSimilarity(ocrProductName, product.getName());
            
            if (similarity > bestSimilarity) {
                bestSimilarity = similarity;
                bestMatch = product;
            }
        }
        
        // Return match only if similarity is above threshold
        if (bestSimilarity >= SIMILARITY_THRESHOLD) {
            System.out.println("Fuzzy Match Found: '" + ocrProductName + "' -> '" + bestMatch.getName() + 
                             "' (Similarity: " + String.format("%.2f%%", bestSimilarity * 100) + ")");
            return bestMatch;
        }
        
        System.out.println("No fuzzy match found for: '" + ocrProductName + 
                         "' (Best match: " + String.format("%.2f%%", bestSimilarity * 100) + ")");
        return null;
    }
    
    /**
     * Get all matching products with similarity scores
     * @param ocrProductName - Product name extracted from OCR
     * @return List of products with similarity scores, sorted by similarity descending
     */
    public List<ProductMatchResult> findAllMatches(String ocrProductName) {
        if (ocrProductName == null || ocrProductName.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Product> allProducts = productRepository.findAll();
        List<ProductMatchResult> results = new ArrayList<>();
        
        for (Product product : allProducts) {
            double similarity = calculateSimilarity(ocrProductName, product.getName());
            if (similarity >= SIMILARITY_THRESHOLD) {
                results.add(new ProductMatchResult(product, similarity));
            }
        }
        
        // Sort by similarity descending
        results.sort((a, b) -> Double.compare(b.getSimilarity(), a.getSimilarity()));
        
        return results;
    }
    
    /**
     * Calculate similarity between two strings using Levenshtein distance
     * Returns value between 0 and 1 (0 = no match, 1 = exact match)
     */
    private double calculateSimilarity(String str1, String str2) {
        // Normalize strings: lowercase and trim
        str1 = str1.toLowerCase().trim();
        str2 = str2.toLowerCase().trim();
        
        // Exact match
        if (str1.equals(str2)) {
            return 1.0;
        }
        
        // Calculate Levenshtein distance
        int distance = levenshteinDistance(str1, str2);
        
        // Convert distance to similarity (0-1)
        int maxLength = Math.max(str1.length(), str2.length());
        if (maxLength == 0) {
            return 1.0;
        }
        
        return 1.0 - ((double) distance / maxLength);
    }
    
    /**
     * Calculate Levenshtein distance between two strings
     * Lower distance = more similar
     */
    private int levenshteinDistance(String str1, String str2) {
        int[][] dp = new int[str1.length() + 1][str2.length() + 1];
        
        // Initialize first row and column
        for (int i = 0; i <= str1.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= str2.length(); j++) {
            dp[0][j] = j;
        }
        
        // Fill the matrix
        for (int i = 1; i <= str1.length(); i++) {
            for (int j = 1; j <= str2.length(); j++) {
                if (str1.charAt(i - 1) == str2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(
                        Math.min(dp[i - 1][j], dp[i][j - 1]),    // deletion or insertion
                        dp[i - 1][j - 1]                          // substitution
                    );
                }
            }
        }
        
        return dp[str1.length()][str2.length()];
    }
    
    /**
     * Result class for fuzzy matching
     */
    public static class ProductMatchResult {
        private Product product;
        private double similarity;
        
        public ProductMatchResult(Product product, double similarity) {
            this.product = product;
            this.similarity = similarity;
        }
        
        public Product getProduct() {
            return product;
        }
        
        public double getSimilarity() {
            return similarity;
        }
        
        public String getSimilarityPercentage() {
            return String.format("%.2f%%", similarity * 100);
        }
    }
}
