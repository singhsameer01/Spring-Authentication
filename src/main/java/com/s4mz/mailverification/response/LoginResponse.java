package com.s4mz.mailverification.response;

import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private long expiresIn;
}
