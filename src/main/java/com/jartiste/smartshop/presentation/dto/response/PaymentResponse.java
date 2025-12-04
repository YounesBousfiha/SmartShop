package com.jartiste.smartshop.presentation.dto.response;

import com.jartiste.smartshop.domain.enums.PaymentMethod;
import com.jartiste.smartshop.domain.enums.PaymentStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
public record PaymentResponse(
        Long id,
        String reference,
        BigDecimal amount,
        PaymentMethod paymentMethod,
        PaymentStatus status,
        String bankName,
        LocalDate dueDate,
        LocalDateTime clearedDate,
        LocalDateTime createdAt) {
}
