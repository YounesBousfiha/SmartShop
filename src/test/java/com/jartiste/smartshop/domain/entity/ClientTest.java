package com.jartiste.smartshop.domain.entity;

import com.jartiste.smartshop.domain.enums.CustomerTier;
import com.jartiste.smartshop.domain.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClientTest {


    private Client client;
    private Product macbook;

    @BeforeEach
    void setup() {
        client  = Client.builder()
                .id(1L)
                .username("tester")
                .totalSpent(BigDecimal.ZERO)
                .totalOrders(0)
                .tier(CustomerTier.BASIC)
                .role(UserRole.CLIENT)
                .build();

        macbook = Product.builder()
                .deleted(false)
                .stock(10)
                .name("MacBook Pro M3")
                .price(BigDecimal.valueOf(35000.00))
                .build();

    }


    @Test
    @DisplayName("Should Update Total Orders, Total Spent and Client Tiers")
    void ShouldUpdateStatsTestAndUpdateClientTier() {
        BigDecimal orderAmount = macbook.getPrice().multiply(BigDecimal.ONE);

        client.updateStats(orderAmount);

        assertEquals(1, client.getTotalOrders());
        assertEquals(0, orderAmount.compareTo(client.getTotalSpent()));
        assertEquals(CustomerTier.PLATINUM, client.getTier());
    }


    @Test
    @DisplayName("Should Calculate the Discount Rate")
    void shouldCalculateDiscountRate() {
        client.setTier(CustomerTier.BASIC);
        BigDecimal rateBasic = client.getDiscountRate(BigDecimal.valueOf(1000));
        assertEquals(BigDecimal.ZERO, rateBasic);

        client.setTier(CustomerTier.SLIVER);
        assertEquals(BigDecimal.ZERO, client.getDiscountRate(BigDecimal.valueOf(400)));
        assertEquals(BigDecimal.valueOf(0.05), client.getDiscountRate(BigDecimal.valueOf(600)));


        client.setTier(CustomerTier.GOLD);
        assertEquals(BigDecimal.valueOf(0.10), client.getDiscountRate(BigDecimal.valueOf(1000)));

        client.setTier(CustomerTier.PLATINUM);
        assertEquals(BigDecimal.valueOf(0.15), client.getDiscountRate(BigDecimal.valueOf(2000)));

    }
}
