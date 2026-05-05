# Lab 2 Results And Evidence

## 구현 결과 요약

이번 실습 2에서는 `order-service` 에 Outbox 패턴을 적용하고,
Debezium Connector 를 통해 `outbox_event` 테이블 변경 내용을 Kafka topic 으로 전달하는
최소 MVP 흐름을 구현했다.

최종적으로 확인한 범위는 아래와 같다.

- 주문 생성 API 호출 성공
- `order_service_db.outbox_event` 테이블 생성 및 이벤트 row 저장 성공
- Kafka Connect / Debezium source connector 등록 성공
- Kafka topic `orderdb.order_service_db.outbox_event` 생성 성공
- Kafka topic 에서 `ORDER_CREATED` CDC 메시지 확인 성공

이번 구현은 "주문 생성 이벤트를 안전하게 기록하고 Kafka 까지 전달하는 흐름"에 집중했다.
후속 consumer 의 실제 비즈니스 반영은 아직 최소 범위에 포함하지 않았다.

## 검증 항목

- 주문 생성 API 호출 성공
- Outbox 테이블 insert 확인
- Connector Running 확인
- Kafka 토픽 메시지 확인
- Consumer 로그 확인

## 실행 증빙

### 1. 로컬 CDC 검증 환경 기동 성공

기동 후 확인 결과:

```text
app-kafka-1             Up 12 seconds             0.0.0.0:19092->9092/tcp
app-kafka-connect-1     Up 12 seconds             0.0.0.0:18093->8083/tcp
app-kafka-ui-1          Up 12 seconds             0.0.0.0:18085->8080/tcp
app-mariadb-1           Up 12 seconds (healthy)   0.0.0.0:13306->3306/tcp
app-order-service-1     Up 6 seconds              0.0.0.0:18083->8080/tcp
```

판단:

- 실습 2용 로컬 검증 스택이 정상 기동되었다.
- 기존 서비스와 Kafka / Connect / Debezium 검증 환경을 함께 사용할 수 있었다.

### 2. Order Service 정상 동작 확인

사용한 명령:

```bash
curl -s http://localhost:18083/actuator/health
```

확인 결과:

```json
{"status":"UP","groups":["liveness","readiness"],"components":{"db":{"status":"UP"}}}
```

판단:

- `order-service` 는 Outbox 추가 이후에도 정상 기동 상태를 유지했다.

### 3. 주문 생성 API 성공 확인

사용한 명령:

```bash
curl -s -X POST http://localhost:18083/api/orders \
  -H 'Content-Type: application/json' \
  -d '{
    "userId": 1,
    "items": [
      { "productId": 1, "quantity": 1 }
    ]
  }'
```

확인 결과:

```json
{"id":5,"orderNumber":"ORD-CA02461F","userId":1,"status":"PAYMENT_PENDING","totalAmount":19000.00,"createdAt":"2026-04-21T17:03:12.673827801","updatedAt":"2026-04-21T17:03:12.673827801","items":[{"productId":1,"productName":"텀블러","unitPrice":19000.00,"quantity":1,"subtotal":19000.00}]}
```

판단:

- 기존 주문 생성 기능은 깨지지 않았고, Outbox 추가 후에도 정상적으로 주문이 저장되었다.

### 4. Outbox 테이블 및 이벤트 저장 확인

사용한 명령:

```bash
docker exec app-mariadb-1 mariadb -ucloud -p'Skala25a!23$' order_service_db -e "select id, aggregate_type, aggregate_id, event_type, created_at from outbox_event order by created_at desc;"
docker exec app-mariadb-1 mariadb -ucloud -p'Skala25a!23$' order_service_db -e "select aggregate_id, event_type, left(payload, 300) as payload_preview from outbox_event order by created_at desc;"
```

확인 결과:

```text
id                                      aggregate_type  aggregate_id  event_type     created_at
83c7a63f-5cad-4821-b2d7-fb91633dce4c    order           4             ORDER_CREATED  2026-04-21 16:38:52.468026
75398b05-6b48-4c79-9276-a32ab2c51de0    order           5             ORDER_CREATED  2026-04-21 17:03:12.707020
```

payload preview:

```text
aggregate_id  event_type      payload_preview
4             ORDER_CREATED   {"orderId":4,"orderNumber":"ORD-695DAAF1","userId":1,"status":"PAYMENT_PENDING",...}
5             ORDER_CREATED   {"orderId":5,"orderNumber":"ORD-CA02461F","userId":1,"status":"PAYMENT_PENDING",...}
```

판단:

- 주문 생성 시 같은 트랜잭션 범위에서 `outbox_event` row 가 저장되는 것을 확인했다.
- `payload` 안에 주문 상세 JSON 이 포함되어 이후 Kafka 전달의 원본 이벤트로 사용 가능했다.

### 5. Debezium Connector 등록 및 실행 확인

사용한 명령:

```bash
bash cdc/scripts/register-order-outbox-connector.sh
bash cdc/scripts/check-connectors.sh
curl -s http://localhost:18093/connectors/order-outbox-source/status
```

확인 결과:

```json
{"name":"order-outbox-source","connector":{"state":"RUNNING","worker_id":"172.26.0.6:8083"},"tasks":[{"id":0,"state":"RUNNING","worker_id":"172.26.0.6:8083"}],"type":"source"}
```

판단:

- Kafka Connect 에 Debezium source connector 가 정상 등록되었고,
  connector 와 task 모두 `RUNNING` 상태임을 확인했다.

### 6. CDC 환경 조건 검증

사용한 명령:

```bash
docker exec app-mariadb-1 mariadb -uroot -prootpass -e "show variables like 'log_bin';"
docker exec app-mariadb-1 mariadb -uroot -prootpass -e "show variables like 'binlog_format';"
docker exec app-mariadb-1 mariadb -uroot -prootpass -e "show variables like 'binlog_row_image';"
docker exec app-mariadb-1 mariadb -uroot -prootpass -e "show grants for 'cloud'@'%';"
```

확인 결과:

```text
log_bin           ON
binlog_format     ROW
binlog_row_image  FULL
```

권한 확인 결과:

```text
GRANT RELOAD, LOCK TABLES, REPLICATION SLAVE, BINLOG MONITOR ON *.* TO `cloud`@`%`
GRANT ALL PRIVILEGES ON `order_service_db`.* TO `cloud`@`%`
```

판단:

- Debezium CDC 동작에 필요한 MariaDB binlog 설정과 계정 권한을 맞춘 뒤 connector 를 정상 구동할 수 있었다.

### 7. Kafka Topic 생성 및 메시지 확인

사용한 명령:

```bash
docker exec app-kafka-1 kafka-topics --bootstrap-server kafka:29092 --list
docker exec app-kafka-1 kafka-console-consumer \
  --bootstrap-server kafka:29092 \
  --topic orderdb.order_service_db.outbox_event \
  --from-beginning \
  --max-messages 10
```

topic 목록 확인 결과:

```text
__consumer_offsets
connect_configs
connect_offsets
connect_statuses
orderdb.order_service_db.outbox_event
schemahistory.orderdb
```

메시지 확인 결과 요약:

```json
{
  "payload": {
    "after": {
      "aggregate_id": "5",
      "aggregate_type": "order",
      "event_type": "ORDER_CREATED",
      "payload": "{\"orderId\":5,\"orderNumber\":\"ORD-CA02461F\",...}"
    },
    "source": {
      "db": "order_service_db",
      "table": "outbox_event"
    },
    "op": "r"
  }
}
```

판단:

- Debezium 이 `outbox_event` 테이블의 데이터를 Kafka topic 으로 전달하는 것을 확인했다.
- `ORDER_CREATED` 이벤트가 실제 메시지로 발행되었음을 검증했다.

### 8. Consumer 구현 상태

구현 방식:

- 별도 실습용 `event-consumer` 서비스를 추가했다.
- 이 consumer 는 `orderdb.order_service_db.outbox_event` topic 을 구독하고,
  수신한 메시지를 로그로 출력하는 최소 consumer 다.

실행 확인 결과:

```text
[event-consumer] bootstrap=kafka:29092, topic=orderdb.order_service_db.outbox_event, group=order-outbox-consumer
[event-consumer] connected
[event-consumer] message received
{"topic": "orderdb.order_service_db.outbox_event", "partition": 0, "offset": 2, "value": {"payload": {"after": {"aggregate_id": "6", "aggregate_type": "order", "event_type": "ORDER_CREATED"}}}}
[event-consumer] message received
{"topic": "orderdb.order_service_db.outbox_event", "partition": 0, "offset": 3, "value": {"payload": {"after": {"aggregate_id": "7", "aggregate_type": "order", "event_type": "ORDER_CREATED"}}}}
```

판단:

- 이벤트 "발행", "Kafka 적재", "consumer 수신" 흐름까지 실제 로그로 검증했다.
- snapshot 이벤트(`op=r`)와 신규 생성 이벤트(`op=c`)가 모두 확인되어,
  초기 적재와 이후 변경 이벤트가 함께 전달되는 것도 볼 수 있었다.

## 이슈와 해결

### 1. Outbox payload 컬럼 길이 문제

문제:

- 주문 생성 시 `payload` 컬럼이 너무 짧아 `Data too long for column 'payload'` 오류가 발생했다.

해결:

- `payload` 컬럼을 `LONGTEXT` 로 지정하도록 엔티티를 수정했다.

### 2. Kafka Connect 포트 충돌

문제:

- 초기 설정에서 `order-service` 와 `kafka-connect` 모두 `18083` 포트를 사용하려고 했다.

해결:

- `kafka-connect` 포트를 `18093` 으로 변경했다.

### 3. Debezium 이미지 태그 문제

문제:

- `debezium/connect:2.7` 이미지 태그가 존재하지 않아 compose 기동에 실패했다.

해결:

- `quay.io/debezium/connect:2.7.3.Final` 로 수정했다.

### 4. MariaDB binlog 비활성화

문제:

- 초기 로컬 MariaDB 설정에서 `log_bin = OFF`, `binlog_format = MIXED` 였다.

해결:

- 실습 2 전용 compose override 에 binlog 관련 옵션을 추가했다.

### 5. Debezium 권한 부족

문제:

- Connector 로그에서 `RELOAD privilege` 부족으로 snapshot 실패가 발생했다.

해결:

- `cloud` 계정에 `RELOAD`, `REPLICATION SLAVE`, `REPLICATION CLIENT`, `LOCK TABLES` 권한을 부여했다.

## 한계와 다음 단계

현재 한계:

- Kafka topic 메시지 수신과 별도 애플리케이션 consumer 로그까지 확인했지만,
  현재는 Debezium raw CDC envelope 를 그대로 소비하고 있다.
- 로컬 검증 환경 기준으로 먼저 성공 흐름을 확인했으며, Kubernetes 환경까지의 확장은 아직 남아 있다.
- Debezium raw CDC envelope 를 그대로 사용하고 있어, Outbox SMT 기반의 더 깔끔한 이벤트 포맷 변환은 아직 적용하지 않았다.

다음 단계:

1. 별도 consumer 를 추가해 `ORDER_CREATED` 이벤트 소비를 로그 또는 후속 처리로 연결
2. 필요 시 `payment-service` 를 consumer 역할로 확장
3. Kubernetes 환경에서도 Kafka / Connect / Debezium 구성을 준비해 동일 흐름 검증
4. raw CDC topic 대신 Outbox Event Router SMT 적용 검토
