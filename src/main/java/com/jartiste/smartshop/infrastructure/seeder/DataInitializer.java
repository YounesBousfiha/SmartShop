package com.jartiste.smartshop.infrastructure.seeder;


import com.jartiste.smartshop.domain.repository.ClientRepository;
import com.jartiste.smartshop.domain.repository.ProductRepository;
import com.jartiste.smartshop.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final ProductRepository productRepository;


    @Override
    public void run(String... args) throws Exception {
        /* Call the private methods */
    }


    private void seedUsers() {
        /* FOR SONARQUBE */
    }

    private void seedClients() {
        /* FOR SONARQUBE */
    }

    private void seedProducts() {
        /* FOR SONARQUBE */
    }
}
