package com.jartiste.smartshop.infrastructure.seeder;


import com.jartiste.smartshop.domain.entity.Admin;
import com.jartiste.smartshop.domain.entity.Client;
import com.jartiste.smartshop.domain.entity.Product;
import com.jartiste.smartshop.domain.enums.UserRole;
import com.jartiste.smartshop.domain.repository.ClientRepository;
import com.jartiste.smartshop.domain.repository.ProductRepository;
import com.jartiste.smartshop.domain.repository.UserRepository;
import com.jartiste.smartshop.infrastructure.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

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
            log.info("Users in Database Already Seeded..");
            return;
        }

        Admin admin = Admin.builder()
                .username("admin")
                .password(passwordUtil.hashPassword("admin123"))
                .role(UserRole.ADMIN)
                .build();

        this.userRepository.save(admin);
    }

    private void seedClients() {
        Client client1 = Client.builder().build();
        Client client2 = Client.builder().build();
        Client client3 = Client.builder().build();
        Client client4 = Client.builder().build();



        this.clientRepository.saveAll(Arrays.asList(
                client1,
                client2,
                client3,
                client4
        ));
    }

    private void seedProducts() {
        Product product1 = Product.builder().build();
        Product product2 = Product.builder().build();
        Product product3 = Product.builder().build();
        Product product4 = Product.builder().build();


        this.productRepository.saveAll(Arrays.asList(
                product1,
                product2,
                product3,
                product4
                )
        );
    }

    private void seedCodePromos() {
        /* Code Promo */
    }
}
