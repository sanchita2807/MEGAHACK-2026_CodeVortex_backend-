package com.smartinvoice.backend.dto;

public class LanguageDTO {
    private Long id;
    private String code;
    private String name;
    private String nativeName;
    private boolean active;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getNativeName() { return nativeName; }
    public void setNativeName(String nativeName) { this.nativeName = nativeName; }
    
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
