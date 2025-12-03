package com.jartiste.smartshop.domain.entity;

import com.jartiste.smartshop.domain.enums.CustomerTier;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;


@Entity
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@DiscriminatorValue("CLIENT")
public class Client extends User {
    private String nom;
    private CustomerTier tier;
    private Integer totalOrders;
    private BigDecimal totalSpent;
    private LocalDate firstOrderDate;
    private LocalDate lastOrderDate;


    public void updateStats(BigDecimal orderAmount) {
        this.totalOrders++;
        this.totalSpent = this.totalSpent.add(orderAmount);
        recalculateTier();
    }

    private void recalculateTier() {
        double spent = this.totalSpent.doubleValue();
        if(this.totalOrders >= 20 || spent >= 15000) this.tier = CustomerTier.PLATINUM;
        else if (this.totalOrders >= 10 || spent >= 5000) this.tier = CustomerTier.GOLD;
        else if (this.totalOrders >= 3 || spent >= 1000) this.tier = CustomerTier.SLIVER;
    }

    public BigDecimal getDiscountRate(BigDecimal subTotal) {
        double amount = subTotal.doubleValue();

        if(this.tier == CustomerTier.SLIVER && amount >= 500) return BigDecimal.valueOf(0.05);
        if (this.tier == CustomerTier.GOLD && amount >= 800) return BigDecimal.valueOf(0.10);
        if (this.tier == CustomerTier.PLATINUM && amount >= 1200) return BigDecimal.valueOf(0.15);

        return BigDecimal.ZERO;
    }
}
