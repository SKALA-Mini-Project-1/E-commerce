# payment-service

결제 생성, 승인, 취소를 담당하는 Spring Boot 서비스입니다. 결제 생성과 상태 변경 시 `order-service`를 호출해 주문 존재 여부와 주문 상태를 검증하고, 결제 성공/취소 결과를 주문 상태에 반영합니다.

## 기본 정보

- 애플리케이션명: `payment-service`
- 로컬 포트: `8084`
- 기본 프로파일: `local`
- MariaDB 프로파일: `mariadb`

## 주요 역할

- 주문 기준 결제 생성
- 결제 상태 조회
- 결제 승인 처리
- 결제 취소 처리
- 결제 시점의 주문/상품 스냅샷 저장

## 도메인 모델

`Payment`

- `id`: 결제 ID
- `paymentKey`: 결제 식별 키
- `orderId`: 주문 ID
- `orderNumber`: 주문 번호 스냅샷
- `userId`: 사용자 ID 스냅샷
- `orderStatusSnapshot`: 결제 생성 시점 주문 상태
- `amount`: 실제 결제 금액
- `orderAmountSnapshot`: 주문 총액 스냅샷
- `productSnapshot`: 주문 상품 스냅샷 문자열
- `method`: 결제 수단
- `status`: 결제 상태
- `requestedAt`: 결제 요청 시각
- `approvedAt`: 결제 승인 시각
- `cancelledAt`: 결제 취소 시각
- `failureReason`: 취소/실패 사유

### 결제 수단 값

- `CARD`
- `BANK_TRANSFER`
- `SIMPLE_PAY`

### 결제 상태 값

- `REQUESTED`
- `APPROVED`
- `DECLINED`
- `CANCELLED`
- `REFUNDED`

## API

기본 경로는 `/api` 입니다.

### 1. 결제 목록 조회

- `GET /api/payments`
- Query
  - `status`: 결제 상태 필터

예시:

```http
GET /api/payments?status=APPROVED
```

### 2. 결제 단건 조회

- `GET /api/payments/{id}`

### 3. 주문 기준 결제 조회

- `GET /api/payments/order/{orderId}`

### 4. 결제 생성

- `POST /api/payments`

요청 본문:

```json
{
  "orderId": 1001,
  "amount": 387000,
  "method": "CARD"
}
```

처리 조건:

1. `order-service`에서 주문 존재 확인
2. 주문 상태가 `CREATED` 또는 `PAYMENT_PENDING`인지 확인
3. 결제 금액과 주문 금액 일치 여부 확인
4. 동일 주문에 대한 활성 결제 존재 여부 확인
5. 주문/상품 스냅샷 저장

성공 시 `201 Created`와 결제 정보를 반환합니다.

### 5. 결제 승인

- `POST /api/payments/{id}/confirm`

처리 흐름:

1. 결제 상태를 `APPROVED`로 변경
2. `order-service`에 주문 상태 `PAID` 반영

주문 서비스 연동 실패 시 `503 Service Unavailable`을 반환할 수 있습니다.

### 6. 결제 취소

- `POST /api/payments/{id}/cancel`

요청 본문은 선택입니다.

```json
{
  "reason": "customer requested cancellation"
}
```

처리 흐름:

1. 결제 상태를 `CANCELLED`로 변경
2. 취소 시각 및 사유 저장
3. `order-service`에 주문 취소 반영

## 응답 및 오류 처리

- 결제나 주문이 없으면 `404 Not Found`
- 잘못된 주문 상태, 금액 불일치, 중복 활성 결제 등은 `400 Bad Request`
- 주문 서비스 호출 실패는 `503 Service Unavailable`

현재 오류 응답은 공통 구조가 아니라 문자열 본문일 수 있습니다.

## 서비스 간 연동

### order-service 호출

- 용도: 주문 조회, 주문 상태 변경, 주문 취소 반영
- 로컬 기본 주소: `http://localhost:8083`
- k8s 기본 주소: `http://order-service:8080`

## 실행

로컬 H2 실행:

```bash
./mvnw spring-boot:run
```

MariaDB 실행:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=mariadb
```

## 참고 경로

- 애플리케이션 설정: `src/main/resources/application.yaml`
- 로컬 설정: `src/main/resources/application-local.yaml`
- MariaDB 설정: `src/main/resources/application-mariadb.yaml`
- API 구현: `src/main/java/com/skala/paymentservice/controller/PaymentController.java`
- 결제 처리: `src/main/java/com/skala/paymentservice/service/PaymentService.java`
