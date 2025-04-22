package com.s4mz.mailverification.dto;

import lombok.Data;

@Data
public class VerifyUserDto {
    private String userName;
    private String verificationCode;
}
