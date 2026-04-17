package com.skala.paymentservice.service;

import com.skala.paymentservice.client.OrderClient;
import com.skala.paymentservice.common.api.ErrorCode;
import com.skala.paymentservice.common.exception.BusinessException;
import com.skala.paymentservice.domain.Payment;
import com.skala.paymentservice.domain.PaymentStatus;
import com.skala.paymentservice.dto.CreatePaymentRequest;
import com.skala.paymentservice.repo.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderClient orderClient;

    public List<Payment> findAll(Optional<PaymentStatus> status) {
        return status.map(paymentRepository::findByStatus)
                .orElseGet(paymentRepository::findAll);
    }

    public Optional<Payment> findById(Long id) {
        return paymentRepository.findById(id);
    }

    public Optional<Payment> findByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId);
    }

    @Transactional
    public Payment create(CreatePaymentRequest request) {
        OrderClient.OrderSummary order = orderClient.getOrder(request.getOrderId());
        validateOrderForPayment(order, request);

        paymentRepository.findByOrderId(request.getOrderId())
                .ifPresent(existing -> {
                    if (existing.getStatus() != PaymentStatus.CANCELLED && existing.getStatus() != PaymentStatus.REFUNDED) {
                        throw new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION,
                                "이미 결제가 생성된 주문입니다: " + request.getOrderId());
                    }
                });

        Payment payment = new Payment();
        payment.setPaymentKey(generatePaymentKey());
        payment.setOrderId(request.getOrderId());
        payment.setOrderNumber(order.orderNumber());
        payment.setUserId(order.userId());
        payment.setOrderStatusSnapshot(order.status());
        payment.setAmount(request.getAmount());
        payment.setOrderAmountSnapshot(order.totalAmount());
        payment.setProductSnapshot(buildProductSnapshot(order));
        payment.setMethod(request.getMethod());
        payment.setStatus(PaymentStatus.REQUESTED);
        return paymentRepository.save(payment);
    }

    @Transactional
    public Optional<Payment> confirm(Long id) {
        return paymentRepository.findById(id)
                .map(payment -> {
                    payment.setStatus(PaymentStatus.APPROVED);
                    payment.setApprovedAt(LocalDateTime.now());
                    payment.setFailureReason(null);
                    Payment savedPayment = paymentRepository.save(payment);
                    orderClient.markOrderPaid(payment.getOrderId());
                    return savedPayment;
                });
    }

    @Transactional
    public Optional<Payment> cancel(Long id, String reason) {
        return paymentRepository.findById(id)
                .map(payment -> {
                    payment.setStatus(PaymentStatus.CANCELLED);
                    payment.setCancelledAt(LocalDateTime.now());
                    payment.setFailureReason(reason);
                    Payment savedPayment = paymentRepository.save(payment);
                    orderClient.cancelOrder(payment.getOrderId());
                    return savedPayment;
                });
    }

    private String generatePaymentKey() {
        return "PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private void validateOrderForPayment(OrderClient.OrderSummary order, CreatePaymentRequest request) {
        if (order.totalAmount() == null || order.totalAmount().compareTo(request.getAmount()) != 0) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION, "주문 금액과 결제 금액이 일치하지 않습니다.");
        }

        if (order.items() == null || order.items().isEmpty()) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION, "주문 상품 정보가 없어 결제를 진행할 수 없습니다.");
        }

        if (!"CREATED".equals(order.status()) && !"PAYMENT_PENDING".equals(order.status())) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION,
                    "결제를 진행할 수 없는 주문 상태입니다: " + order.status());
        }
    }

    private String buildProductSnapshot(OrderClient.OrderSummary order) {
        return order.items().stream()
                .sorted(Comparator.comparing(OrderClient.OrderItemSummary::productId))
                .map(item -> "%d:%s x%d".formatted(item.productId(), item.productName(), item.quantity()))
                .reduce((left, right) -> left + ", " + right)
                .orElse("");
    }
}
