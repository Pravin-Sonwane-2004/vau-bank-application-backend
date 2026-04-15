package com.vaul.vaul.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.validator.constraints.UniqueElements;

import java.awt.*;
import java.time.LocalDateTime;
import java.util.Date;

@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    @Email
//    @UniqueElements
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private Long phone;

    @Lob
    @Column(nullable = true)
    private byte[] image;

    @Column(nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

}