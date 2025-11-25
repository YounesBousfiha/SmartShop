package com.jartiste.smartshop.presentation.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank(message = "name is required")
        String name,

        @NotNull(message = "Price is required")
        BigDecimal price,

        @NotNull(message = "Stock is Required")
        @Min(value = 0, message = "Stock can't be negative")
        Integer stock
) {
}
