package com.jartiste.smartshop.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "username is required")
        String username,

        @NotBlank(message = "Password is required")
        String password
) {
}
