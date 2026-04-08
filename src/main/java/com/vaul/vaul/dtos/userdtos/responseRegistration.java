package com.vaul.vaul.dtos.userdtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class responseRegistration {
    private Long id;
    private String name;
    private String email;
}
