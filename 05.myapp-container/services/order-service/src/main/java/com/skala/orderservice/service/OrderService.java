package com.skala.orderservice.service;

import com.skala.orderservice.client.ProductClient;
import com.skala.orderservice.client.UserClient;
import com.skala.orderservice.common.api.ErrorCode;
import com.skala.orderservice.common.exception.BusinessException;
import com.skala.orderservice.domain.CustomerOrder;
import com.skala.orderservice.domain.OrderItem;
import com.skala.orderservice.domain.OrderStatus;
import com.skala.orderservice.dto.CreateOrderItemRequest;
import com.skala.orderservice.dto.CreateOrderRequest;
import com.skala.orderservice.repo.CustomerOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final CustomerOrderRepository customerOrderRepository;
    private final UserClient userClient;
    private final ProductClient productClient;
    private final OutboxEventService outboxEventService;

    public List<CustomerOrder> findAll() {
        return customerOrderRepository.findAll();
    }

    public Optional<CustomerOrder> findById(Long id) {
        return customerOrderRepository.findById(id);
    }

    public Optional<CustomerOrder> findByOrderNumber(String orderNumber) {
        return customerOrderRepository.findByOrderNumber(orderNumber);
    }

    public List<CustomerOrder> findByUserId(Long userId) {
        return customerOrderRepository.findByUserId(userId);
    }

    @Transactional
    public CustomerOrder create(CreateOrderRequest request) {
        userClient.assertUserExists(request.getUserId());

        List<ResolvedOrderItem> resolvedItems = resolveOrderItems(request.getItems());
        List<StockReservation> reservedStocks = new ArrayList<>();

        CustomerOrder order = new CustomerOrder();
        order.setOrderNumber(generateOrderNumber());
        order.setUserId(request.getUserId());
        order.setStatus(OrderStatus.PAYMENT_PENDING);
        order.setTotalAmount(BigDecimal.ZERO);

        try {
            for (ResolvedOrderItem item : resolvedItems) {
                productClient.deductStock(item.productId(), item.quantity());
                reservedStocks.add(new StockReservation(item.productId(), item.quantity()));
            }

            order.replaceItems(toOrderItems(resolvedItems));
            order.setTotalAmount(calculateTotal(order.getItems()));
            CustomerOrder savedOrder = customerOrderRepository.save(order);
            outboxEventService.enqueueOrderCreated(savedOrder);
            return savedOrder;
        } catch (RuntimeException e) {
            compensateReservedStocks(reservedStocks);
            throw e;
        }
    }

    @Transactional
    public Optional<CustomerOrder> updateStatus(Long id, OrderStatus status) {
        return customerOrderRepository.findById(id)
                .map(order -> {
                    order.setStatus(status);
                    return customerOrderRepository.save(order);
                });
    }

    @Transactional
    public Optional<CustomerOrder> cancel(Long id) {
        return customerOrderRepository.findById(id)
                .map(order -> {
                    if (order.getStatus() == OrderStatus.CANCELLED) {
                        return order;
                    }

                    restoreOrderStocks(order);
                    order.setStatus(OrderStatus.CANCELLED);
                    return customerOrderRepository.save(order);
                });
    }

    private List<ResolvedOrderItem> resolveOrderItems(List<CreateOrderItemRequest> items) {
        return items.stream()
                .map(item -> {
                    ProductClient.ProductSummary product = productClient.getProduct(item.getProductId());

                    if (!Boolean.TRUE.equals(product.getActive())) {
                        throw new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION,
                                "비활성 상품은 주문할 수 없습니다: " + item.getProductId());
                    }
                    if (product.getStockQuantity() == null || product.getStockQuantity() < item.getQuantity()) {
                        throw new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION,
                                "재고가 부족합니다: " + item.getProductId());
                    }
                    return new ResolvedOrderItem(
                            item.getProductId(),
                            product.getName(),
                            product.getPrice(),
                            item.getQuantity()
                    );
                })
                .toList();
    }

    private List<OrderItem> toOrderItems(List<ResolvedOrderItem> items) {
        return items.stream()
                .map(item -> new OrderItem(
                        null,
                        item.productId(),
                        item.productName(),
                        item.unitPrice(),
                        item.quantity(),
                        item.unitPrice().multiply(BigDecimal.valueOf(item.quantity())),
                        null
                ))
                .toList();
    }

    private BigDecimal calculateTotal(List<OrderItem> items) {
        return items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void compensateReservedStocks(List<StockReservation> reservedStocks) {
        for (int i = reservedStocks.size() - 1; i >= 0; i--) {
            StockReservation reservation = reservedStocks.get(i);
            try {
                productClient.restoreStock(reservation.productId(), reservation.quantity());
            } catch (RuntimeException e) {
                // 보상 실패는 로깅 가능한 지점이지만, 현재 구조에서는 원래 예외를 우선 유지한다.
            }
        }
    }

    private void restoreOrderStocks(CustomerOrder order) {
        List<StockReservation> reservations = order.getItems().stream()
                .map(item -> new StockReservation(item.getProductId(), item.getQuantity()))
                .toList();

        for (StockReservation reservation : reservations) {
            productClient.restoreStock(reservation.productId(), reservation.quantity());
        }
    }

    private String generateOrderNumber() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private record ResolvedOrderItem(Long productId, String productName, BigDecimal unitPrice, Integer quantity) {
    }

    private record StockReservation(Long productId, Integer quantity) {
    }
}
