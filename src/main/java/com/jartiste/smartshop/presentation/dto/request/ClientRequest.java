package com.jartiste.smartshop.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClientRequest(
        @NotBlank(message = "Nom required")
        String nom,

        @NotBlank(message = "username reuquired")
        String username,

        @NotBlank(message = "Password required")
        @Size(min = 6, message = "Password need to at least 6 chracters")
        String password
) {
}
