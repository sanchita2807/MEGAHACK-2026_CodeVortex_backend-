package com.smartinvoice.backend.repository;

import com.smartinvoice.backend.entity.Language;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LanguageRepository extends JpaRepository<Language, Long> {
    List<Language> findByActiveTrue();
}
