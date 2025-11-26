package com.jartiste.smartshop.presentation.controller;

import com.jartiste.smartshop.application.service.ClientService;
import com.jartiste.smartshop.domain.enums.UserRole;
import com.jartiste.smartshop.domain.exception.ForbiddenException;
import com.jartiste.smartshop.presentation.annotation.RequireRole;
import com.jartiste.smartshop.presentation.dto.request.ClientRequest;
import com.jartiste.smartshop.presentation.dto.response.ClientResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    @PostMapping
    @RequireRole(UserRole.ADMIN)
    public ResponseEntity<Long> createClient(@RequestBody ClientRequest request) {
        ClientResponse response = this.clientService.createClient(request);
        return ResponseEntity.created(URI.create("/api/clients/" + response.id())).body(response.id());
    }

    @GetMapping
    @RequireRole(UserRole.ADMIN)
    public ResponseEntity<Page<ClientResponse>> getAllClients(
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "0") int page
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ClientResponse> clientResponses = this.clientService.getAllClients(pageable);
        return ResponseEntity.ok(clientResponses);
    }

    @GetMapping("/{id}")
    @RequireRole({UserRole.ADMIN, UserRole.CLIENT})
    public ResponseEntity<ClientResponse> getClient(@PathVariable Long id, HttpSession session) {
        validateAccess(id, session);
        ClientResponse response = this.clientService.getClientById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @RequireRole(UserRole.ADMIN)
    public ResponseEntity<ClientResponse> updateClient(
            @PathVariable Long id,
            @RequestBody ClientRequest request,
            HttpSession session
    ) {
        validateAccess(id, session);
        ClientResponse response = this.clientService.updateClient(id, request);
        return ResponseEntity.ok(response);
    }


    @DeleteMapping("/{id}")
    @RequireRole(UserRole.ADMIN)
    public ResponseEntity<Void> deleteClient(@PathVariable Long id) {
        this.clientService.deleteClient(id);
        return ResponseEntity.noContent().build();
    }

    private void validateAccess(Long targetId, HttpSession session) {
        UserRole role = (UserRole)  session.getAttribute("USER_ROLE");
        Long currentUserId = (Long) session.getAttribute("USER_ID");

        if(UserRole.ADMIN.equals(role)) {
            return;
        }

        if(!targetId.equals(currentUserId)) {
            throw new ForbiddenException("You don't have the permission");
        }
    }
}
