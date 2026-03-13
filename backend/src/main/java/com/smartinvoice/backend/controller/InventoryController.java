package com.smartinvoice.backend.controller;

import com.smartinvoice.backend.dto.ProductDTO;
import com.smartinvoice.backend.entity.Product;
import com.smartinvoice.backend.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/inventory")
@CrossOrigin(origins = "*")
public class InventoryController {
    
    @Autowired
    private ProductRepository productRepository;
    
    @GetMapping("/products")
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        List<Product> products = productRepository.findAll();
        
        List<ProductDTO> dtos = products.stream()
            .map(p -> new ProductDTO(p.getId(), p.getName(), p.getStockLevel(), p.getThreshold(), p.getPrice()))
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(dtos);
    }
    
    @PostMapping("/products")
    public ResponseEntity<ProductDTO> addProduct(@RequestBody ProductDTO dto) {
        Product product = Product.builder()
            .name(dto.getName())
            .stockLevel(dto.getStockLevel())
            .threshold(dto.getThreshold())
            .price(dto.getPrice())
            .build();
        
        Product saved = productRepository.save(product);
        
        return ResponseEntity.ok(new ProductDTO(
            saved.getId(),
            saved.getName(),
            saved.getStockLevel(),
            saved.getThreshold(),
            saved.getPrice()
        ));
    }
    
    @PutMapping("/products/{id}")
    public ResponseEntity<ProductDTO> updateProduct(@PathVariable Long id, @RequestBody ProductDTO dto) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Product not found"));
        
        product.setName(dto.getName());
        product.setStockLevel(dto.getStockLevel());
        product.setThreshold(dto.getThreshold());
        product.setPrice(dto.getPrice());
        
        Product updated = productRepository.save(product);
        
        return ResponseEntity.ok(new ProductDTO(
            updated.getId(),
            updated.getName(),
            updated.getStockLevel(),
            updated.getThreshold(),
            updated.getPrice()
        ));
    }
    
    @DeleteMapping("/products/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
