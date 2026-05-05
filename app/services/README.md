# services

백엔드 서비스 소스와 이벤트 소비자 코드를 모아둔 디렉토리입니다.

## 하위 디렉토리

- `user-service`: 사용자/지역 조회 중심의 기준 서비스
- `product-service`: 상품 도메인 서비스
- `order-service`: 주문 생성과 Outbox 이벤트 발행 서비스
- `payment-service`: 결제 처리 서비스
- `event-consumer`: Kafka 이벤트를 소비하는 Python 소비자

## 읽는 방법

기준 구조를 이해하려면 `user-service`를 먼저 보고, 그 다음 `order-service`와 `event-consumer`를 보면 이벤트 연동 흐름을 파악하기 쉽습니다.
