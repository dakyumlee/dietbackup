package com.mydiet.dto;
import com.mydiet.model.Role;
import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;
}