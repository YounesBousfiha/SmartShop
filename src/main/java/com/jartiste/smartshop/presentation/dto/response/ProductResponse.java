package com.jartiste.smartshop.presentation.dto.response;


import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record ProductResponse(
        Long id,
        String name,
        BigDecimal price,
        Integer stock,
        Boolean deleted) {
}
