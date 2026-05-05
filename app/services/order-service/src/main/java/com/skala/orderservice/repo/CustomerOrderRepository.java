package com.skala.orderservice.repo;

import com.skala.orderservice.domain.CustomerOrder;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {

    @Override
    @EntityGraph(attributePaths = "items")
    List<CustomerOrder> findAll();

    @Override
    @EntityGraph(attributePaths = "items")
    Optional<CustomerOrder> findById(Long id);

    @EntityGraph(attributePaths = "items")
    Optional<CustomerOrder> findByOrderNumber(String orderNumber);

    @EntityGraph(attributePaths = "items")
    List<CustomerOrder> findByUserId(Long userId);
}
