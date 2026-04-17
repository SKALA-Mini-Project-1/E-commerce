package com.skala.productservice.service;

import com.skala.productservice.common.api.ErrorCode;
import com.skala.productservice.common.exception.BusinessException;
import com.skala.productservice.common.exception.ResourceNotFoundException;
import com.skala.productservice.domain.Product;
import com.skala.productservice.dto.ProductRequest;
import com.skala.productservice.repo.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;

    public List<Product> findAll(Optional<String> name, Optional<Boolean> active) {
        if (name.isPresent() && active.isPresent()) {
            return productRepository.findByNameContainingIgnoreCaseAndActive(name.get(), active.get());
        }
        if (name.isPresent()) {
            return productRepository.findByNameContainingIgnoreCase(name.get());
        }
        if (active.isPresent()) {
            return productRepository.findByActive(active.get());
        }
        return productRepository.findAll();
    }

    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }

    public Optional<Product> findBySku(String sku) {
        return productRepository.findBySku(sku);
    }

    @Transactional
    public Product create(ProductRequest request) {
        validateProduct(request);
        if (productRepository.existsBySku(request.getSku())) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION, "이미 존재하는 SKU입니다: " + request.getSku());
        }
        Product product = toEntity(request);
        return productRepository.save(product);
    }

    @Transactional
    public Product update(Long id, ProductRequest request) {
        validateProduct(request);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("상품을 찾을 수 없습니다: " + id));

        if (!product.getSku().equals(request.getSku())
                && productRepository.existsBySku(request.getSku())) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION, "이미 존재하는 SKU입니다: " + request.getSku());
        }

        product.setSku(request.getSku());
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setActive(request.getActive() == null ? Boolean.TRUE : request.getActive());
        return productRepository.save(product);
    }

    @Transactional
    public Product updateStock(Long id, Integer stockQuantity) {
        if (stockQuantity == null || stockQuantity < 0) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "재고는 0 이상이어야 합니다.");
        }
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("상품을 찾을 수 없습니다: " + id));
        product.setStockQuantity(stockQuantity);
        return productRepository.save(product);
    }

    @Transactional
    public Product deductStock(Long id, Integer quantity) {
        if (quantity == null || quantity < 1) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "차감 수량은 1 이상이어야 합니다.");
        }
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("상품을 찾을 수 없습니다: " + id));
        if (product.getStockQuantity() < quantity) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION, "재고가 부족합니다.");
        }
        product.setStockQuantity(product.getStockQuantity() - quantity);
        return productRepository.save(product);
    }

    @Transactional
    public Product restoreStock(Long id, Integer quantity) {
        if (quantity == null || quantity < 1) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "복구 수량은 1 이상이어야 합니다.");
        }
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("상품을 찾을 수 없습니다: " + id));
        product.setStockQuantity(product.getStockQuantity() + quantity);
        return productRepository.save(product);
    }

    @Transactional
    public boolean delete(Long id) {
        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
            return true;
        }
        return false;
    }

    private Product toEntity(ProductRequest request) {
        Product product = new Product();
        product.setSku(request.getSku());
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setActive(request.getActive() == null ? Boolean.TRUE : request.getActive());
        return product;
    }

    private void validateProduct(ProductRequest request) {
        if (request.getPrice() == null || request.getPrice().signum() < 0) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "가격은 0 이상이어야 합니다.");
        }
        if (request.getStockQuantity() == null || request.getStockQuantity() < 0) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "재고는 0 이상이어야 합니다.");
        }
    }
}
