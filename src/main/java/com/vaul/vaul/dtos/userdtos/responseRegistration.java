package com.vaul.vaul.dtos.userdtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class responseRegistration {
    private Long id;
    private String name;
    private String email;
    private Long phone;
    private byte[] image;

}
