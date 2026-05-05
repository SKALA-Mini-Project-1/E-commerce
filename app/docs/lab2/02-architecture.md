# Lab 2 Architecture

## 구성 요소

이번 실습 2의 최소 아키텍처는 아래 구성 요소로 이루어진다.

- `order-service`
  주문 생성 API를 받고 주문 데이터와 Outbox 이벤트를 저장한다.
- MariaDB
  주문 데이터와 `outbox_event` 테이블을 함께 보관한다.
- `outbox_event` 테이블
  발행해야 할 이벤트를 트랜잭션 안에서 안전하게 저장한다.
- Debezium Connector
  MariaDB binlog 또는 CDC 변경 스트림에서 `outbox_event` 변화를 감지한다.
- Kafka
  이벤트를 topic 단위로 전달한다.
- Consumer
  이벤트를 읽어 최소한 로그를 남기거나 다음 처리의 시작점이 된다.

## 데이터 흐름

1. 주문 API 호출
2. 주문 데이터 저장
3. 같은 트랜잭션에서 Outbox 이벤트 저장
4. Debezium이 Outbox 변경 감지
5. Kafka 토픽 발행
6. Consumer 수신 및 후속 처리

## 아키텍처 의도

이번 구조는 "주문 저장"과 "이벤트 발행"을 직접 분리하지 않고,
먼저 DB 안의 Outbox에 이벤트를 적재한 뒤 CDC가 이를 가져가도록 설계한다.

이 방식의 장점은 다음과 같다.

- 애플리케이션 코드에서 Kafka 전송 성공 여부를 직접 관리하지 않아도 된다.
- 주문 저장과 이벤트 기록을 같은 트랜잭션으로 묶을 수 있다.
- Connector와 Consumer를 교체하거나 늘려도 주문 서비스의 핵심 로직 수정이 적다.

## EDA 관점에서의 역할 분리

이번 실습 구조를 EDA 관점에서 보면 각 구성 요소의 역할은 아래와 같다.

- Producer
  - `order-service`
  - 주문 생성이라는 비즈니스 이벤트의 발생 주체
- Event Store
  - `outbox_event` 테이블
  - 발행 전 이벤트를 안전하게 저장하는 위치
- Event Propagation
  - Debezium Connector
  - DB 변경 내용을 CDC로 읽어 Kafka에 전달
- Event Broker
  - Kafka
  - 이벤트를 topic 단위로 전달하는 브로커
- Consumer
  - `event-consumer`
  - Kafka topic을 구독하고 이벤트를 소비하는 주체

따라서 이번 구조는 "주문 서비스가 이벤트를 직접 상대 서비스에 호출하는 구조"가 아니라,
"이벤트를 발행하고 다른 컴포넌트가 이를 구독해서 반응하는 구조"라는 점에서 EDA로 설명할 수 있다.

## 현재 구현 범위의 의미

현재 구현은 전체 시스템을 완전한 이벤트 기반으로 전환한 것이 아니라,
주문 생성 시나리오에 대해 EDA의 핵심 메커니즘을 적용한 MVP 단계다.

즉, 아래 범위까지를 실제로 구현했다.

- 주문 생성
- Outbox 이벤트 저장
- Debezium CDC
- Kafka topic 발행
- Consumer 수신

이 정도 범위만으로도 실습 주제인 "Outbox 기반 대량 처리 가능 구조"의 핵심을 설명하기에 충분하다.

## 설계 결정

이번 실습의 주요 설계 결정은 아래와 같다.

- Producer 역할은 `order-service`가 담당한다.
- 이벤트 타입은 `ORDER_CREATED` 하나로 시작한다.
- CDC 대상은 주문 테이블 전체가 아니라 `outbox_event` 테이블이다.
- 첫 consumer는 비즈니스 변경을 최소화하기 위해 "로그 확인 중심"의 단순 consumer로 시작한다.
- 이벤트 파이프라인이 안정화되면 이후 `payment-service` 같은 실제 후속 처리 서비스로 확장할 수 있다.

## 예상 리소스 배치

- 애플리케이션 코드
  - `app/services/order-service`
- CDC 설정
  - `cdc/connectors`
  - `cdc/scripts`
- 운영 매니페스트
  - 필요 시 `k8s/connect`, `k8s/kafka`, `k8s/cdc`
- 제출 문서
  - `app/docs/lab2`

## 제외 범위

오늘 구현 범위에서 제외하는 항목은 아래와 같다.

- 기존 주문 처리 전체를 완전한 EDA 구조로 재작성하는 작업
- `payment-service`, `product-service`의 비즈니스 로직을 즉시 consumer 기반으로 변경하는 작업
- 다중 이벤트 타입과 복잡한 재처리 정책
- 운영 고가용성 수준의 Kafka/Connect 튜닝

즉 이번 실습은 "대표 시나리오 1개를 안정적으로 연결하는 것"에 집중한다.
