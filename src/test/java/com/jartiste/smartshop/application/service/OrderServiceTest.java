package com.jartiste.smartshop.application.service;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {



    @Test
    void shouldCreateNewOrder() {}

    @Test
    void shouldThrowExceptionIfClientNonExist() {}

    @Test
    void shouldThrowExceptionIfProductNonExist() {}

    @Test
    void shouldGetOrderById() {}

    @Test
    void shouldThrowExceptionIfOrderNonExist() {}

    @Test
    void shouldGetOrderByClientId() {}

    @Test
    void shouldValidateOrder() {}

    @Test
    void shouldThrowExceptionIfValidatingOrderNontExist() {}

    @Test
    void shouldThrowExceptionifOrderStatusNotPending() {}

    @Test
    void shouldThrowExceptionifOrderNotPaidFully() {}

    @Test
    void shouldCancelOrder() {}

    @Test
    void shouldThrowExceptionIfCancelingOrderNonExist() {}

    @Test
    void shouldThrowsExceptionIfCanceledStatusOrderNotPending() {}
}
