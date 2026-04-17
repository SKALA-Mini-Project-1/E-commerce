# Current DB ERD

이 프로젝트의 DB 구조는 서비스별 독립 스키마를 사용하는 MSA 형태다.

- `user-service` -> `user_service_db`
- `product-service` -> `product_service_db`
- `order-service` -> `order_service_db`
- `payment-service` -> `payment_service_db`

MariaDB 프로파일에서 각 서비스는 `ddl-auto: update`로 JPA 엔터티 기준 테이블을 생성/갱신한다.

## ERD

```mermaid
erDiagram
    ORDERS ||--|{ ORDER_ITEMS : "order_id (physical FK)"

    USERS {
        BIGINT id PK
        VARCHAR name
        VARCHAR email UK
    }

    PRODUCTS {
        BIGINT id PK
        VARCHAR sku UK
        VARCHAR name
        VARCHAR description NULL
        DECIMAL price
        INT stock_quantity
        BOOLEAN active
    }

    ORDERS {
        BIGINT id PK
        VARCHAR order_number UK
        BIGINT user_id
        VARCHAR status
        DECIMAL total_amount
        DATETIME created_at
        DATETIME updated_at
    }

    ORDER_ITEMS {
        BIGINT id PK
        BIGINT product_id
        VARCHAR product_name
        DECIMAL unit_price
        INT quantity
        DECIMAL subtotal
        BIGINT order_id FK
    }

    PAYMENTS {
        BIGINT id PK
        VARCHAR payment_key UK
        BIGINT order_id
        VARCHAR order_number
        BIGINT user_id
        VARCHAR order_status_snapshot
        DECIMAL amount
        DECIMAL order_amount_snapshot
        VARCHAR product_snapshot
        VARCHAR method
        VARCHAR status
        DATETIME requested_at
        DATETIME approved_at NULL
        DATETIME cancelled_at NULL
        VARCHAR failure_reason NULL
    }
```

## 서비스별 테이블 정리

### 1. user_service_db

#### `users`
- `id` PK, auto increment
- `name` NOT NULL
- `email` NOT NULL, UNIQUE

### 2. product_service_db

#### `products`
- `id` PK, auto increment
- `sku` NOT NULL, UNIQUE
- `name` NOT NULL
- `description` NULL
- `price` NOT NULL, DECIMAL(12,2)
- `stock_quantity` NOT NULL
- `active` NOT NULL

### 3. order_service_db

#### `orders`
- `id` PK, auto increment
- `order_number` NOT NULL, UNIQUE, length 50
- `user_id` NOT NULL
- `status` NOT NULL, ENUM string 저장
  - `CREATED`
  - `PAYMENT_PENDING`
  - `PAID`
  - `FAILED`
  - `CANCELLED`
- `total_amount` NOT NULL, DECIMAL(12,2)
- `created_at` NOT NULL
- `updated_at` NOT NULL

#### `order_items`
- `id` PK, auto increment
- `product_id` NOT NULL
- `product_name` NOT NULL, length 100
- `unit_price` NOT NULL, DECIMAL(12,2)
- `quantity` NOT NULL
- `subtotal` NOT NULL, DECIMAL(12,2)
- `order_id` NOT NULL, FK -> `orders.id`

관계:
- `orders` 1 : N `order_items`

### 4. payment_service_db

#### `payments`
- `id` PK, auto increment
- `payment_key` NOT NULL, UNIQUE, length 50
- `order_id` NOT NULL
- `order_number` NOT NULL, length 50
- `user_id` NOT NULL
- `order_status_snapshot` NOT NULL, length 30
- `amount` NOT NULL, DECIMAL(12,2)
- `order_amount_snapshot` NOT NULL, DECIMAL(12,2)
- `product_snapshot` NOT NULL, length 1000
- `method` NOT NULL, ENUM string 저장
  - `CARD`
  - `BANK_TRANSFER`
  - `SIMPLE_PAY`
- `status` NOT NULL, ENUM string 저장
  - `REQUESTED`
  - `APPROVED`
  - `DECLINED`
  - `CANCELLED`
  - `REFUNDED`
- `requested_at` NOT NULL
- `approved_at` NULL
- `cancelled_at` NULL
- `failure_reason` NULL, length 255

## 중요한 해석 포인트

이 구조는 "공유 DB의 거대한 ERD"가 아니라 "서비스별 로컬 DB + 서비스 간 ID 참조" 구조다.

- 물리 FK가 있는 관계
  - `order_items.order_id -> orders.id`
- 물리 FK가 없는 논리 참조
  - `orders.user_id` -> user-service의 `users.id`
  - `order_items.product_id` -> product-service의 `products.id`
  - `payments.order_id`, `payments.order_number` -> order-service의 `orders`
  - `payments.user_id` -> user-service의 `users.id`

즉, 주문/결제 서비스는 사용자/상품 정보를 조인하지 않고 ID 또는 스냅샷 값으로 참조한다.

## 근거 파일

- `user-service` 엔터티: `services/user-service/src/main/java/com/skala/springbootsample/domain`
- `product-service` 엔터티: `services/product-service/src/main/java/com/skala/productservice/domain`
- `order-service` 엔터티: `services/order-service/src/main/java/com/skala/orderservice/domain`
- `payment-service` 엔터티: `services/payment-service/src/main/java/com/skala/paymentservice/domain`
- 서비스별 DB 생성 SQL: `infra/databases/mariadb/local-init/001-create-service-schemas.sql`
