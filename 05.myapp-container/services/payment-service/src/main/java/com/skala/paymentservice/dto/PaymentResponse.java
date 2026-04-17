package com.skala.paymentservice.dto;

import com.skala.paymentservice.domain.Payment;
import com.skala.paymentservice.domain.PaymentMethod;
import com.skala.paymentservice.domain.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "결제 응답")
public record PaymentResponse(
        @Schema(description = "결제 ID", example = "2001")
        Long id,
        @Schema(description = "결제 키", example = "PAY-ABCD1234")
        String paymentKey,
        @Schema(description = "주문 ID", example = "1001")
        Long orderId,
        @Schema(description = "주문 번호", example = "ORD-ABC12345")
        String orderNumber,
        @Schema(description = "사용자 ID", example = "1")
        Long userId,
        @Schema(description = "주문 상태 스냅샷", example = "PAYMENT_PENDING")
        String orderStatusSnapshot,
        @Schema(description = "결제 금액", example = "387000")
        BigDecimal amount,
        @Schema(description = "주문 금액 스냅샷", example = "387000")
        BigDecimal orderAmountSnapshot,
        @Schema(description = "상품 스냅샷", example = "10:Mechanical Keyboard x2, 11:Mouse x1")
        String productSnapshot,
        @Schema(description = "결제 수단")
        PaymentMethod method,
        @Schema(description = "결제 상태")
        PaymentStatus status,
        @Schema(description = "결제 요청 시각")
        LocalDateTime requestedAt,
        @Schema(description = "결제 승인 시각")
        LocalDateTime approvedAt,
        @Schema(description = "결제 취소 시각")
        LocalDateTime cancelledAt,
        @Schema(description = "실패 또는 취소 사유")
        String failureReason
) {
    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getPaymentKey(),
                payment.getOrderId(),
                payment.getOrderNumber(),
                payment.getUserId(),
                payment.getOrderStatusSnapshot(),
                payment.getAmount(),
                payment.getOrderAmountSnapshot(),
                payment.getProductSnapshot(),
                payment.getMethod(),
                payment.getStatus(),
                payment.getRequestedAt(),
                payment.getApprovedAt(),
                payment.getCancelledAt(),
                payment.getFailureReason()
        );
    }
}
