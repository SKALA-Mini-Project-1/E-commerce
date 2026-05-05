package com.skala.productservice.repo;

import com.skala.productservice.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByNameContainingIgnoreCase(String name);

    List<Product> findByActive(Boolean active);

    List<Product> findByNameContainingIgnoreCaseAndActive(String name, Boolean active);

    Optional<Product> findBySku(String sku);

    boolean existsBySku(String sku);
}
