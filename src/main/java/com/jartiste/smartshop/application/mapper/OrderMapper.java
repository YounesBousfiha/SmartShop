package com.jartiste.smartshop.application.mapper;


import com.jartiste.smartshop.domain.entity.Order;
import com.jartiste.smartshop.domain.entity.OrderItem;
import com.jartiste.smartshop.presentation.dto.request.OrderRequest;
import com.jartiste.smartshop.presentation.dto.response.OrderItemResponse;
import com.jartiste.smartshop.presentation.dto.response.OrderResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    Order toEntity(OrderRequest request);

    @Mapping(source = "client.id", target = "clientId")
    @Mapping(source = "client.nom", target = "clientName")
    @Mapping(source = "itemList", target = "itemList")
    @Mapping(source = "orderStatus", target = "status")
    OrderResponse toResponse(Order order);

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    @Mapping(target = "totalLine", expression = "java(item.getUnitPrice().multiply(java.math.BigDecimal.valueOf(item.getQuantity()")
    OrderItemResponse toItemResponse(OrderItem item);
}
