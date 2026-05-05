# product-service

제품 마스터 데이터와 재고를 관리하는 Spring Boot 서비스입니다. 주문 생성 시 `order-service`가 이 서비스를 호출해 상품 존재 여부를 확인하고 재고를 차감하거나 복구합니다.

## 기본 정보

- 애플리케이션명: `product-service`
- 로컬 포트: `8082`
- 기본 프로파일: `local`
- MariaDB 프로파일: `mariadb`

## 주요 역할

- 상품 등록, 조회, 수정, 삭제
- 상품 SKU 기반 조회
- 재고 수량 직접 수정
- 주문 생성 보상 처리를 위한 재고 차감/복구 API 제공

## 도메인 모델

`Product`

- `id`: 상품 ID
- `sku`: 상품 식별 코드
- `name`: 상품명
- `description`: 상품 설명
- `price`: 가격
- `stockQuantity`: 재고 수량
- `active`: 판매 여부

## API

기본 경로는 `/api` 입니다.

### 1. 상품 목록 조회

- `GET /api/products`
- Query
  - `name`: 상품명 부분 검색
  - `active`: 판매 여부 필터

예시:

```http
GET /api/products?name=mouse&active=true
```

### 2. 상품 단건 조회

- `GET /api/products/{id}`

### 3. SKU로 상품 조회

- `GET /api/products/sku/{sku}`

### 4. 상품 등록

- `POST /api/products`

요청 본문:

```json
{
  "sku": "SKU-1001",
  "name": "Mechanical Keyboard",
  "description": "87-key keyboard",
  "price": 129000,
  "stockQuantity": 50,
  "active": true
}
```

성공 시 `201 Created`와 생성된 상품을 반환합니다.

### 5. 상품 수정

- `PUT /api/products/{id}`

요청 본문은 상품 등록과 동일한 구조입니다.

### 6. 재고 수량 직접 수정

- `PATCH /api/products/{id}/stock`

요청 본문:

```json
{
  "stockQuantity": 120
}
```

### 7. 재고 차감

- `POST /api/products/{id}/deduct-stock`

요청 본문:

```json
{
  "quantity": 2
}
```

주문 생성 시 `order-service`가 호출합니다. 재고 부족 시 `400 Bad Request`를 반환합니다.

### 8. 재고 복구

- `POST /api/products/{id}/restore-stock`

요청 본문:

```json
{
  "quantity": 2
}
```

주문 생성 중간 실패나 주문 저장 실패 시 보상 처리로 사용합니다.

### 9. 상품 삭제

- `DELETE /api/products/{id}`

성공 시 `204 No Content`를 반환합니다.

## 응답 및 오류 처리

- 조회 대상이 없으면 `404 Not Found`
- 잘못된 요청이나 비즈니스 검증 실패 시 `400 Bad Request`
- 현재 공통 예외 포맷은 분리되어 있지 않아 오류 메시지는 문자열 본문으로 반환될 수 있습니다.

## 연동 포인트

- `order-service` -> `product-service`
  - 상품 조회
  - 재고 차감
  - 재고 복구

Kubernetes 내부 호출 기본 주소:

- `http://product-service:8080`

로컬 호출 기본 주소:

- `http://localhost:8082`

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
- API 구현: `src/main/java/com/skala/productservice/controller/ProductController.java`
