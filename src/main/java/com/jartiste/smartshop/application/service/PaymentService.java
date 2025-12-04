package com.jartiste.smartshop.application.service;


import com.jartiste.smartshop.domain.entity.Client;
import com.jartiste.smartshop.domain.entity.Order;
import com.jartiste.smartshop.domain.entity.Payment;
import com.jartiste.smartshop.domain.enums.OrderStatus;
import com.jartiste.smartshop.domain.enums.PaymentMethod;
import com.jartiste.smartshop.domain.enums.PaymentStatus;
import com.jartiste.smartshop.domain.exception.BusinessLogicViolation;
import com.jartiste.smartshop.domain.exception.ResourceNotFound;
import com.jartiste.smartshop.domain.repository.ClientRepository;
import com.jartiste.smartshop.domain.repository.OrderRepository;
import com.jartiste.smartshop.domain.repository.PaymentRepository;
import com.jartiste.smartshop.presentation.dto.request.PaymentRequest;
import com.jartiste.smartshop.presentation.dto.response.PaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final ClientRepository clientRepository;

    @Transactional
    public PaymentResponse addPayment(Long orderId, PaymentRequest request) {
        Order order = this.orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFound("Order Not Found"));

        // Validate order status
        if(order.getOrderStatus() == OrderStatus.CANCELED || order.getOrderStatus() == OrderStatus.REJECTED) {
            throw new BusinessLogicViolation("Order Already Canceled Or Rejected");
        }

        // Validate payment amount doesn't exceed remaining amount
        if(request.amount().compareTo(order.getRemainingAmount()) > 0) {
            throw new BusinessLogicViolation("The amount is more than what remains");
        }

        // Validate payment method specific rules
        validatePaymentMethodRules(request);

        // Generate reference if not provided
        String reference = generateReference(request);

        // Determine initial payment status based on payment method
        PaymentStatus initialStatus = determineInitialStatus(request.paymentMethod());

        // Set clearedDate for ESPECES (cash) payments
        LocalDateTime clearedDate = (request.paymentMethod() == PaymentMethod.ESPECES)
                ? LocalDateTime.now()
                : null;

        // Create payment
        Payment payment = Payment.builder()
                .order(order)
                .amount(request.amount())
                .paymentMethod(request.paymentMethod())
                .reference(reference)
                .bankName(request.bankName())
                .dueDate(request.dueDate())
                .paymentStatus(initialStatus)
                .clearedDate(clearedDate)
                .build();

        // Update order remaining amount
        BigDecimal newRemaining = order.getRemainingAmount().subtract(request.amount());
        order.setRemainingAmount(newRemaining);

        // Save payment
        Payment savedPayment = this.paymentRepository.save(payment);

        // Check if order is fully paid and auto-confirm
        if (newRemaining.compareTo(BigDecimal.ZERO) == 0 && order.getOrderStatus() == OrderStatus.PENDING) {
            order.setOrderStatus(OrderStatus.CONFIRMED);

            // Update client stats
            Client client = order.getClient();
            client.updateStats(order.getTotalAmount());
            this.clientRepository.save(client);
        }

        // Save order with updated remaining amount and possibly new status
        this.orderRepository.save(order);

        return buildPaymentResponse(savedPayment);
    }

    private void validatePaymentMethodRules(PaymentRequest request) {
        switch (request.paymentMethod()) {
            case ESPECES -> {
                if (request.amount().compareTo(BigDecimal.valueOf(20000)) > 0) {
                    throw new BusinessLogicViolation("Cash payment surpasses limit of 20,000 DH");
                }
            }
            case CHEQUE -> {
                if (request.reference() == null || request.reference().isBlank()) {
                    throw new BusinessLogicViolation("Check payment requires a reference");
                }
                if (request.bankName() == null || request.bankName().isBlank()) {
                    throw new BusinessLogicViolation("Check payment requires bank name");
                }
                if (request.dueDate() == null) {
                    throw new BusinessLogicViolation("Check payment requires due date");
                }
            }
            case VIREMENT -> {
                if (request.reference() == null || request.reference().isBlank()) {
                    throw new BusinessLogicViolation("Transfer payment requires a reference");
                }
                if (request.bankName() == null || request.bankName().isBlank()) {
                    throw new BusinessLogicViolation("Transfer payment requires bank name");
                }
            }
        }
    }

    private String generateReference(PaymentRequest request) {
        if (request.reference() != null && !request.reference().isBlank()) {
            return request.reference();
        }

        if (request.paymentMethod() == PaymentMethod.ESPECES) {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            return "ESP-" + timestamp;
        }

        return null;
    }

    private PaymentStatus determineInitialStatus(PaymentMethod paymentMethod) {
        return switch (paymentMethod) {
            case ESPECES -> PaymentStatus.ENCAISSE;
            case CHEQUE, VIREMENT -> PaymentStatus.EN_ATTENTE;
        };
    }

    private PaymentResponse buildPaymentResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .reference(payment.getReference())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .status(payment.getPaymentStatus())
                .bankName(payment.getBankName())
                .dueDate(payment.getDueDate())
                .clearedDate(payment.getClearedDate())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}
