package com.jartiste.smartshop.presentation.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderItemRequest(
        @NotNull(message = "ID is required")
        Long productId,

        @Min(value = 1, message = "Quantity need to be positive")
        int quantity
) {
}
