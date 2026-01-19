package com.yappifychatapp.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    @Id
    private String id;

    @DBRef
    private User sender;

    private String content;

    @DBRef
    @JsonIgnoreProperties({"latestMessage"}) // Prevent circular reference
    private Chat chat;

    @DBRef
    private List<User> readBy = new ArrayList<>();

    @CreatedDate
    private LocalDateTime createdAt;

    // New fields for edit functionality
    private boolean edited = false;

    private LocalDateTime editedAt;
}