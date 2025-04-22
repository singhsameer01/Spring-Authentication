package com.s4mz.mailverification.dto;

import lombok.Data;
import lombok.Getter;

@Data
public class RegisterUserDto {
    private String userName;
    private String email;
    private String password;
}
