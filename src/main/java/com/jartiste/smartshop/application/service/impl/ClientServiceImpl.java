package com.jartiste.smartshop.application.service.impl;

import com.jartiste.smartshop.application.mapper.ClientMapper;
import com.jartiste.smartshop.application.service.IClientService;
import com.jartiste.smartshop.domain.entity.Client;
import com.jartiste.smartshop.domain.exception.BusinessLogicViolation;
import com.jartiste.smartshop.domain.exception.ResourceNotFound;
import com.jartiste.smartshop.domain.repository.ClientRepository;
import com.jartiste.smartshop.domain.repository.UserRepository;
import com.jartiste.smartshop.infrastructure.util.PasswordUtil;
import com.jartiste.smartshop.presentation.dto.request.ClientRequest;
import com.jartiste.smartshop.presentation.dto.response.ClientResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements IClientService {

    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final ClientMapper clientMapper;
    private final PasswordUtil passwordUtil;


    public ClientResponse createClient(ClientRequest request) {
        if(userRepository.existsByUsername(request.username())) {
            throw new BusinessLogicViolation("username already exists");
        }

        Client client = this.clientMapper.toEntity(request);
        client.setPassword(passwordUtil.hashPassword(request.password()));

        Client newClient = this.clientRepository.save(client);

        return this.clientMapper.toResponse(newClient);
    }

    public ClientResponse getClientById(Long id) {
        return this.clientRepository.findById(id)
                .map(clientMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFound("Client not Found"));
    }

    public Page<ClientResponse> getAllClients(Pageable pageable) {
        return this.clientRepository.findAll(pageable)
                .map(clientMapper::toResponse);
    }

    public ClientResponse updateClient(Long id, ClientRequest request) {
        Client client = this.clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFound("Client not Found"));

        clientMapper.updateEntityFromDto(request, client);

        Client updatedClient = this.clientRepository.save(client);

        return this.clientMapper.toResponse(updatedClient);
    }


    public void deleteClient(Long id) {
        if(!this.clientRepository.existsById(id)) {
            throw  new ResourceNotFound("Client not Found");
        }

        this.clientRepository.deleteById(id);
    }
}
