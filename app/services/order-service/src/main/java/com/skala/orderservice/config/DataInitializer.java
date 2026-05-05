package com.skala.orderservice.config;

import com.skala.orderservice.domain.CustomerOrder;
import com.skala.orderservice.domain.OrderItem;
import com.skala.orderservice.domain.OrderStatus;
import com.skala.orderservice.repo.CustomerOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final CustomerOrderRepository customerOrderRepository;

    @Override
    public void run(String... args) {
        if (customerOrderRepository.count() > 0) {
            return;
        }

        log.info("주문 초기 데이터 설정 시작");

        CustomerOrder firstOrder = new CustomerOrder();
        firstOrder.setOrderNumber("ORD-SAMPLE-01");
        firstOrder.setUserId(1L);
        firstOrder.setStatus(OrderStatus.PAYMENT_PENDING);
        firstOrder.setTotalAmount(new BigDecimal("127000.00"));
        firstOrder.replaceItems(List.of(
                new OrderItem(null, 1L, "텀블러", new BigDecimal("19000.00"), 2, new BigDecimal("38000.00"), null),
                new OrderItem(null, 2L, "기계식 키보드", new BigDecimal("89000.00"), 1, new BigDecimal("89000.00"), null)
        ));

        CustomerOrder secondOrder = new CustomerOrder();
        secondOrder.setOrderNumber("ORD-SAMPLE-02");
        secondOrder.setUserId(2L);
        secondOrder.setStatus(OrderStatus.PAID);
        secondOrder.setTotalAmount(new BigDecimal("29000.00"));
        secondOrder.replaceItems(List.of(
                new OrderItem(null, 3L, "무선 마우스", new BigDecimal("29000.00"), 1, new BigDecimal("29000.00"), null)
        ));

        customerOrderRepository.save(firstOrder);
        customerOrderRepository.save(secondOrder);

        log.info("주문 초기 데이터 설정 완료");
    }
}
