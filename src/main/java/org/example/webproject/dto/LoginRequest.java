package org.example.webproject.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;
}
