# order-service

주문 생성, 조회, 상태 변경을 담당하는 Spring Boot 서비스입니다. 주문 생성 시 `user-service`로 사용자 존재를 검증하고, `product-service`로 상품 조회 및 재고 차감을 수행합니다.

## 기본 정보

- 애플리케이션명: `order-service`
- 로컬 포트: `8083`
- 기본 프로파일: `local`
- MariaDB 프로파일: `mariadb`

## 주요 역할

- 사용자 기준 주문 생성
- 주문 상세 및 주문번호 기반 조회
- 주문 상태 변경
- 결제 성공/취소에 따른 주문 상태 반영
- 주문 생성 중 장애 발생 시 재고 복구 보상 처리

## 도메인 모델

`CustomerOrder`

- `id`: 주문 ID
- `orderNumber`: 주문 번호
- `userId`: 주문 사용자 ID
- `status`: 주문 상태
- `totalAmount`: 총 주문 금액
- `createdAt`: 생성 시각
- `updatedAt`: 수정 시각
- `items`: 주문 상품 목록

`OrderItem`

- `id`: 주문 항목 ID
- `productId`: 상품 ID
- `productName`: 주문 시점 상품명
- `unitPrice`: 주문 시점 단가
- `quantity`: 수량
- `subtotal`: 항목 합계

### 주문 상태 값

- `CREATED`
- `PAYMENT_PENDING`
- `PAID`
- `FAILED`
- `CANCELLED`

## API

기본 경로는 `/api` 입니다.

### 1. 주문 목록 조회

- `GET /api/orders`
- Query
  - `userId`: 특정 사용자 주문만 조회

예시:

```http
GET /api/orders?userId=1
```

### 2. 주문 단건 조회

- `GET /api/orders/{id}`

### 3. 주문번호로 조회

- `GET /api/orders/order-number/{orderNumber}`

### 4. 주문 생성

- `POST /api/orders`

요청 본문:

```json
{
  "userId": 1,
  "items": [
    {
      "productId": 10,
      "quantity": 2
    },
    {
      "productId": 11,
      "quantity": 1
    }
  ]
}
```

처리 흐름:

1. `user-service`에서 사용자 존재 확인
2. `product-service`에서 상품 정보 조회
3. 각 상품 재고 차감
4. 주문 저장
5. 중간 실패 시 이미 차감한 재고 복구

성공 시 `201 Created`와 주문 정보를 반환합니다.

### 5. 주문 상태 변경

- `PATCH /api/orders/{id}/status`

요청 본문:

```json
{
  "status": "PAID"
}
```

주로 `payment-service`가 결제 승인/취소 흐름에서 호출합니다.

### 6. 주문 취소

- `POST /api/orders/{id}/cancel`

결제 취소 후 주문 상태를 `CANCELLED`로 반영할 때 사용합니다.

## 응답 및 오류 처리

- 주문이 없으면 `404 Not Found`
- 사용자 없음, 상품 없음, 재고 부족, 잘못된 상태 변경 등은 `400 Bad Request`
- 외부 서비스 호출 실패는 `503 Service Unavailable`

현재 오류 응답은 공통 포맷이 아니라 문자열 본문일 수 있습니다.

## 서비스 간 연동

### user-service 호출

- 용도: 주문 생성 전 사용자 존재 검증
- 로컬 기본 주소: `http://localhost:8081`
- k8s 기본 주소: `http://user-service:8080`

### product-service 호출

- 용도: 상품 조회, 재고 차감, 재고 복구
- 로컬 기본 주소: `http://localhost:8082`
- k8s 기본 주소: `http://product-service:8080`

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
- API 구현: `src/main/java/com/skala/orderservice/controller/OrderController.java`
- 주문 처리: `src/main/java/com/skala/orderservice/service/OrderService.java`
