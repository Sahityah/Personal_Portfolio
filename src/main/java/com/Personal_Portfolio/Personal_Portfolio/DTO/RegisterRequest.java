package com.Personal_Portfolio.Personal_Portfolio.DTO;

import lombok.Data;

@Data
public class RegisterRequest {
    private String name;
    private String email;
    private String password;
}
