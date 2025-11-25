package com.jartiste.smartshop.presentation.controller;

import com.jartiste.smartshop.application.service.OrderService;
import com.jartiste.smartshop.domain.enums.UserRole;
import com.jartiste.smartshop.domain.exception.ForbiddenException;
import com.jartiste.smartshop.presentation.annotation.RequireRole;
import com.jartiste.smartshop.presentation.dto.request.OrderRequest;
import com.jartiste.smartshop.presentation.dto.response.OrderResponse;
import jakarta.servlet.http.HttpSession;
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
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @RequireRole({UserRole.ADMIN, UserRole.CLIENT})
    public ResponseEntity<OrderResponse> createOrder(@RequestBody OrderRequest request, HttpSession session) {
        validateClientAccess(request.ClientId(), session);
        OrderResponse response = this.orderService.createOrder(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @RequireRole({UserRole.ADMIN, UserRole.CLIENT})
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long id, HttpSession session) {
        OrderResponse order = this.orderService.getOrderById(id);

        validateClientAccess(order.clientId(), session);

        return ResponseEntity.ok(order);
    }

    @GetMapping("/client/{clientId}")
    @RequireRole({UserRole.ADMIN, UserRole.CLIENT})
    public ResponseEntity<Page<OrderResponse>> getClientHistory(
            @PathVariable Long clientId,
            int page,
            int size,
            HttpSession session
            ) {
        validateClientAccess(clientId, session);
        Pageable pageable = PageRequest.of(page,size);

        Page<OrderResponse> response = this.orderService.getOrderByClient(clientId, pageable);
        return ResponseEntity.ok(response);
    }


    private void validateClientAccess(Long targetClientId, HttpSession session) {
        String role = (String) session.getAttribute("USER_ROLE");
        Long currentUserId = (Long) session.getAttribute("USER_ID");

        if(UserRole.ADMIN.name().equals(role)) {
            return;
        }

        if(!targetClientId.equals(currentUserId)) {
            throw new ForbiddenException("You don't have Permissions");
        }
    }
}
