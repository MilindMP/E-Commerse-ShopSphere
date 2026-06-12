package com.shopsphere.user.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String userId;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    private String firstname;
    private String lastname;
    private String phone;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(nullable = false)
    private Long createdAt = System.currentTimeMillis();

    private Long updatedAt;
}
