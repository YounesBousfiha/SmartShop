package com.jartiste.smartshop.application.mapper;

import com.jartiste.smartshop.domain.entity.Client;
import com.jartiste.smartshop.presentation.dto.request.ClientRequest;
import com.jartiste.smartshop.presentation.dto.response.ClientResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ClientMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", constant = "CLIENT")
    @Mapping(target = "tier", constant = "BASIC")
    @Mapping(target = "totalOrders", constant = "0")
    @Mapping(target = "totalSpent", constant = "0.0")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "password", ignore = true)
    Client toEntity(ClientRequest request);

    ClientResponse toResponse(Client client);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "tier", ignore = true)
    @Mapping(target = "totalOrders", ignore = true)
    @Mapping(target = "totalSpent", ignore = true)
    void updateEntityFromDto(ClientRequest request, @MappingTarget Client client);
}
