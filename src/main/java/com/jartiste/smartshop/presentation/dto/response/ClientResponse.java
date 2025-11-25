package com.jartiste.smartshop.presentation.dto.response;

import com.jartiste.smartshop.domain.enums.CustomerTier;
import com.jartiste.smartshop.domain.enums.UserRole;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record ClientResponse(
        Long id,
        String nom,
        String username,
        UserRole role,
        CustomerTier tier,
        int totalOrders,
        BigDecimal totalSpent
) {
}
