package com.smartinvoice.backend.repository;

import com.smartinvoice.backend.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByStockLevelLessThanEqual(Integer stockLevel);
    Optional<Product> findByName(String name);
}
