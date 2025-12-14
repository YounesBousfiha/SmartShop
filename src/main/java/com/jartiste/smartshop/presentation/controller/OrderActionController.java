package com.jartiste.smartshop.presentation.controller;


import com.jartiste.smartshop.application.service.IOrderService;
import com.jartiste.smartshop.application.service.IPaymentService;
import com.jartiste.smartshop.domain.enums.UserRole;
import com.jartiste.smartshop.presentation.annotation.RequireRole;
import com.jartiste.smartshop.presentation.dto.request.PaymentRequest;
import com.jartiste.smartshop.presentation.dto.response.OrderResponse;
import com.jartiste.smartshop.presentation.dto.response.PaymentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Order Actions", description = "Order action APIs - Payment and status management (Admin only)")
public class OrderActionController {

    private final IPaymentService paymentService;
    private final IOrderService orderService;

    @Operation(
            summary = "Add payment to order",
            description = "Add a payment transaction to an order (Admin only). Supports cash, check, and wire transfer."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Payment added successfully",
                    content = @Content(schema = @Schema(implementation = PaymentResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid payment data or amount exceeds remaining balance",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Order not found",
                    content = @Content
            )
    })
    @PostMapping("/{id}/payments")
    @RequireRole(UserRole.ADMIN)
    public ResponseEntity<PaymentResponse> addPayment(
            @Parameter(description = "Order ID") @PathVariable Long id,
            @Valid @RequestBody PaymentRequest request
    ) {
        PaymentResponse response = this.paymentService.addPayment(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Confirm order",
            description = "Confirm a pending order (Admin only). Order must be fully paid."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Order confirmed successfully",
                    content = @Content(schema = @Schema(implementation = OrderResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Order not pending or not fully paid",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Order not found",
                    content = @Content
            )
    })
    @PatchMapping("/{id}/confirm")
    @RequireRole(UserRole.ADMIN)
    public ResponseEntity<OrderResponse> confirmOrder(
            @Parameter(description = "Order ID") @PathVariable Long id
    ) {
        OrderResponse response = this.orderService.validateOrder(id);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Cancel order",
            description = "Cancel a pending order (Admin only). Restores product stock."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Order cancelled successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Only pending orders can be cancelled",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Order not found",
                    content = @Content
            )
    })
    @PatchMapping("/{id}/cancel")
    @RequireRole(UserRole.ADMIN)
    public ResponseEntity<Void> cancelOrder(
            @Parameter(description = "Order ID") @PathVariable Long id
    ) {
        this.orderService.cancelOrder(id);
        return ResponseEntity.noContent().build();
    }
}
