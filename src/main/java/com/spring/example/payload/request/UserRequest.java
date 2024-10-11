package com.spring.example.payload.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UserRequest {
    @NotEmpty(message = "Name should not be null.")
    @Pattern(regexp = "^[a-zA-Z]*$", message = "Invalid name format, please use only letters.")
    private String name;
}
