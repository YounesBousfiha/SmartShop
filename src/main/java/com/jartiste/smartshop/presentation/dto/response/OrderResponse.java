package com.jartiste.smartshop.presentation.dto.response;


import com.jartiste.smartshop.domain.enums.OrderStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Builder
public record OrderResponse(
        Long id,
        Long clientId,
        String clientName,
        List<OrderItemResponse> itemList,
        BigDecimal subTotal,
        BigDecimal discountAmount,
        BigDecimal taxAmount,
        BigDecimal totalAmount,
        BigDecimal remainingAmount,
        String promoCode,
        OrderStatus status,
        LocalDateTime createdAt) {
}
