package com.smartinvoice.backend.config;

import com.smartinvoice.backend.entity.Product;
import com.smartinvoice.backend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final ProductRepository productRepository;

    @Override
    public void run(String... args) throws Exception {
        if (productRepository.count() == 0) {
            productRepository.saveAll(List.of(
                    Product.builder().name("Aashirvaad Atta 5kg").stockLevel(2).threshold(5)
                            .price(new BigDecimal("250.00")).build(),
                    Product.builder().name("Tata Salt 1kg").stockLevel(5).threshold(10).price(new BigDecimal("28.00"))
                            .build(),
                    Product.builder().name("Fortune Oil 1L").stockLevel(15).threshold(5).price(new BigDecimal("160.00"))
                            .build(),
                    Product.builder().name("Maggi Noodles 400g").stockLevel(50).threshold(10)
                            .price(new BigDecimal("96.00")).build()));
        }
    }
}
