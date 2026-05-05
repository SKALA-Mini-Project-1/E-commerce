# CDC Workspace

이 디렉터리는 CDC 관련 설정을 모아두는 위치다.

권장 구성:

- `connectors/`
  Debezium connector 등록 JSON 파일
- `scripts/`
  connector 등록, 상태 확인, topic 확인 스크립트

처음에는 최소한 아래 파일부터 추가하면 된다.

- `connectors/order-outbox-source.json`
- `scripts/register-order-outbox-connector.sh`
- `scripts/check-connectors.sh`

## 권장 실행 방식

기존 서비스 compose 를 직접 수정하지 않고, CDC 전용 확장 compose 를 겹쳐서 실행한다.

기본 원칙:

- `docker-compose.yml`
  주문/결제/상품/사용자 서비스와 MariaDB 기본 구동
- `docker-compose.lab2.yml`
  Kafka, Kafka Connect, Debezium 검증용 스택 추가

예시:

```bash
cd /Users/yangyewon/workspace/shopping-mall-k8s-lab/E-commerce/app
docker compose -f docker-compose.yml -f docker-compose.lab2.yml up -d --build
```

확인 포인트:

- MariaDB 기동 여부
- order-service API 동작 여부
- Kafka Connect REST API 응답 여부
- Kafka UI 접속 가능 여부
- event-consumer 로그 출력 여부

주의:

- 기본 서비스는 `build:` 가 있으므로 처음 실행 시 `--build` 를 함께 사용하는 편이 안전하다.
- Debezium Connect 이미지는 `quay.io/debezium/connect:2.7.3.Final` 기준으로 사용한다.

현재 포트 계획:

- Kafka host access: `localhost:19092`
- Kafka Connect REST: `localhost:18093`
- Kafka UI: `http://localhost:18085`

이후 순서:

1. Outbox 테이블/코드 추가
2. Connector JSON 작성
3. Connector 등록
4. 주문 생성 후 topic 메시지 확인
5. Consumer 확인

## 현재 첫 번째 connector 전략

첫 번째 검증은 Debezium Outbox SMT 같은 고급 변환 없이,
`order_service_db.outbox_event` 테이블 자체를 CDC 대상으로 잡는 단순한 source connector 방식으로 시작한다.

이 방식을 쓰는 이유:

- 초기 설정이 단순하다.
- 실패 지점을 줄일 수 있다.
- `order-service -> outbox_event -> Kafka topic` 흐름을 먼저 안정적으로 검증할 수 있다.

예상 topic:

- `orderdb.order_service_db.outbox_event`

## Connector 등록 전 확인

MariaDB binlog 가 켜져 있어야 Debezium 이 동작한다.

예시 확인 명령:

```bash
docker exec app-mariadb-1 mariadb -uroot -prootpass -e "show variables like 'log_bin';"
docker exec app-mariadb-1 mariadb -uroot -prootpass -e "show variables like 'binlog_format';"
```

권장 기대값:

- `log_bin = ON`
- `binlog_format = ROW`

만약 값이 다르면 `docker-compose.lab2.yml` 에서 MariaDB command override 로 CDC 옵션을 켜고,
MariaDB 컨테이너를 recreate 해야 한다.

현재 적용한 옵션:

- `--server-id=223344`
- `--log-bin=mysql-bin`
- `--binlog-format=ROW`
- `--binlog-row-image=FULL`

## Debezium 계정 권한

로컬 실습에서는 애플리케이션과 Debezium connector 가 모두 `cloud` 계정을 사용한다.
따라서 아래 권한이 필요하다.

- 서비스 DB들에 대한 권한
- `RELOAD`
- `REPLICATION SLAVE`
- `REPLICATION CLIENT`
- `LOCK TABLES`

초기화 SQL 에도 같은 권한을 반영해 두었다.

## 팀원 재현 방법

아래 순서대로 실행하면 팀원도 같은 로컬 환경을 재현할 수 있다.

### 1. 기본 스택 + CDC 확장 스택 기동

```bash
cd /Users/yangyewon/workspace/shopping-mall-k8s-lab/E-commerce/app
docker compose -f docker-compose.yml -f docker-compose.lab2.yml up -d --build
```

### 2. Kafka Connect 확인

```bash
curl -s http://localhost:18093/
curl -s http://localhost:18093/connectors
```

### 3. MariaDB CDC 설정 확인

```bash
docker exec app-mariadb-1 mariadb -uroot -prootpass -e "show variables like 'log_bin';"
docker exec app-mariadb-1 mariadb -uroot -prootpass -e "show variables like 'binlog_format';"
```

### 4. Connector 등록

```bash
cd /Users/yangyewon/workspace/shopping-mall-k8s-lab/E-commerce
bash cdc/scripts/register-order-outbox-connector.sh
bash cdc/scripts/check-connectors.sh
```

### 5. 주문 생성으로 이벤트 발생

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

### 6. Topic 및 consumer 확인

```bash
docker exec app-kafka-1 kafka-topics --bootstrap-server kafka:29092 --list
docker logs --tail 100 app-event-consumer-1
```

기대 결과:

- `orderdb.order_service_db.outbox_event` topic 존재
- `event-consumer` 로그에 CDC 메시지 출력
