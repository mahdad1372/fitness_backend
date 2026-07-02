package com.example.fitness.dto;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
//@NoArgsConstructor
//@AllArgsConstructor
public class MessageDTO {
    private String user;
    private String message;

    // Default constructor (required for JSON deserialization)
    public MessageDTO() {}

    public MessageDTO(String user, String message) {
        this.user = user;
        this.message = message;
    }

    // Getters and Setters
    public String getUser() { return user; }
    public void setUser(String user) { this.user = user; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}