package com.skala.paymentservice.controller;

import com.skala.paymentservice.common.exception.ResourceNotFoundException;
import com.skala.paymentservice.domain.PaymentStatus;
import com.skala.paymentservice.dto.CreatePaymentRequest;
import com.skala.paymentservice.dto.PaymentDecisionRequest;
import com.skala.paymentservice.dto.PaymentResponse;
import com.skala.paymentservice.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Validated
@Tag(name = "Payments", description = "결제 관리 API")
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "결제 목록 조회")
    @GetMapping("/payments")
    public ResponseEntity<List<PaymentResponse>> getPayments(@RequestParam Optional<PaymentStatus> status) {
        return ResponseEntity.ok(paymentService.findAll(status).stream()
                .map(PaymentResponse::from)
                .toList());
    }

    @Operation(summary = "결제 단건 조회")
    @GetMapping("/payments/{id}")
    public ResponseEntity<PaymentResponse> getPaymentById(@PathVariable @Positive Long id) {
        return paymentService.findById(id)
                .map(PaymentResponse::from)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("결제를 찾을 수 없습니다: " + id));
    }

    @Operation(summary = "주문 기준 결제 조회")
    @GetMapping("/payments/order/{orderId}")
    public ResponseEntity<PaymentResponse> getPaymentByOrderId(@PathVariable @Positive Long orderId) {
        return paymentService.findByOrderId(orderId)
                .map(PaymentResponse::from)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("결제를 찾을 수 없습니다. orderId=" + orderId));
    }

    @Operation(summary = "결제 생성")
    @PostMapping("/payments")
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody CreatePaymentRequest request) {
        return ResponseEntity.status(201).body(PaymentResponse.from(paymentService.create(request)));
    }

    @Operation(summary = "결제 승인")
    @PostMapping("/payments/{id}/confirm")
    public ResponseEntity<PaymentResponse> confirmPayment(@PathVariable @Positive Long id) {
        return paymentService.confirm(id)
                .map(PaymentResponse::from)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("결제를 찾을 수 없습니다: " + id));
    }

    @Operation(summary = "결제 취소")
    @PostMapping("/payments/{id}/cancel")
    public ResponseEntity<PaymentResponse> cancelPayment(@PathVariable @Positive Long id,
                                                         @Valid @RequestBody(required = false) PaymentDecisionRequest request) {
        String reason = request == null ? null : request.getReason();
        return paymentService.cancel(id, reason)
                .map(PaymentResponse::from)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("결제를 찾을 수 없습니다: " + id));
    }
}
