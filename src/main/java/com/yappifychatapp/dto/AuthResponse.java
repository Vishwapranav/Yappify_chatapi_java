// src/main/java/com/yappifychatapp/dto/AuthResponse.java
package com.yappifychatapp.dto;

import com.yappifychatapp.models.User;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private User user;
    private String token;
}
