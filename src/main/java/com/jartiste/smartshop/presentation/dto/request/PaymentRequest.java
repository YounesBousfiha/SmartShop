package com.jartiste.smartshop.presentation.dto.request;

import com.jartiste.smartshop.domain.enums.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PaymentRequest(
        @NotNull(message = "Amount is Required")
        @DecimalMin(value = "0.1", message = "Amount need to be Positive")
        BigDecimal amount,

        @NotNull(message = "One payment method is required")
        PaymentMethod paymentMethod,

        String reference,

        String bankName,

        LocalDate dueDate
) {
}
