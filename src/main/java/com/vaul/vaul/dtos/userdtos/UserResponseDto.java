package com.vaul.vaul.dtos.userdtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class UserResponseDto {
    private Long id;
    private String name;
    private String email;
    private Long phone;
    private byte[] image;
    private String message;

}
