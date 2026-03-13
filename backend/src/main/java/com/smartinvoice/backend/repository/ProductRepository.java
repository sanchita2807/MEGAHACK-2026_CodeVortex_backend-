package com.smartinvoice.backend.repository;

import com.smartinvoice.backend.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query("SELECT p FROM Product p WHERE p.stockLeft < p.minStock")
    List<Product> findLowStockProducts();
    
    @Query("SELECT COUNT(p) FROM Product p")
    Long countTotalProducts();
    
    @Query("SELECT SUM(p.stockLeft) FROM Product p")
    Long sumTotalStock();
}
