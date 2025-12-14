package com.jartiste.smartshop.application.service;

import com.jartiste.smartshop.presentation.dto.request.ClientRequest;
import com.jartiste.smartshop.presentation.dto.response.ClientResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IClientService {
    ClientResponse createClient(ClientRequest request);
    ClientResponse getClientById(Long id);
    Page<ClientResponse> getAllClients(Pageable pageable);
    ClientResponse updateClient(Long id, ClientRequest request);
    void deleteClient(Long id);
}
