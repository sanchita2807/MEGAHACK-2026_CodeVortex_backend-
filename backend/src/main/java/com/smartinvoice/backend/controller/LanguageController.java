package com.smartinvoice.backend.controller;

import com.smartinvoice.backend.dto.LanguageDTO;
import com.smartinvoice.backend.entity.Language;
import com.smartinvoice.backend.repository.LanguageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/languages")
@CrossOrigin(origins = "*")
public class LanguageController {

    @Autowired
    private LanguageRepository languageRepository;

    @GetMapping
    public ResponseEntity<List<LanguageDTO>> getActiveLanguages() {
        List<LanguageDTO> languages = languageRepository.findByActiveTrue().stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(languages);
    }

    private LanguageDTO toDTO(Language lang) {
        LanguageDTO dto = new LanguageDTO();
        dto.setId(lang.getId());
        dto.setCode(lang.getCode());
        dto.setName(lang.getName());
        dto.setNativeName(lang.getNativeName());
        dto.setActive(lang.isActive());
        return dto;
    }
}
