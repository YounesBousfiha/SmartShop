package com.jartiste.smartshop.presentation.dto.request;

import com.jartiste.smartshop.domain.entity.OrderItem;

import java.util.List;

public record OrderRequest(
        Long ClientId,
        String promoCode,

        List<OrderItemRequest> items
        ) {
}
