package com.jartiste.smartshop.application.service;


import com.jartiste.smartshop.domain.entity.Client;
import com.jartiste.smartshop.domain.entity.Order;
import com.jartiste.smartshop.domain.entity.Payment;
import com.jartiste.smartshop.domain.enums.OrderStatus;
import com.jartiste.smartshop.domain.enums.PaymentMethod;
import com.jartiste.smartshop.domain.enums.PaymentStatus;
import com.jartiste.smartshop.domain.exception.BusinessLogicViolation;
import com.jartiste.smartshop.domain.repository.ClientRepository;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ClientRepository clientRepository;

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

        PaymentRequest request = new PaymentRequest(
                BigDecimal.valueOf(4000), 
                PaymentMethod.CHEQUE,
                "CHQ-123456",
                "Bank Al-Maghrib",
                LocalDate.now().plusDays(30)
        );

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> {
            Payment p = (Payment) i.getArguments()[0];
            p.setId(1L);
            return p;
        });

        PaymentResponse response = paymentService.addPayment(1L, request);

        assertEquals("CHQ-123456", response.reference());
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

        PaymentRequest request = new PaymentRequest(
                BigDecimal.valueOf(21000), 
                PaymentMethod.ESPECES,
                null,
                null,
                null
        );

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

        PaymentRequest request = new PaymentRequest(
                BigDecimal.valueOf(200), 
                PaymentMethod.ESPECES,
                null,
                null,
                null
        );

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(BusinessLogicViolation.class, () -> {
            paymentService.addPayment(order.getId(), request);
        });
    }

    @Test
    @DisplayName("Should auto-confirm order when fully paid")
    void shouldAutoConfirmOrderWhenFullyPaid() {
        Client client = Client.builder()
                .id(1L)
                .totalOrders(0)
                .totalSpent(BigDecimal.ZERO)
                .build();

        Order order = Order.builder()
                .id(1L)
                .orderStatus(OrderStatus.PENDING)
                .totalAmount(BigDecimal.valueOf(5000))
                .remainingAmount(BigDecimal.valueOf(5000))
                .client(client)
                .payments(new ArrayList<>())
                .build();

        PaymentRequest request = new PaymentRequest(
                BigDecimal.valueOf(5000), 
                PaymentMethod.ESPECES,
                null,
                null,
                null
        );

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> {
            Payment p = (Payment) i.getArguments()[0];
            p.setId(1L);
            return p;
        });
        when(clientRepository.save(any(Client.class))).thenAnswer(i -> i.getArguments()[0]);

        PaymentResponse response = paymentService.addPayment(1L, request);

        assertEquals(OrderStatus.CONFIRMED, order.getOrderStatus());
        assertEquals(0, order.getRemainingAmount().compareTo(BigDecimal.ZERO));
        assertNotNull(response.reference());
        assertTrue(response.reference().startsWith("ESP-"));
    }

    @Test
    @DisplayName("Should throw exception if CHEQUE payment missing required fields")
    void shouldFailIfChequePaymentMissingFields() {
        Order order = Order.builder()
                .id(1L)
                .orderStatus(OrderStatus.PENDING)
                .remainingAmount(BigDecimal.valueOf(10000))
                .build();

        PaymentRequest request = new PaymentRequest(
                BigDecimal.valueOf(5000), 
                PaymentMethod.CHEQUE,
                null,  // missing reference
                null,  // missing bankName
                null   // missing dueDate
        );

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(BusinessLogicViolation.class, () -> {
            paymentService.addPayment(order.getId(), request);
        });
    }

    @Test
    @DisplayName("Should throw exception if VIREMENT payment missing required fields")
    void shouldFailIfVirementPaymentMissingFields() {
        Order order = Order.builder()
                .id(1L)
                .orderStatus(OrderStatus.PENDING)
                .remainingAmount(BigDecimal.valueOf(10000))
                .build();

        PaymentRequest request = new PaymentRequest(
                BigDecimal.valueOf(5000), 
                PaymentMethod.VIREMENT,
                null,  // missing reference
                null,  // missing bankName
                null
        );

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(BusinessLogicViolation.class, () -> {
            paymentService.addPayment(order.getId(), request);
        });
    }
}
