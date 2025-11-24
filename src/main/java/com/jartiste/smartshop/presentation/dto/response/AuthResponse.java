package com.jartiste.smartshop.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.jartiste.smartshop.domain.enums.CustomerTier;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuthResponse(
        Long id,
        String username,
        String role,
        CustomerTier tier,
        String message
) {
}
