package com.jartiste.smartshop.presentation.controller;

import com.jartiste.smartshop.application.service.IOrderService;
import com.jartiste.smartshop.domain.enums.UserRole;
import com.jartiste.smartshop.domain.exception.ForbiddenException;
import com.jartiste.smartshop.presentation.annotation.RequireRole;
import com.jartiste.smartshop.presentation.dto.request.OrderRequest;
import com.jartiste.smartshop.presentation.dto.response.OrderResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order management APIs")
public class OrderController {

    private final IOrderService orderService;

    @Operation(
            summary = "Create a new order",
            description = "Create a new order for a client with products and optional promo code"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Order created successfully",
                    content = @Content(schema = @Schema(implementation = OrderResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input or insufficient stock",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Client or product not found",
                    content = @Content
            )
    })
    @PostMapping
    @RequireRole({UserRole.ADMIN, UserRole.CLIENT})
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderRequest request, HttpSession session) {
        validateClientAccess(request.ClientId(), session);
        OrderResponse response = this.orderService.createOrder(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Get order by ID",
            description = "Retrieve order details by ID (Admin or order owner)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved order",
                    content = @Content(schema = @Schema(implementation = OrderResponse.class))
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
    @GetMapping("/{id}")
    @RequireRole({UserRole.ADMIN, UserRole.CLIENT})
    public ResponseEntity<OrderResponse> getOrder(
            @Parameter(description = "Order ID") @PathVariable Long id,
            HttpSession session
    ) {
        OrderResponse order = this.orderService.getOrderById(id);
        validateClientAccess(order.clientId(), session);
        return ResponseEntity.ok(order);
    }

    @Operation(
            summary = "Get client order history",
            description = "Retrieve paginated order history for a specific client (Admin or own orders)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved orders",
                    content = @Content(schema = @Schema(implementation = Page.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied",
                    content = @Content
            )
    })
    @GetMapping("/client/{clientId}")
    @RequireRole({UserRole.ADMIN, UserRole.CLIENT})
    public ResponseEntity<Page<OrderResponse>> getClientHistory(
            @Parameter(description = "Client ID") @PathVariable Long clientId,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            HttpSession session
    ) {
        validateClientAccess(clientId, session);
        Pageable pageable = PageRequest.of(page, size);
        Page<OrderResponse> response = this.orderService.getOrderByClient(clientId, pageable);
        return ResponseEntity.ok(response);
    }


    private void validateClientAccess(Long targetClientId, HttpSession session) {
        UserRole role = (UserRole) session.getAttribute("USER_ROLE");
        Long currentUserId = (Long) session.getAttribute("USER_ID");

        if(UserRole.ADMIN.equals(role)) {
            return;
        }

        if(!targetClientId.equals(currentUserId)) {
            throw new ForbiddenException("You don't have Permissions");
        }
    }
}
