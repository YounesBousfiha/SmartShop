package com.jartiste.smartshop.domain.entity;

import com.jartiste.smartshop.domain.exception.BusinessLogicViolation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class ProductTest {

    private Product macbook;
    private Product cable;

    @BeforeEach
    void setup() {
        macbook = Product.builder()
                .stock(20)
                .build();

        cable = Product.builder()
                .stock(15)
                .build();
    }


    @Test
    @DisplayName("Should Decrease the Stock")
    void shouldDecreaseStock() {
        macbook.decreaseStock(10);

        assertEquals(10, macbook.getStock());
    }


    @Test
    @DisplayName("Should Increase the Stock")
    void shouldIncreaseStock() {
        cable.decreaseStock(5);

        assertEquals(10, cable.getStock());
    }

    @Test
    @DisplayName("quantity > stock Should Throw Exception")
    void outOfStockTest() {

        assertThrows(BusinessLogicViolation.class, () -> {
            macbook.decreaseStock(30);
        });
    }
}
