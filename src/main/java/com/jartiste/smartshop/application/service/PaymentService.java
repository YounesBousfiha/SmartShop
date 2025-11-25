package com.jartiste.smartshop.application.service;


import com.jartiste.smartshop.domain.entity.Order;
import com.jartiste.smartshop.domain.entity.Payment;
import com.jartiste.smartshop.domain.enums.OrderStatus;
import com.jartiste.smartshop.domain.enums.PaymentMethod;
import com.jartiste.smartshop.domain.enums.PaymentStatus;
import com.jartiste.smartshop.domain.exception.BusinessLogicViolation;
import com.jartiste.smartshop.domain.exception.ResourceNotFound;
import com.jartiste.smartshop.domain.repository.OrderRepository;
import com.jartiste.smartshop.domain.repository.PaymentRepository;
import com.jartiste.smartshop.presentation.dto.request.PaymentRequest;
import com.jartiste.smartshop.presentation.dto.response.PaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;


    public PaymentResponse addPayment(Long orderId, PaymentRequest request) {
        Order order = this.orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFound("Order Not Found"));

        if(order.getOrderStatus() == OrderStatus.CANCELED || order.getOrderStatus() == OrderStatus.REJECTED) {
            throw new BusinessLogicViolation("Order Already Canceled Or Rejected");
        }

        if(request.amount().compareTo(order.getRemainingAmount()) > 0) {
            throw  new BusinessLogicViolation("the Amount is More that what remain");
        }

        if(request.paymentMethod() == PaymentMethod.ESPECES && request.amount().compareTo(BigDecimal.valueOf(20000)) > 0) {
            throw new BusinessLogicViolation("Cash Payment surpass Limit of 20.000");
        }

        int nextPaymentNumber = order.getPayments().size() + 1;

        Payment payment = Payment.builder()
                .order(order)
                .amount(request.amount())
                .paymentMethod(request.paymentMethod())
                .paymentNumber(nextPaymentNumber)
                .paymentStatus(request.paymentMethod() == PaymentMethod.ESPECES ? PaymentStatus.ENCAISSE : PaymentStatus.EN_ATTENTE)
                .build();

        BigDecimal newRemaining = order.getRemainingAmount().subtract(request.amount());
        order.setRemainingAmount(newRemaining);

        this.paymentRepository.save(payment);
        this.orderRepository.save(order);

        return PaymentResponse.builder()
                .id(payment.getId())
                .paymentNumber(payment.getPaymentNumber())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .status(payment.getPaymentStatus())
                .paymentDate(payment.getClearedDate())
                .build();
    }
}
