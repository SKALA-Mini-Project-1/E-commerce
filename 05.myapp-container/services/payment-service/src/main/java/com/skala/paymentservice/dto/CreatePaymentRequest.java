package com.skala.paymentservice.dto;

import com.skala.paymentservice.domain.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Schema(description = "결제 생성 요청")
public class CreatePaymentRequest {

    @NotNull
    @Positive
    @Schema(description = "주문 ID", example = "1001")
    private Long orderId;

    @NotNull
    @DecimalMin("0.0")
    @Schema(description = "결제 금액", example = "387000")
    private BigDecimal amount;

    @NotNull
    @Schema(description = "결제 수단", example = "CARD")
    private PaymentMethod method;
}
