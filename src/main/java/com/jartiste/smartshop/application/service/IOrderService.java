package com.jartiste.smartshop.application.service;

import com.jartiste.smartshop.presentation.dto.request.OrderRequest;
import com.jartiste.smartshop.presentation.dto.response.OrderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IOrderService {
    OrderResponse createOrder(OrderRequest request);
    OrderResponse getOrderById(Long id);
    Page<OrderResponse> getOrderByClient(Long clientId, Pageable pageable);
    OrderResponse validateOrder(Long orderId);
    void cancelOrder(Long orderId);
}
