package com.jartiste.smartshop.presentation.controller;

import com.jartiste.smartshop.application.service.IClientService;
import com.jartiste.smartshop.domain.enums.UserRole;
import com.jartiste.smartshop.domain.exception.ForbiddenException;
import com.jartiste.smartshop.presentation.annotation.RequireRole;
import com.jartiste.smartshop.presentation.dto.request.ClientRequest;
import com.jartiste.smartshop.presentation.dto.response.ClientResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
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
@Tag(name = "Clients", description = "Client management APIs - Admin access required")
public class ClientController {

    private final IClientService clientService;

    @Operation(
            summary = "Create a new client",
            description = "Register a new client in the system (Admin only)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Client created successfully",
                    content = @Content(schema = @Schema(implementation = Long.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input or username already exists",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied",
                    content = @Content
            )
    })
    @PostMapping
    @RequireRole(UserRole.ADMIN)
    public ResponseEntity<Long> createClient(@Valid @RequestBody ClientRequest request) {
        ClientResponse response = this.clientService.createClient(request);
        return ResponseEntity.created(URI.create("/api/clients/" + response.id())).body(response.id());
    }

    @Operation(
            summary = "Get all clients",
            description = "Retrieve paginated list of all clients (Admin only)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved clients",
                    content = @Content(schema = @Schema(implementation = Page.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied",
                    content = @Content
            )
    })
    @GetMapping
    @RequireRole(UserRole.ADMIN)
    public ResponseEntity<Page<ClientResponse>> getAllClients(
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ClientResponse> clientResponses = this.clientService.getAllClients(pageable);
        return ResponseEntity.ok(clientResponses);
    }

    @Operation(
            summary = "Get client by ID",
            description = "Retrieve client details by ID (Admin or own profile)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved client",
                    content = @Content(schema = @Schema(implementation = ClientResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Client not found",
                    content = @Content
            )
    })
    @GetMapping("/{id}")
    @RequireRole({UserRole.ADMIN, UserRole.CLIENT})
    public ResponseEntity<ClientResponse> getClient(
            @Parameter(description = "Client ID") @PathVariable Long id,
            HttpSession session
    ) {
        validateAccess(id, session);
        ClientResponse response = this.clientService.getClientById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Update client",
            description = "Update client information (Admin or own profile)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Client updated successfully",
                    content = @Content(schema = @Schema(implementation = ClientResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Client not found",
                    content = @Content
            )
    })
    @PutMapping("/{id}")
    @RequireRole(UserRole.ADMIN)
    public ResponseEntity<ClientResponse> updateClient(
            @Parameter(description = "Client ID") @PathVariable Long id,
            @Valid @RequestBody ClientRequest request,
            HttpSession session
    ) {
        validateAccess(id, session);
        ClientResponse response = this.clientService.updateClient(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Delete client",
            description = "Delete a client from the system (Admin only)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Client deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Client not found",
                    content = @Content
            )
    })
    @DeleteMapping("/{id}")
    @RequireRole(UserRole.ADMIN)
    public ResponseEntity<Void> deleteClient(@Parameter(description = "Client ID") @PathVariable Long id) {
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
