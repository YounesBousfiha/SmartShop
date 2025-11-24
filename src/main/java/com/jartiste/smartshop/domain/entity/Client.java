package com.jartiste.smartshop.domain.entity;

import com.jartiste.smartshop.domain.enums.CustomerTier;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;


@Entity
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Client extends User {
    private CustomerTier tier;
}
