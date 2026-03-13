package com.smartinvoice.backend.dto;

import com.smartinvoice.backend.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String shopName;
    private String businessType;
    private Integer userType; // Only visible to admins
    private Boolean passwordSet; // Only visible to admins
    
    public static UserDTO fromUser(User user, boolean isAdmin) {
        return UserDTO.builder()
            .id(user.getId())
            .name(user.getName())
            .email(user.getEmail())
            .phone(user.getPhone())
            .shopName(user.getShopName())
            .businessType(user.getBusinessType())
            .userType(isAdmin ? user.getUserType() : null)
            .passwordSet(isAdmin ? user.isPasswordSet() : null)
            .build();
    }
}
