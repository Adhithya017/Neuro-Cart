package com.neurocart.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

public class AuthDTO {

    @Data
    public static class RegisterRequest {
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50)
        private String username;

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;

        @NotBlank(message = "Password is required")
        @Size(min = 6, max = 100, message = "Password must be 6-100 characters")
        private String password;

        @NotBlank(message = "Full name is required")
        private String fullName;

        private String phone;
        private String role; // CUSTOMER, VENDOR
    }

    @Data
    public static class LoginRequest {
        @NotBlank(message = "Email is required")
        @Email
        private String email;

        @NotBlank(message = "Password is required")
        private String password;
    }

    @Data
    public static class AuthResponse {
        private String token;
        private String type = "Bearer";
        private Long userId;
        private String username;
        private String email;
        private String fullName;
        private String role;
        private String avatarUrl;

        public AuthResponse(String token, Long userId, String username, String email, String fullName, String role,
                String avatarUrl) {
            this.token = token;
            this.userId = userId;
            this.username = username;
            this.email = email;
            this.fullName = fullName;
            this.role = role;
            this.avatarUrl = avatarUrl;
        }
    }
}
