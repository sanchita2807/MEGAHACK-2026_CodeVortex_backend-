package com.smartinvoice.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String shopName;

    @Column(nullable = false)
    private String businessType;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private Integer userType = 0; // 0 = regular user, 1 = admin

    private boolean passwordSet = false;
}
