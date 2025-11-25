package com.jartiste.smartshop.application.service;


import com.jartiste.smartshop.application.mapper.OrderMapper;
import com.jartiste.smartshop.domain.entity.Client;
import com.jartiste.smartshop.domain.entity.Order;
import com.jartiste.smartshop.domain.entity.OrderItem;
import com.jartiste.smartshop.domain.entity.Product;
import com.jartiste.smartshop.domain.exception.ResourceNotFound;
import com.jartiste.smartshop.domain.repository.ClientRepository;
import com.jartiste.smartshop.domain.repository.OrderRepository;
import com.jartiste.smartshop.domain.repository.ProductRepository;
import com.jartiste.smartshop.domain.service.OrderDomainService;
import com.jartiste.smartshop.presentation.dto.request.OrderRequest;
import com.jartiste.smartshop.presentation.dto.response.OrderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ClientRepository clientRepository;
    private final OrderDomainService orderDomainService;
    private final OrderMapper orderMapper;

    public OrderResponse createOrder(OrderRequest request) {
        Client client = this.clientRepository.findById(request.ClientId())
                .orElseThrow(() -> new ResourceNotFound("Client not Found"));

        Order order = orderDomainService.initializeOrder(client, request.promoCode());
        order.setItemList(new ArrayList<>());


        List<OrderItem> orderItems = new ArrayList<>();
        for(var itemReq : request.items()) {
            Product product = this.productRepository.findByIdAndDeletedFalse(itemReq.productId())
                    .orElseThrow(() -> new ResourceNotFound("Product not Found"));

            OrderItem item = OrderItem.builder()
                    .product(product)
                    .quantity(itemReq.quantity())
                    .unitPrice(product.getPrice())
                    .build();

            orderItems.add(item);
        }

        orderDomainService.processOrderItem(order, orderItems);
        orderDomainService.calculateFinalAmounts(order);

        Order savedOrder = this.orderRepository.save(order);

        client.updateStats(savedOrder.getTotalAmount());

        this.clientRepository.save(client);

        return this.orderMapper.toResponse(savedOrder);
    }

    public OrderResponse getOrderById(Long id) {
        return this.orderRepository.findById(id)
                .map(orderMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFound("Order not Found"));
    }

    public Page<OrderResponse> getOrderByClient(Long clientId, Pageable pageable) {
        return this.orderRepository.findByClient_Id(clientId, pageable)
                .map(orderMapper::toResponse);
    }
}
