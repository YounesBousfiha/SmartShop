package com.jartiste.smartshop.application.service;


import com.jartiste.smartshop.application.mapper.ClientMapper;
import com.jartiste.smartshop.domain.entity.Client;
import com.jartiste.smartshop.domain.enums.CustomerTier;
import com.jartiste.smartshop.domain.enums.UserRole;
import com.jartiste.smartshop.domain.repository.ClientRepository;
import com.jartiste.smartshop.domain.repository.UserRepository;
import com.jartiste.smartshop.infrastructure.util.PasswordUtil;
import com.jartiste.smartshop.presentation.dto.request.ClientRequest;
import com.jartiste.smartshop.presentation.dto.response.ClientResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.any;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ClientMapper clientMapper;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ClientService clientService;

    @Spy
    private final PasswordUtil passwordUtil = new PasswordUtil();

    @Test
    void shouldCreateClient() {
        Client client = Client.builder()
                .id(2L)
                .nom("tester")
                .username("tester_user")
                .tier(CustomerTier.BASIC)
                .password(passwordUtil.hashPassword("test123"))
                .role(UserRole.CLIENT)
                .build();

        ClientRequest request = new ClientRequest("tester", "tester_user", passwordUtil.hashPassword("test123"));
        ClientResponse expectedResponse = ClientResponse.builder()
                .id(2L)
                .nom("tester")
                .username("tester_user")
                .role(UserRole.CLIENT)
                .tier(CustomerTier.BASIC)
                .totalOrders(0)
                .totalSpent(BigDecimal.ZERO)
                .build();

        when(userRepository.existsByUsername(request.username())).thenReturn(false);

        when(clientMapper.toEntity(ArgumentMatchers.any(ClientRequest.class))).thenReturn(client);

        when(clientRepository.save(ArgumentMatchers.any(Client.class))).thenReturn(client);
        when(clientMapper.toResponse(ArgumentMatchers.any(Client.class))).thenReturn(
                new ClientResponse(2L, "tester", "tester_user", UserRole.CLIENT, CustomerTier.BASIC, 0, BigDecimal.ZERO)
        );

        ClientResponse response = clientService.createClient(request);

        assertNotNull(response);
        assertEquals(expectedResponse.id(), response.id());
    }

    @Test
    void shouldThrowExceptionIfUsernameAlreadyExists() {}

    @Test
    void shouldGetClientById() {}

    @Test
    void shouldGetAllClientPaginated() {}

    @Test
    void shouldUpdateClient() {}

    @Test
    void shouldThrowExceptionIfUpdatedClientNotExists() {}

    @Test
    void shouldDeleteClient() {}

    @Test
    void shouldThrowExceptionIfDeletedClientNotExists() {}
}
