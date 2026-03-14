package com.smartinvoice.backend.repository;

import com.smartinvoice.backend.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    List<Invoice> findByVendor(String vendor);
    List<Invoice> findByScanDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    List<Invoice> findByStatus(String status);
    List<Invoice> findTop10ByOrderByScanDateDesc();
}
