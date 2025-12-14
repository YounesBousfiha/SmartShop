package com.jartiste.smartshop.application.service;

import com.jartiste.smartshop.presentation.dto.request.PaymentRequest;
import com.jartiste.smartshop.presentation.dto.response.PaymentResponse;

import java.util.List;

public interface IPaymentService {
    PaymentResponse addPayment(Long orderId, PaymentRequest request);
    List<PaymentResponse> getPaymentByOrder(Long orderId);
}
