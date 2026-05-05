# connectors

Kafka Connect 또는 Debezium에 등록할 커넥터 정의 파일을 보관하는 디렉토리입니다.

## 포함 파일

- `order-outbox-source.json`: 주문 Outbox 테이블을 CDC 대상으로 읽는 소스 커넥터 설정

## 역할

데이터베이스 변경 이벤트를 Kafka 토픽으로 전달하는 출발점 설정을 담당합니다.
