package com.jartiste.smartshop.domain.entity;

import com.jartiste.smartshop.domain.enums.CustomerTier;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;


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
}
