package com.vaul.vaul.dtos.customerdtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomerResponseDto {
    private Long id;
    private String name;
    private String email;
    private Long phone;
    private byte[] image;
    private LocalDateTime createdAt;
    private String message;
}
