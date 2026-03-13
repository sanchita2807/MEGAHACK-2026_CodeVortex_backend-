package com.smartinvoice.backend.repository;

import com.smartinvoice.backend.entity.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface VendorRepository extends JpaRepository<Vendor, Long> {
    List<Vendor> findByStatus(Vendor.VendorStatus status);
    Vendor findByEmail(String email);
}
