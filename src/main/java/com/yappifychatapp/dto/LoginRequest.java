// src/main/java/com/yappifychatapp/dto/LoginRequest.java
package com.yappifychatapp.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;
}
