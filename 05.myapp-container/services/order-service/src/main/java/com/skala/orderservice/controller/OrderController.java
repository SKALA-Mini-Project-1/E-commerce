package com.skala.orderservice.controller;

import com.skala.orderservice.common.api.ErrorResponse;
import com.skala.orderservice.common.exception.ResourceNotFoundException;
import com.skala.orderservice.dto.CreateOrderRequest;
import com.skala.orderservice.dto.OrderResponse;
import com.skala.orderservice.dto.OrderStatusUpdateRequest;
import com.skala.orderservice.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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
@Tag(name = "Orders", description = "주문 관리 API")
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "주문 목록 조회")
    @GetMapping("/orders")
    public ResponseEntity<List<OrderResponse>> getOrders(@RequestParam Optional<@Positive Long> userId) {
        List<OrderResponse> responses = userId.map(orderService::findByUserId)
                .orElseGet(orderService::findAll)
                .stream()
                .map(OrderResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "주문 단건 조회")
    @GetMapping("/orders/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable @Positive Long id) {
        return orderService.findById(id)
                .map(OrderResponse::from)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("주문을 찾을 수 없습니다: " + id));
    }

    @Operation(summary = "주문번호 기준 주문 조회")
    @GetMapping("/orders/order-number/{orderNumber}")
    public ResponseEntity<OrderResponse> getOrderByOrderNumber(@PathVariable String orderNumber) {
        return orderService.findByOrderNumber(orderNumber)
                .map(OrderResponse::from)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("주문을 찾을 수 없습니다. orderNumber=" + orderNumber));
    }

    @Operation(summary = "주문 생성")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "주문 생성 성공"),
            @ApiResponse(responseCode = "400", description = "입력값 또는 비즈니스 검증 실패", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "503", description = "외부 서비스 호출 실패", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/orders")
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        return new ResponseEntity<>(OrderResponse.from(orderService.create(request)), HttpStatus.CREATED);
    }

    @Operation(summary = "주문 상태 변경")
    @PatchMapping("/orders/{id}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(@PathVariable @Positive Long id,
                                                           @Valid @RequestBody OrderStatusUpdateRequest request) {
        return orderService.updateStatus(id, request.getStatus())
                .map(OrderResponse::from)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("주문을 찾을 수 없습니다: " + id));
    }

    @Operation(summary = "주문 취소")
    @PostMapping("/orders/{id}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable @Positive Long id) {
        return orderService.cancel(id)
                .map(OrderResponse::from)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("주문을 찾을 수 없습니다: " + id));
    }
}
