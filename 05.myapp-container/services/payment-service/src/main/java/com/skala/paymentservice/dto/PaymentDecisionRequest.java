package com.skala.paymentservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "결제 취소 사유 요청")
public class PaymentDecisionRequest {

    @Size(max = 255)
    @Schema(description = "취소 사유", example = "customer requested cancellation")
    private String reason;
}
