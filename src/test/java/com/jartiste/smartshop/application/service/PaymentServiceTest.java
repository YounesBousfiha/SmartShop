package com.jartiste.smartshop.application.service;


import com.jartiste.smartshop.domain.entity.Order;
import com.jartiste.smartshop.domain.entity.Payment;
import com.jartiste.smartshop.domain.enums.OrderStatus;
import com.jartiste.smartshop.domain.enums.PaymentMethod;
import com.jartiste.smartshop.domain.enums.PaymentStatus;
import com.jartiste.smartshop.domain.exception.BusinessLogicViolation;
import com.jartiste.smartshop.domain.repository.OrderRepository;
import com.jartiste.smartshop.domain.repository.PaymentRepository;
import com.jartiste.smartshop.presentation.dto.request.PaymentRequest;
import com.jartiste.smartshop.presentation.dto.response.PaymentResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    @DisplayName("Should add valid partial payment and reduce remaining amount")
    void shouldAddValidPayment() {
        Order order = Order.builder()
                .id(1L)
                .orderStatus(OrderStatus.PENDING)
                .remainingAmount(BigDecimal.valueOf(10000))
                .payments(new ArrayList<>())
                .build();

        PaymentRequest request = new PaymentRequest(BigDecimal.valueOf(4000), PaymentMethod.CHEQUE);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.save(any(Payment.class))).thenAnswer( i -> i.getArguments()[0]);

        PaymentResponse response = paymentService.addPayment(1L, request);

        assertEquals(1, response.paymentNumber());
        assertEquals(PaymentStatus.EN_ATTENTE, response.status());
        assertEquals(0, BigDecimal.valueOf(6000).compareTo(order.getRemainingAmount()));
    }

    @Test
    @DisplayName("Should throw exception if payment exceeds 20,000 in Cash")
    void shouldFailForLargeCashPayment() {
        Order order = Order.builder()
                .id(1L)
                .orderStatus(OrderStatus.PENDING)
                .remainingAmount(BigDecimal.valueOf(60000))
                .build();

        PaymentRequest request = new PaymentRequest(BigDecimal.valueOf(21000), PaymentMethod.ESPECES);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(BusinessLogicViolation.class, () -> {
            paymentService.addPayment(order.getId(), request);
        });
    }

    @Test
    @DisplayName("Should throw exception if paying more than remaining amount")
    void shouldFailIfAmountExceedRemaining() {
        Order order = Order.builder()
                .id(1L)
                .orderStatus(OrderStatus.PENDING)
                .remainingAmount(BigDecimal.valueOf(100))
                .build();

        PaymentRequest request = new PaymentRequest(BigDecimal.valueOf(200), PaymentMethod.ESPECES);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(BusinessLogicViolation.class, () -> {
            paymentService.addPayment(order.getId(), request);
        });
    }
}
