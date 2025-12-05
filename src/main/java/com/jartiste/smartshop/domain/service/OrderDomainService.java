package com.jartiste.smartshop.domain.service;

import com.jartiste.smartshop.domain.entity.Client;
import com.jartiste.smartshop.domain.entity.Order;
import com.jartiste.smartshop.domain.entity.OrderItem;
import com.jartiste.smartshop.domain.enums.OrderStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class OrderDomainService {

    public Order initializeOrder(Client client, String promoCode) {
        return Order.builder()
                .client(client)
                .promoCode(promoCode)
                .orderStatus(OrderStatus.PENDING)
                .build();
    }

    public void processOrderItem(Order order, List<OrderItem> itemList) {
        BigDecimal subTotal = BigDecimal.ZERO;
        boolean hasInsufficientStock = false;

        for(OrderItem item: itemList) {
            if(!item.getProduct().hasAvailableStock(item.getQuantity())) {
                hasInsufficientStock = true;
                break;
            }
        }

        if(hasInsufficientStock) {
            order.setOrderStatus(OrderStatus.REJECTED);
        }

        for(OrderItem item: itemList) {
            if(order.getOrderStatus() != OrderStatus.REJECTED) {
                item.getProduct().decreaseStock(item.getQuantity());
            }

            item.setOrder(order);
            order.getItemList().add(item);

            BigDecimal linetotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            subTotal = subTotal.add(linetotal);
        }

        order.setSubTotal(subTotal);
    }

    public void calculateFinalAmounts(Order order) {

        BigDecimal discountRate = order.getClient().getDiscountRate(order.getSubTotal());

        if(order.getPromoCode() != null && order.getPromoCode().matches("PROMO-[A-Z0-9]{4}")) {
            discountRate = discountRate.add(BigDecimal.valueOf(0.05));
        }

        BigDecimal discountAmount = order.getSubTotal().multiply(discountRate);


        order.setDiscountAmount(discountAmount.setScale(2, RoundingMode.HALF_UP));

        BigDecimal netHT = order.getSubTotal().subtract(order.getDiscountAmount());

        if(netHT.compareTo(BigDecimal.ZERO) < 0) netHT = BigDecimal.ZERO;

        BigDecimal tva = netHT.multiply(BigDecimal.valueOf(0.20));

        order.setTaxAmount(tva.setScale(2, RoundingMode.HALF_UP));

        BigDecimal total = netHT.add(tva).setScale(2, RoundingMode.HALF_UP);

        order.setTotalAmount(total);
        order.setRemainingAmount(total);
    }
}
