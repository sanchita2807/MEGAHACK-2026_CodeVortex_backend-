package com.smartinvoice.backend.repository;

import com.smartinvoice.backend.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    List<Invoice> findTop10ByOrderByScanDateDesc();
}
