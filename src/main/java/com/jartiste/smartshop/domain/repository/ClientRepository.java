package com.jartiste.smartshop.domain.repository;

import com.jartiste.smartshop.domain.entity.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    Page<Client> findByNomContainingIgnoreCase(String keyword, Pageable pageable);

    Object existsByUsername(String username);
}
