package com.jartiste.smartshop.presentation.controller;


import com.jartiste.smartshop.application.service.OrderService;
import com.jartiste.smartshop.application.service.PaymentService;
import com.jartiste.smartshop.domain.enums.UserRole;
import com.jartiste.smartshop.presentation.annotation.RequireRole;
import com.jartiste.smartshop.presentation.dto.request.PaymentRequest;
import com.jartiste.smartshop.presentation.dto.response.OrderResponse;
import com.jartiste.smartshop.presentation.dto.response.PaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderActionController {

    private final PaymentService paymentService;
    private final OrderService orderService;


    @PostMapping("/{id}/payments")
    @RequireRole(UserRole.ADMIN)
    public ResponseEntity<PaymentResponse> addPayment(
            @PathVariable Long id,
            @RequestBody PaymentRequest request
            ) {
        PaymentResponse response = this.paymentService.addPayment(id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/confirm")
    @RequireRole(UserRole.ADMIN)
    public ResponseEntity<OrderResponse> confirmOrder(@PathVariable Long id) {
        OrderResponse response = this.orderService.validateOrder(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/cancel")
    @RequireRole(UserRole.ADMIN)
    public ResponseEntity<Void> cancelOrder(@PathVariable Long id) {
        this.orderService.cancelOrder(id);
        return ResponseEntity.noContent().build();
    }
}
