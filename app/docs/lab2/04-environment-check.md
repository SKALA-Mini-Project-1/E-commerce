# Lab 2 Environment Check

## 점검 목적

실습 2는 애플리케이션 코드뿐 아니라 DB, CDC, Connector, Kafka, Consumer가 함께 맞물린다.
그래서 구현 전에 어떤 환경이 이미 준비되어 있고, 무엇을 새로 띄워야 하는지 먼저 확인해야 한다.

## 점검 항목

### 1. `order-service` 애플리케이션 구조

- JPA 기반 서비스인지
- 주문 생성 메서드에 트랜잭션이 적용되어 있는지
- 주문 저장 직후 Outbox 저장을 추가하기 쉬운 구조인지

현재 확인 결과:

- `order-service`는 Spring Boot + JPA 기반이다.
- `create()` 메서드에 `@Transactional` 이 적용되어 있다.
- 주문 저장 로직은 `customerOrderRepository.save(order)` 기반이라 Outbox 저장 추가가 비교적 쉽다.

### 2. 주문 저장소와 DB 확인

- 주문 데이터가 어떤 DB를 사용하는지
- CDC 대상으로 사용할 수 있는 DB인지
- Outbox 테이블을 추가해도 기존 구조를 크게 깨지 않는지

현재 판단:

- MariaDB 기반 구성이 이미 존재한다.
- Outbox 테이블을 같은 DB에 추가하는 방향이 가장 단순하고 안정적이다.
- 로컬 `docker-compose.yml` 은 `mariadb` 와 서비스별 개별 스키마를 사용하는 구조다.
- `001-create-service-schemas.sql` 에서 `order_service_db` 가 미리 생성되므로,
  로컬 검증 시 같은 DB 안에 `outbox_event` 테이블을 추가하는 방식이 적합하다.

### 3. Kafka / Connect / Debezium 준비 상태

확인할 항목:

- Kafka broker 존재 여부
- Kafka Connect 존재 여부
- Debezium connector 등록 가능 여부
- connector 상태 확인 API 사용 가능 여부

현재 상태:

- 클러스터에는 `kafka1` 네임스페이스에 Kafka/Connect 관련 리소스 흔적이 존재한다.
- `debezium-source-connect-connect-api`, `jdbc-sink-connect-connect-api`,
  `my-kafka-cluster-kafka-bootstrap` 같은 서비스가 보인다.
- 그러나 `strimzi-cluster-operator` 는 `Terminating` 상태였고,
  관련 deployment/statefulset 도 불안정한 흔적이 있다.
- 따라서 `kafka1` 은 "참고 가능한 환경"이지, 바로 의존하기 좋은 안정 환경으로 보긴 어렵다.

### 4. Kubernetes 영향 범위

확인할 항목:

- 실습 1 안정화된 `ecommerce` 환경을 바로 흔들어야 하는지
- 별도 환경 또는 로컬 검증이 가능한지

현재 판단:

- `ecommerce` 는 실습 1 안정 상태를 유지하는 편이 좋다.
- 실습 2 초기 검증은 로컬 `docker-compose` 기반 또는 별도 테스트 네임스페이스에서 진행하는 편이 안전하다.
- `kafka1` 네임스페이스는 삭제 중이거나 불안정한 실험 잔재로 보고 직접 의존하지 않는 것이 낫다.

## 현재 결론

- `order-service`는 Outbox를 붙이기 좋은 구조다.
- 대표 시나리오는 주문 생성 이벤트로 확정해도 무리가 없다.
- 로컬 `docker-compose.yml` 은 실습 2 검증의 시작점으로 사용하기 적합하다.
- 다만 현재 compose 에는 Kafka/Connect/Debezium 이 포함되어 있지 않으므로,
  실습 2용 보강이 필요하다.
- 구현 전 Kafka/Connect/Debezium 환경 확인이 필수다.
- 제출 안정성을 위해 본 운영성 환경으로 바로 진입하지 않는 전략이 적절하다.

## 다음 확인 예정 항목

- 실습 2용 로컬 Kafka/Connect/Debezium 추가 방안
- Connector 등록 JSON 구조
- Topic 생성 및 조회 방법
- Consumer를 어떤 방식으로 둘지

## 추가 확인 결과

- 로컬 MariaDB 기본 설정에서는 `log_bin = OFF`, `binlog_format = MIXED` 였다.
- 이 상태에서는 Debezium CDC connector 가 동작할 수 없다.
- 따라서 실습 2용 compose override 에 MariaDB binlog 옵션을 추가해 CDC 가능 상태로 바꾸는 작업이 필요하다.
- Connector 등록 후에는 `cloud` 계정에 Debezium snapshot/replication 권한이 부족해서 topic 생성이 진행되지 않았다.
- 필요한 권한은 `RELOAD`, `REPLICATION SLAVE`, `REPLICATION CLIENT`, `LOCK TABLES` 이다.
