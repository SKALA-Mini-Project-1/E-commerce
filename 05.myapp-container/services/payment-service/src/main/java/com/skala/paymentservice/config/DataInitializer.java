package com.skala.paymentservice.config;

import com.skala.paymentservice.domain.Payment;
import com.skala.paymentservice.domain.PaymentMethod;
import com.skala.paymentservice.domain.PaymentStatus;
import com.skala.paymentservice.repo.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final PaymentRepository paymentRepository;

    @Override
    public void run(String... args) {
        if (paymentRepository.count() > 0) {
            return;
        }

        log.info("결제 초기 데이터 설정 시작");

        Payment requested = new Payment();
        requested.setPaymentKey("PAY-SAMPLE-01");
        requested.setOrderId(1L);
        requested.setOrderNumber("ORD-SAMPLE-01");
        requested.setUserId(1L);
        requested.setOrderStatusSnapshot("PAYMENT_PENDING");
        requested.setAmount(new BigDecimal("127000.00"));
        requested.setOrderAmountSnapshot(new BigDecimal("127000.00"));
        requested.setProductSnapshot("1:텀블러 x2, 2:기계식 키보드 x1");
        requested.setMethod(PaymentMethod.CARD);
        requested.setStatus(PaymentStatus.REQUESTED);

        Payment approved = new Payment();
        approved.setPaymentKey("PAY-SAMPLE-02");
        approved.setOrderId(2L);
        approved.setOrderNumber("ORD-SAMPLE-02");
        approved.setUserId(2L);
        approved.setOrderStatusSnapshot("PAID");
        approved.setAmount(new BigDecimal("29000.00"));
        approved.setOrderAmountSnapshot(new BigDecimal("29000.00"));
        approved.setProductSnapshot("3:무선 마우스 x1");
        approved.setMethod(PaymentMethod.SIMPLE_PAY);
        approved.setStatus(PaymentStatus.APPROVED);
        approved.setRequestedAt(LocalDateTime.now().minusMinutes(10));
        approved.setApprovedAt(LocalDateTime.now().minusMinutes(5));

        paymentRepository.save(requested);
        paymentRepository.save(approved);

        log.info("결제 초기 데이터 설정 완료");
    }
}
