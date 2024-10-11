package com.spring.example.payload.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RoleRequest {
    @NotNull(message = "Role should not be null.")
    @Pattern(regexp = "^[A-Z]+$", message = "Role name must be uppercase letters only.")
    private String name;
}
