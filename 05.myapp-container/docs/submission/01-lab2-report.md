# Lab 2 제출 보고서

## 1. 실습 개요

실습 2는 기존 쇼핑몰 MSA 프로젝트를 `CDC(Change Data Capture)` 기반 데이터 동기화와 `Outbox` 기반 `EDA(Event-Driven Architecture)` 구조로 확장하는 것이다.

기존 프로젝트에는 `user-service`, `product-service`, `order-service`, `payment-service` 서비스별로 독립된 MariaDB 스키마를 사용하는 구조가 이미 구현되어 있다.

이번 실습 보고서에서는 "주문 생성 시나리오에 대해 Outbox + CDC + Kafka + Consumer 흐름을 실제로 검증한 과정"을 작성한다.

사용자 시나리오는 다음과 같다.

- 주문 생성 시 비즈니스 데이터와 이벤트를 함께 기록한다.
- DB 변경 내용을 CDC로 감지한다.
- Kafka topic 으로 이벤트를 전달한다.
- 별도 consumer가 이벤트를 수신한다.


## 2. Outbox 패턴과 CDC 적용 이유

기존 `order-service`는 주문 생성 시 다음과 같은 동기 구조를 사용한다.

1. `user-service`에 사용자 존재 여부 확인
2. `product-service`에 상품 정보 조회
3. `product-service`에 재고 차감 요청
4. 주문 저장

이 구조는 이벤트 기반 확장 관점에서 다음과 같은 한계가 있다.

- 주문 처리와 후속 처리가 강하게 결합된다.
- 주문 저장과 이벤트 발행을 분리 처리하면 정합성 문제가 생길 수 있다.
- 대량 트래픽 상황에서 후속 작업이 늘수록 응답 지연과 장애 전파 가능성이 커진다.
- 향후 알림, 통계, 후속 결제 처리 같은 기능을 추가할 때 주문 서비스 수정 범위가 커질 수 있다.

이 문제를 줄이기 위해 이번 실습에서는 `Outbox 패턴`과 `CDC`를 적용한다.

## 3. 대표 사용자 시나리오

이번 실습에서 선택하여 구현한 대표 시나리오는 `주문 생성`이다.

사용자 시나리오는 다음과 같다.

> 1. 사용자가 상품을 선택하고 주문 생성 API를 호출한다.
> 2. `order-service`는 사용자와 상품을 검증하고 재고를 차감한다.
> 3. 주문 데이터를 `orders`, `order_items` 테이블에 저장한다.
> 4. 같은 트랜잭션 안에서 `outbox_event` 테이블에 `ORDER_CREATED` 이벤트를 저장한다.
> 5. Debezium Connector가 MariaDB binlog를 읽고 `outbox_event` 변경을 감지한다.
> 6. Debezium이 Kafka topic 으로 이벤트를 발행한다.
> 7. Consumer는 Kafka topic을 구독하여 이벤트를 수신하고 로그로 출력한다.

이 시나리오를 선택한 이유는 다음과 같다.

- 주문 생성은 쇼핑몰 도메인에서 가장 대표적인 이벤트다.
- Outbox 저장, CDC, Kafka 발행, Consumer 수신까지 끝까지 확인하기 좋다.
- 기존 비즈니스 로직을 크게 깨지 않으면서 EDA의 핵심 패턴을 입증할 수 있다.

전체 시스템을 완전한 이벤트 기반으로 재작성한 것은 아니지만 후속 처리가 직접 호출 방식이 아니라 `이벤트 발행 -> 브로커 전달 -> 소비자 반응` 구조로 연결되므로, 주문 생성 시나리오 기준 EDA 패턴이 성립한다.

- 주문 서비스가 상태 변화를 이벤트로 기록한다.
- 이벤트는 Outbox 테이블에 저장되어 서비스 내부 상태 변화와 함께 관리된다.
- Debezium CDC가 DB 변경 내용을 읽어 Kafka로 전달한다.
- 별도 Consumer가 Kafka topic을 구독하고 이벤트를 소비한다.


## 4. 아키텍처 설계

### 4.1 구성 요소

이번 실습 2에서 사용한 주요 구성 요소는 아래와 같다.

- `order-service`
  - 주문 생성 API 처리
  - 주문 데이터 저장
  - Outbox 이벤트 저장
- MariaDB
  - `order_service_db` 포함 서비스별 DB 저장소
- `outbox_event` 테이블
  - 발행할 이벤트를 안전하게 저장하는 테이블
- Debezium Connector
  - MariaDB binlog를 읽어 CDC 수행
- Kafka
  - CDC 이벤트 전달 브로커
- `event-consumer`
  - Kafka topic 구독 및 로그 출력

### 4.2 데이터 흐름

아키텍처의 데이터 흐름은 아래와 같다.

1. 사용자가 `POST /api/orders` 호출
2. `order-service`가 주문을 생성
3. 같은 트랜잭션에서 `outbox_event` row 저장
4. Debezium이 `outbox_event` 변경 감지
5. Kafka topic `orderdb.order_service_db.outbox_event` 생성 및 메시지 발행
6. `event-consumer` 가 해당 메시지를 수신

### 4.3 역할 분리

EDA 관점에서 역할을 구분하면 다음과 같다.

- Producer: `order-service`
- Event Store: `outbox_event`
- Event Propagation: Debezium Connector
- Event Broker: Kafka
- Consumer: `event-consumer`

### 4.4 설계 의도

설계의 목적은 `주문 저장`과 `이벤트 발행`을 애플리케이션 코드에서 직접 동시에 처리하지 않고, 먼저 DB 내부의 Outbox에 안전하게 적재한 뒤 CDC가 이를 외부로 전달하는 것 이다.

해당 설계를 적용한다면 다음과 같은 장점이 있다.

- 주문 데이터와 이벤트 기록을 같은 트랜잭션으로 묶을 수 있다.
- Kafka 전송 성공/실패를 주문 서비스가 직접 관리하지 않아도 된다.
- 향후 consumer를 추가해도 주문 서비스 수정이 최소화된다.

## 5. 구현 범위

이번 실습에서 구현한 범위는 다음과 같다.

- `order-service`에 Outbox 엔티티 추가
- 주문 생성 시 Outbox row 저장
- Debezium source connector 등록
- Kafka topic 생성 확인
- Kafka topic 메시지 확인
- 별도 `event-consumer` 를 통한 메시지 소비 확인

## 6. 구현 내용

### 6.1 Order Service Outbox 추가

`order-service`에 아래 요소를 추가했다.

- `OutboxEvent` 엔티티
- `OutboxEventRepository`
- `OutboxEventService`
- 주문 생성 성공 후 `ORDER_CREATED` 이벤트를 저장하도록 `OrderService` 수정

Outbox payload에는 다음 정보가 JSON으로 저장되도록 했다.

- `orderId`
- `orderNumber`
- `userId`
- `status`
- `totalAmount`
- `createdAt`
- 주문 상품 목록

### 6.2 로컬 실습 2 실행 환경 구성

기존 `docker-compose.yml` 은 MariaDB와 기본 서비스만 포함하고 있었다.
이번 실습을 위해 별도 확장 파일 `docker-compose.lab2.yml` 을 추가하여 아래 요소를 보강했다.

- Zookeeper
- Kafka
- Kafka Connect (Debezium)
- Kafka UI
- `event-consumer`

이 방식을 선택한 이유는 기존 서비스 환경을 크게 흔들지 않고, 실습 2 검증 스택만 분리해서 추가할 수 있기 때문이다.

### 6.3 CDC 사전 조건 설정

Debezium 이 정상 동작하려면 MariaDB binlog 설정과 사용자 권한이 필요했다.

필요 조건:

- `log_bin = ON`
- `binlog_format = ROW`
- `binlog_row_image = FULL`

또한 connector에서 사용하는 `cloud` 계정에 아래 권한을 추가했다.

- `RELOAD`
- `REPLICATION SLAVE`
- `REPLICATION CLIENT`
- `LOCK TABLES`

### 6.4 Debezium Connector 구성

Connector는 `order_service_db.outbox_event` 테이블만 CDC 대상으로 잡는
가장 단순한 source connector 방식으로 시작했다.

주요 설정은 다음과 같다.

- connector class: `io.debezium.connector.mysql.MySqlConnector`
- database: `order_service_db`
- table include: `order_service_db.outbox_event`
- topic prefix: `orderdb`

이 설정으로 생성되는 topic 이름은:

- `orderdb.order_service_db.outbox_event`

## 7. 검증 과정

### 7.1 서비스 기동 확인

로컬 검증 환경 기동 후 다음 컨테이너가 정상 실행되는 것을 확인했다.

- `mariadb`
- `order-service`
- `kafka`
- `kafka-connect`
- `kafka-ui`
- `event-consumer`

또한 `order-service` health endpoint 에서 `UP` 상태를 확인했다.

실행 상태 예시는 아래와 같다.

```text
NAME                                  IMAGE                                  COMMAND                   SERVICE           CREATED          STATUS                    PORTS
05myapp-container-kafka-1             confluentinc/cp-kafka:7.6.1            "/etc/confluent/dock…"   kafka             12 seconds ago   Up 12 seconds             0.0.0.0:19092->9092/tcp
05myapp-container-kafka-connect-1     quay.io/debezium/connect:2.7.3.Final   "/docker-entrypoint.…"   kafka-connect     12 seconds ago   Up 12 seconds             0.0.0.0:18093->8083/tcp
05myapp-container-kafka-ui-1          provectuslabs/kafka-ui:latest          "/bin/sh -c 'java --…"   kafka-ui          12 seconds ago   Up 12 seconds             0.0.0.0:18085->8080/tcp
05myapp-container-mariadb-1           mariadb:11.4                           "docker-entrypoint.s…"   mariadb           13 seconds ago   Up 12 seconds (healthy)   0.0.0.0:13306->3306/tcp
05myapp-container-order-service-1     local/order-service:compose            "sh -c 'exec java $J…"   order-service     12 seconds ago   Up 6 seconds              0.0.0.0:18083->8080/tcp
05myapp-container-payment-service-1   local/payment-service:compose          "sh -c 'exec java $J…"   payment-service   12 seconds ago   Up 6 seconds              0.0.0.0:18084->8080/tcp
05myapp-container-product-service-1   local/product-service:compose          "sh -c 'exec java $J…"   product-service   12 seconds ago   Up 6 seconds              0.0.0.0:18082->8080/tcp
05myapp-container-user-service-1      local/user-service:compose             "sh -c 'exec java $J…"   user-service      12 seconds ago   Up 6 seconds              0.0.0.0:18081->8080/tcp
05myapp-container-zookeeper-1         confluentinc/cp-zookeeper:7.6.1        "/etc/confluent/dock…"   zookeeper         13 seconds ago   Up 12 seconds             0.0.0.0:12181->2181/tcp
```

```json
{"status":"UP","groups":["liveness","readiness"],"components":{"db":{"status":"UP","details":{"database":"MariaDB","validationQuery":"isValid()"}},"diskSpace":{"status":"UP","details":{"total":485421555712,"free":430127435776,"threshold":10485760,"path":"/app/.","exists":true}},"livenessState":{"status":"UP"},"myService":{"status":"UP","details":{"service":"External API","responseTime":"588ms","url":"https://jsonplaceholder.typicode.com"}},"ping":{"status":"UP"},"readinessState":{"status":"UP"},"ssl":{"status":"UP","details":{"validChains":[],"invalidChains":[]}}}}
```

### 7.2 주문 생성 API 확인

주문 생성 API를 호출하여 주문이 정상적으로 저장되는지 확인했다.

응답 예시:

```json
{
  "id": 6,
  "orderNumber": "ORD-536C3786",
  "userId": 1,
  "status": "PAYMENT_PENDING",
  "totalAmount": 19000.00,
  "createdAt": "2026-04-21T17:14:43.657828385",
  "updatedAt": "2026-04-21T17:14:43.657828385",
  "items": [
    {
      "productId": 1,
      "productName": "텀블러",
      "unitPrice": 19000.00,
      "quantity": 1,
      "subtotal": 19000.00
    }
  ]
}
```

이는 Outbox 추가 후에도 기존 주문 생성 기능이 깨지지 않았다는 것을 의미한다.

### 7.3 Outbox 이벤트 저장 확인

MariaDB에서 `outbox_event` 테이블을 조회한 결과,
`ORDER_CREATED` 이벤트 row가 실제로 저장되는 것을 확인했다.

예시:

```text
id                                      aggregate_type  aggregate_id  event_type     created_at
75398b05-6b48-4c79-9276-a32ab2c51de0    order           5             ORDER_CREATED  2026-04-21 17:03:12.707020
83c7a63f-5cad-4821-b2d7-fb91633dce4c    order           4             ORDER_CREATED  2026-04-21 16:38:52.468026
```

또한 payload preview 에 주문 정보 JSON이 포함되어 있는 것도 확인했다.

```text
aggregate_id  event_type      payload_preview
4             ORDER_CREATED   {"orderId":4,"orderNumber":"ORD-695DAAF1","userId":1,"status":"PAYMENT_PENDING","totalAmount":"19000.00","createdAt":"2026-04-21T16:38:52.441314458","items":[{"productId":1,"productName":"텀블러","quantity":1,"unitPrice":"19000.00","subtotal":"19000.00"}]}
```

### 7.4 Connector 실행 확인

Kafka Connect 에 Debezium source connector 를 등록한 뒤,
connector 와 task 가 모두 `RUNNING` 상태임을 확인했다.

예시:

```json
{
  "name": "order-outbox-source",
  "connector": {
    "state": "RUNNING",
    "worker_id": "172.26.0.6:8083"
  },
  "tasks": [
    {
      "id": 0,
      "state": "RUNNING",
      "worker_id": "172.26.0.6:8083"
    }
  ],
  "type": "source"
}
```

### 7.5 Topic 생성 및 메시지 확인

Kafka topic 목록에서 다음 topic 이 생성된 것을 확인했다.

```text
__consumer_offsets
connect_configs
connect_offsets
connect_statuses
orderdb.order_service_db.outbox_event
schemahistory.orderdb
```

이후 `kafka-console-consumer` 로 topic 메시지를 읽어, `ORDER_CREATED` CDC 메시지가 Kafka에 적재되는 것을 확인했다.

대표 메시지 예시는 아래와 같다.

```json
{
  "payload": {
    "before": null,
    "after": {
      "id": "75398b05-6b48-4c79-9276-a32ab2c51de0",
      "aggregate_id": "5",
      "aggregate_type": "order",
      "event_type": "ORDER_CREATED",
      "payload": "{\"orderId\":5,\"orderNumber\":\"ORD-CA02461F\",\"userId\":1,\"status\":\"PAYMENT_PENDING\",\"totalAmount\":\"19000.00\",\"createdAt\":\"2026-04-21T17:03:12.673827801\",\"items\":[{\"productId\":1,\"productName\":\"텀블러\",\"quantity\":1,\"unitPrice\":\"19000.00\",\"subtotal\":\"19000.00\"}]}"
    },
    "source": {
      "db": "order_service_db",
      "table": "outbox_event",
      "file": "mysql-bin.000001"
    },
    "op": "r"
  }
}
```

Kafka 메시지에는 `aggregate_id`, `aggregate_type`, `event_type`, `payload` 와 함께 Debezium source metadata 가 포함되어 있었다.

### 7.6 Consumer 수신 확인

별도 `event-consumer` 서비스에서 topic 을 구독하고 로그를 출력하도록 구현했다.

확인 결과:

- 기존 snapshot 이벤트 (`op = r`) 수신
- 신규 주문 생성 이벤트 (`op = c`) 수신

예를 들어 신규 주문의 경우 다음과 같은 로그가 확인되었다. 이를 통해 이벤트 발행뿐 아니라 소비까지 실제로 검증했다.

```json
[event-consumer] bootstrap=kafka:29092, topic=orderdb.order_service_db.outbox_event, group=order-outbox-consumer
[event-consumer] connected
[event-consumer] message received
{"topic":"orderdb.order_service_db.outbox_event","partition":0,"offset":2,"value":{"payload":{"after":{"aggregate_id":"6","aggregate_type":"order","event_type":"ORDER_CREATED","payload":"{\"orderId\":6,\"orderNumber\":\"ORD-536C3786\",\"userId\":1,\"status\":\"PAYMENT_PENDING\",\"totalAmount\":\"19000.00\",\"createdAt\":\"2026-04-21T17:14:43.657828385\",\"items\":[{\"productId\":1,\"productName\":\"텀블러\",\"quantity\":1,\"unitPrice\":\"19000.00\",\"subtotal\":\"19000.00\"}]}"}}}}
[event-consumer] message received
{"topic":"orderdb.order_service_db.outbox_event","partition":0,"offset":3,"value":{"payload":{"after":{"aggregate_id":"7","aggregate_type":"order","event_type":"ORDER_CREATED","payload":"{\"orderId\":7,\"orderNumber\":\"ORD-FA31DC0A\",\"userId\":1,\"status\":\"PAYMENT_PENDING\",\"totalAmount\":\"19000.00\",\"createdAt\":\"2026-04-21T17:17:48.935629471\",\"items\":[{\"productId\":1,\"productName\":\"텀블러\",\"quantity\":1,\"unitPrice\":\"19000.00\",\"subtotal\":\"19000.00\"}]}"}}}}
```

## 8. 트러블슈팅

실습 2를 진행하면서 발생한 주요 트러블 슈팅과 수정 내용은 다음과 같다.

| 구간 | 문제 증상 | 원인 | 수정한 부분 | 수정 내용 | 결과 |
| --- | --- | --- | --- | --- | --- |
| Outbox 저장 | 주문 생성 API 호출 시 `Data too long for column 'payload'` 오류 발생 | `outbox_event.payload` 컬럼이 긴 JSON 문자열을 저장하기에 부족한 타입으로 생성됨 | `order-service` 의 `OutboxEvent` 엔티티 | `payload` 컬럼 정의를 `LONGTEXT` 로 변경 | 주문 생성 API가 다시 정상 응답했고, Outbox 이벤트 저장까지 성공했다. |
| Kafka Connect 기동 | `order-service` 와 `kafka-connect` 가 같은 포트를 사용하여 충돌 가능성 발생 | 기존 compose 에서 `order-service` 가 `18083` 을 사용 중이었고, lab2 compose 에서 `kafka-connect` 도 같은 포트를 사용하려고 함 | `docker-compose.lab2.yml` | `kafka-connect` 외부 포트를 `18093` 으로 변경 | `order-service` 와 `kafka-connect` 를 동시에 안정적으로 기동할 수 있게 되었다. |
| Debezium 이미지 | compose 기동 시 `debezium/connect:2.7` 이미지를 찾지 못해 실패 | 잘못된 이미지 태그 사용 | `docker-compose.lab2.yml` | `quay.io/debezium/connect:2.7.3.Final` 로 교체 | Kafka Connect / Debezium 컨테이너가 정상 실행되었다. |
| CDC 사전 조건 | Debezium connector 등록 후 topic 이 생성되지 않음 | MariaDB 에서 binlog 가 꺼져 있고 format 이 CDC 친화적으로 설정되지 않음 | `docker-compose.lab2.yml` 의 MariaDB 실행 옵션 | `--log-bin=mysql-bin`, `--binlog-format=ROW`, `--binlog-row-image=FULL`, `--server-id=223344` 추가 | `log_bin = ON`, `binlog_format = ROW`, `binlog_row_image = FULL` 을 확인했고 CDC 수행이 가능해졌다. |
| Debezium snapshot 권한 | connector 는 `RUNNING` 이지만 실제 snapshot 단계에서 계속 재시도 | Debezium 계정에 `RELOAD`, replication 관련 권한이 부족함 | MariaDB 권한 설정 및 초기화 SQL | `cloud` 계정에 `RELOAD`, `REPLICATION SLAVE`, `REPLICATION CLIENT`, `LOCK TABLES` 권한 추가 | connector 가 실제로 `outbox_event` 를 읽어 Kafka topic 을 생성하고 메시지를 적재했다. |
| Consumer 동작 | 초기 `event-consumer` 로그가 `[connected]` 만 반복되고 메시지 수신 로그가 보이지 않음 | consumer 루프가 연결을 반복 생성하는 형태라 안정적으로 polling 하지 못함 | `services/event-consumer/consumer.py` | `for message in consumer` 방식 대신 `poll(timeout_ms=3000)` 기반 루프로 수정 | snapshot 이벤트와 신규 생성 이벤트(`op = c`) 모두 정상 수신하는 것을 확인했다. |

## 9. 최종 결과

- 주문 생성 API가 기존처럼 정상 동작 확인 완료
- 주문 생성 시 `outbox_event` 에 `ORDER_CREATED` 이벤트가 저장
- Debezium Connector 가 이를 CDC 로 감지
- Kafka topic 으로 메시지가 발행
- 별도 consumer 가 이를 수신

`주문 생성 시나리오에 대한 EDA MVP` 를 성공적으로 구현하고 검증 완료했다.
