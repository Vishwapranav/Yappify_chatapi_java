// src/main/java/com/yappifychatapp/dto/RegisterRequest.java
package com.yappifychatapp.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String name;
    private String email;
    private String password;
    private String pic; // optional
}
