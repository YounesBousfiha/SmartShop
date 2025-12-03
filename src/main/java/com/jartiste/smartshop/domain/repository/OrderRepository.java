package com.jartiste.smartshop.domain.repository;

import com.jartiste.smartshop.domain.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByClient_Id(Long clientId, Pageable pageable);
    Page<Order> findAll(Pageable pageable);
}
