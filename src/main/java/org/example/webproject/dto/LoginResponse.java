package org.example.webproject.dto;

import lombok.Data;

@Data
public class LoginResponse {
    private boolean success;
    private String message;
    private String username;

    public static LoginResponse success(String username) {
        LoginResponse response = new LoginResponse();
        response.setSuccess(true);
        response.setUsername(username);
        response.setMessage("登录成功");
        return response;
    }

    public static LoginResponse failure(String message) {
        LoginResponse response = new LoginResponse();
        response.setSuccess(false);
        response.setMessage(message);
        return response;
    }
}
