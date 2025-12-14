package com.jartiste.smartshop.application.service.impl;


import com.jartiste.smartshop.application.service.IPaymentService;
// import com.jartiste.smartshop.domain.entity.Client;
import com.jartiste.smartshop.domain.entity.Order;
import com.jartiste.smartshop.domain.entity.Payment;
import com.jartiste.smartshop.domain.enums.OrderStatus;
import com.jartiste.smartshop.domain.enums.PaymentMethod;
import com.jartiste.smartshop.domain.enums.PaymentStatus;
import com.jartiste.smartshop.domain.exception.BusinessLogicViolation;
import com.jartiste.smartshop.domain.exception.ResourceNotFound;
// import com.jartiste.smartshop.domain.repository.ClientRepository;
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
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements IPaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    // private final ClientRepository clientRepository;
    private static final String LOCAL_DATETIME_PATTERN = "yyyyMMddHHmmss";

    @Transactional
    public PaymentResponse addPayment(Long orderId, PaymentRequest request) {
        Order order = this.orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFound("Order Not Found"));

        if(order.getOrderStatus() == OrderStatus.CANCELED || order.getOrderStatus() == OrderStatus.REJECTED) {
            throw new BusinessLogicViolation("Order Already Canceled Or Rejected");
        }

        if(request.amount().compareTo(order.getRemainingAmount()) > 0) {
            throw new BusinessLogicViolation("The amount is more than what remains");
        }

        validatePaymentMethodRules(request);

        String reference = generateReference(request);

        PaymentStatus initialStatus = determineInitialStatus(request.paymentMethod());

        LocalDateTime clearedDate = (request.paymentMethod() == PaymentMethod.ESPECES)
                ? LocalDateTime.now()
                : null;

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

        BigDecimal newRemaining = order.getRemainingAmount().subtract(request.amount());
        order.setRemainingAmount(newRemaining);

        Payment savedPayment = this.paymentRepository.save(payment);

        /* if (newRemaining.compareTo(BigDecimal.ZERO) == 0 && order.getOrderStatus() == OrderStatus.PENDING) {
            order.setOrderStatus(OrderStatus.CONFIRMED);

            Client client = order.getClient();
            client.updateStats(order.getTotalAmount());
            this.clientRepository.save(client);
        }*/

        this.orderRepository.save(order);

        return buildPaymentResponse(savedPayment);
    }

    public List<PaymentResponse> getPaymentByOrder(Long orderId) {
        return paymentRepository.findAll().stream()
                .filter(p -> p.getOrder().getId().equals(orderId))
                .map(this::buildPaymentResponse)
                .toList();
    }

    private void validatePaymentMethodRules(PaymentRequest request) {
        switch (request.paymentMethod()) {
            case ESPECES -> {
                if (request.amount().compareTo(BigDecimal.valueOf(20000)) > 0) {
                    throw new BusinessLogicViolation("Cash payment surpasses limit of 20,000 DH");
                }
            }
            case CHEQUE -> {
                if (request.bankName() == null || request.bankName().isBlank()) {
                    throw new BusinessLogicViolation("Check payment requires bank name");
                }
                if (request.dueDate() == null) {
                    throw new BusinessLogicViolation("Check payment requires due date");
                }
            }
            case VIREMENT -> {
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
        switch (request.paymentMethod()) {

            case PaymentMethod.ESPECES -> {
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern(LOCAL_DATETIME_PATTERN));
                return "ESP-" + timestamp;
            }

            case PaymentMethod.VIREMENT -> {
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern(LOCAL_DATETIME_PATTERN));
                return "VIR-" + timestamp;
            }

            case PaymentMethod.CHEQUE -> {
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern(LOCAL_DATETIME_PATTERN));
                return "CHQ-" + timestamp;
            }
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
// list de payment de type VIREMENT avec STATUS ENCAISSE
// sort part order decroissant par date de creation

  /*  public List<PaymentResponse> miseEnsituation() {
        List<Payment> paymentList = this.paymentRepository.findAll();

        return paymentList.stream()
                .filter(p -> p.getPaymentMethod() == PaymentMethod.VIREMENT && p.getPaymentStatus() == PaymentStatus.ENCAISSE)
                .sorted(Comparator.comparing(p -> p.getCreatedAt().toLocalDate()).reversed())
                .map(PaymentMapper::toResponse)
                .toList();


    }*/
}
