package com.skala.productservice.config;

import com.skala.productservice.domain.Product;
import com.skala.productservice.repo.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final ProductRepository productRepository;

    @Override
    public void run(String... args) {
        if (productRepository.count() > 0) {
            return;
        }

        log.info("상품 초기 데이터 설정 시작");
        productRepository.save(new Product(null, "SKU-1001", "텀블러", "보온 텀블러", new BigDecimal("19000.00"), 120, true));
        productRepository.save(new Product(null, "SKU-1002", "기계식 키보드", "87키 배열 기계식 키보드", new BigDecimal("89000.00"), 40, true));
        productRepository.save(new Product(null, "SKU-1003", "무선 마우스", "사무용 무선 마우스", new BigDecimal("29000.00"), 75, true));
        log.info("상품 초기 데이터 설정 완료");
    }
}
