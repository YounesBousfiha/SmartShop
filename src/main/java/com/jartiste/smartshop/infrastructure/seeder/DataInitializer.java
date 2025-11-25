package com.jartiste.smartshop.infrastructure.seeder;


import com.jartiste.smartshop.domain.entity.Admin;
import com.jartiste.smartshop.domain.entity.Client;
import com.jartiste.smartshop.domain.entity.Product;
import com.jartiste.smartshop.domain.enums.CustomerTier;
import com.jartiste.smartshop.domain.enums.UserRole;
import com.jartiste.smartshop.domain.repository.ClientRepository;
import com.jartiste.smartshop.domain.repository.ProductRepository;
import com.jartiste.smartshop.domain.repository.UserRepository;
import com.jartiste.smartshop.infrastructure.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;


@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final ProductRepository productRepository;
    private final PasswordUtil passwordUtil;


    @Override
    public void run(String... args) throws Exception {
        seedUsers();
        seedClients();
        seedProducts();
        seedCodePromos();
    }


    private void seedUsers() {
        if(this.userRepository.count() > 0) {
            log.info("Users(Admin) Already Seeded...");
            return;
        }

        Admin admin = Admin.builder()
                .username("admin")
                .password(passwordUtil.hashPassword("admin123"))
                .role(UserRole.ADMIN)
                .build();

        this.userRepository.save(admin);
        log.info("Admin User Seeded");
    }

    private void seedClients() {

        if(this.clientRepository.count() > 0) {
            log.info("Client Already Seeded...");
            return;
        }
        // Silver Client
        Client client1 = Client.builder()
                .nom("Nokia Maroc")
                .username("nokia_admin")
                .password(passwordUtil.hashPassword("nokia123"))
                .tier(CustomerTier.SLIVER)
                .role(UserRole.CLIENT)
                .totalOrders(5)
                .totalSpent(BigDecimal.valueOf(2500.00))
                .build();

        // Gold Client
        Client client2 = Client.builder()
                .nom("Samsung Store")
                .username("samsung_user")
                .password(passwordUtil.hashPassword("samsung123"))
                .tier(CustomerTier.GOLD)
                .totalOrders(12)
                .totalSpent(BigDecimal.valueOf(8000.00))
                .build();

        // Platinum Client
        Client client3 = Client.builder()
                .nom("Tech Giant SARL")
                .username("tech_giant")
                .password(passwordUtil.hashPassword("giant123"))
                .tier(CustomerTier.PLATINUM)
                .totalOrders(25)
                .totalSpent(BigDecimal.valueOf(50000.00))
                .build();

        // Basic Client
        Client client4 = Client.builder()
                .nom("Small Shop")
                .username("new_shop")
                .password(passwordUtil.hashPassword("shop123"))
                .tier(CustomerTier.BASIC)
                .totalOrders(0)
                .totalSpent(BigDecimal.ZERO)
                .build();



        this.clientRepository.saveAll(Arrays.asList(
                client1,
                client2,
                client3,
                client4
        ));
        log.info("Client seeded: Silver, Gold, Platinum, Basic");
    }

    private void seedProducts() {

        if(this.productRepository.count() > 0) {
            log.info("Product Already Seeded...");
            return;
        }
        // Expensive Product
        Product product1 = Product.builder()
                .name("MacBook Pro M3 Max")
                .price(BigDecimal.valueOf(35000.00))
                .stock(10)
                .deleted(false)
                .build();

        // Inexpensive Product
        Product product2 = Product.builder()
                .name("Souris Logitech Sans Fil")
                .price(BigDecimal.valueOf(150.00))
                .stock(100)
                .deleted(false)
                .build();

        // Medium price Product
        Product product3 = Product.builder()
                .name("Ecran Dell 27 pouces 4K")
                .price(BigDecimal.valueOf(4500.00))
                .stock(20)
                .deleted(false)
                .build();

        // Stock Zero
        Product product4 = Product.builder()
                .name("Carte Graphique RTX 4090 (Rupture)")
                .price(BigDecimal.valueOf(25000.00))
                .stock(0) // Stock Zero!
                .deleted(false)
                .build();

        // Soft Deleted Product
        Product product5 = Product.builder()
                .name("Cable VGA")
                .price(BigDecimal.valueOf(50.00))
                .stock(5)
                .deleted(true)
                .build();



        this.productRepository.saveAll(Arrays.asList(
                product1,
                product2,
                product3,
                product4,
                product5
                )
        );

        log.info("Product Seeded (Expensive, Inexpensive, Medium , Out of Stock, Deleted)");
    }

    private void seedCodePromos() {
        /* Code Promo */
    }
}
