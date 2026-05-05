package com.skala.orderservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skala.orderservice.domain.CustomerOrder;
import com.skala.orderservice.domain.OutboxEvent;
import com.skala.orderservice.repo.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OutboxEventService {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public void enqueueOrderCreated(CustomerOrder order) {
        OutboxEvent event = new OutboxEvent();
        event.setAggregateType("order");
        event.setAggregateId(String.valueOf(order.getId()));
        event.setEventType("ORDER_CREATED");
        event.setPayload(toJson(new OrderCreatedPayload(
                order.getId(),
                order.getOrderNumber(),
                order.getUserId(),
                order.getStatus().name(),
                order.getTotalAmount().toPlainString(),
                order.getCreatedAt(),
                order.getItems().stream()
                        .map(item -> new OrderCreatedItemPayload(
                                item.getProductId(),
                                item.getProductName(),
                                item.getQuantity(),
                                item.getUnitPrice().toPlainString(),
                                item.getSubtotal().toPlainString()
                        ))
                        .toList()
        )));
        outboxEventRepository.save(event);
    }

    private String toJson(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Outbox payload serialization failed", e);
        }
    }

    private record OrderCreatedPayload(
            Long orderId,
            String orderNumber,
            Long userId,
            String status,
            String totalAmount,
            java.time.LocalDateTime createdAt,
            List<OrderCreatedItemPayload> items
    ) {
    }

    private record OrderCreatedItemPayload(
            Long productId,
            String productName,
            Integer quantity,
            String unitPrice,
            String subtotal
    ) {
    }
}
