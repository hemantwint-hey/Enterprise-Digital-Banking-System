package org.example.enterprisedigitalbankingsystem.auth.entity;

import jakarta.persistence.*;
import lombok.*;

import java.nio.file.FileStore;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID userId;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    @Column(nullable = false)
    private boolean kycVerified;

    @Column(nullable = false)
    private boolean emailVerified;

    @Column(nullable = false)
    private boolean phoneVerified;

    private LocalDateTime lastLogin;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;


}