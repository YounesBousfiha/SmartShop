package com.jartiste.smartshop.domain.service;


import com.jartiste.smartshop.domain.entity.Client;
import com.jartiste.smartshop.domain.entity.Order;
import com.jartiste.smartshop.domain.entity.OrderItem;
import com.jartiste.smartshop.domain.entity.Product;
import com.jartiste.smartshop.domain.enums.CustomerTier;
import com.jartiste.smartshop.domain.enums.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class OrderDomainServiceTest {

    @InjectMocks
    private OrderDomainService orderDomainService;

    private Client goldClient;
    private Product laptop;

    @BeforeEach
    void setup() {
        goldClient = Client.builder()
                .id(1L)
                .username("gold_user")
                .tier(CustomerTier.GOLD)
                .build();

        laptop = Product.builder()
                .id(100L)
                .name("MacBook")
                .price(BigDecimal.valueOf(1000.00))
                .stock(10)
                .deleted(false)
                .build();
    }

    @Test
    @DisplayName("Should Set the Data")
    void initializeOrderTest() {
        // public Order initializeOrder(Client client, String promoCode)

        Order order = orderDomainService.initializeOrder(goldClient, "PROMO-1234");

        assertEquals(goldClient.getNom(), order.getClient().getNom());
        assertEquals("PROMO-1234", order.getPromoCode());
        assertEquals(OrderStatus.PENDING, order.getOrderStatus());
    }

    @Test
    @DisplayName("Should Calculate correct total for GOLD Client")
    void shouldCalculateTotalWithGoldDiscount() {
        Order order = Order.builder()
                .client(goldClient)
                .subTotal(BigDecimal.valueOf(1000.00))
                .build();

        orderDomainService.calculateFinalAmounts(order);

        assertEquals(0, BigDecimal.valueOf(1000.00).compareTo(order.getSubTotal()));
        assertEquals(0, BigDecimal.valueOf(180.00).compareTo(order.getTaxAmount()));
        assertEquals(0, BigDecimal.valueOf(1080.00).compareTo(order.getTotalAmount()));
    }

    @Test
    @DisplayName("Should calculate total for Basic Client without discount")
    void shouldCalculateTotalForBasicClient() {
        Client clietBasic = Client.builder()
                .tier(CustomerTier.BASIC)
                .build();

        Order order = Order.builder()
                .client(clietBasic)
                .subTotal(BigDecimal.valueOf(1000.00))
                .build();


        orderDomainService.calculateFinalAmounts(order);

        assertEquals(0, BigDecimal.ZERO.compareTo(order.getDiscountAmount()));
        assertEquals(0, BigDecimal.valueOf(200.00).compareTo(order.getTaxAmount()));
    }

    @Test
    @DisplayName("Should decrease product stock when processing it")
    void shouldDecreseStock() {
        Order order = new Order();
        order.setItemList(new ArrayList<>());
        order.setOrderStatus(OrderStatus.PENDING);

        OrderItem item = OrderItem.builder()
                .product(laptop)
                .quantity(2)
                .unitPrice(laptop.getPrice())
                .build();

        orderDomainService.processOrderItem(order, List.of(item));

        assertEquals(8, laptop.getStock());
        assertEquals(BigDecimal.valueOf(2000.00), order.getSubTotal());
        assertEquals(OrderStatus.PENDING, order.getOrderStatus());
    }

    @Test
    @DisplayName("Should create order with REJECTED status when stock is insufficient")
    void shouldCreateRejectedOrderWhenStockIsLow() {
        Order order = new Order();
        order.setItemList(new ArrayList<>());
        OrderItem item = OrderItem.builder()
                .product(laptop)
                .quantity(20)
                .unitPrice(laptop.getPrice())
                .build();

        orderDomainService.processOrderItem(order, List.of(item));

        assertEquals(OrderStatus.REJECTED, order.getOrderStatus());
        assertEquals(10, laptop.getStock());
        assertEquals(BigDecimal.valueOf(20000.00), order.getSubTotal());
    }
}
