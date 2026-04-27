# Lab 2 Implementation Plan

## 오늘 구현 범위

오늘 구현 범위는 "주문 생성 이벤트를 Outbox + CDC + Kafka로 안전하게 흘려보내는 최소 MVP"다.

세부 범위:

- `order-service`에 Outbox 엔티티/테이블 추가
- 주문 생성 시 주문 데이터와 Outbox 이벤트를 같은 트랜잭션으로 저장
- Debezium connector 등록
- Kafka topic 메시지 발행 확인
- Consumer 수신 확인
- 결과 문서 작성

오늘은 "이벤트 발행 파이프라인을 입증하는 것"이 목표이며,
후속 서비스의 실제 비즈니스 반영까지는 필수 범위로 두지 않는다.

## 작업 순서

1. 시나리오 고정
2. 아키텍처 문서 작성
3. 환경 체크 결과 정리
4. `order-service`에 Outbox 테이블/엔티티/저장 코드 추가
5. 로컬 또는 별도 환경에서 connector 연결
6. Kafka 토픽 메시지 확인
7. Consumer 확인
8. 결과 문서 작성
9. 마지막에 필요하면 `ecommerce` 연결 범위만 확장

## 단계별 상세 계획

### 1. 시나리오 문서 작성

- 대표 시나리오를 `주문 생성`으로 고정한다.
- 왜 Outbox/CDC가 필요한지 설명한다.
- 무엇을 구현하고 무엇을 제외할지 미리 선언한다.

### 2. 아키텍처 문서 작성

- Producer, DB, Outbox, Connector, Kafka, Consumer의 역할을 정리한다.
- 데이터 흐름을 단계별로 문서화한다.
- 제출 시 설명할 핵심 설계 결정을 확정한다.

### 3. 환경 체크 결과 정리

- Kafka, Connect, Debezium이 이미 준비되어 있는지 확인한다.
- `order-service`가 사용하는 DB와 CDC 적용 가능성을 확인한다.
- 실습 1에서 안정화한 `ecommerce` 환경을 바로 흔들지 않아도 되는지 판단한다.

### 4. `order-service`에 Outbox 테이블/엔티티/저장 코드 추가

- 주문 생성 로직을 크게 바꾸지 않는다.
- 주문 저장 후 같은 트랜잭션 안에서 Outbox row를 저장한다.
- 기존 API 성공/실패 흐름이 깨지지 않는지 확인한다.

### 5. 로컬 또는 별도 환경에서 connector 연결

- Connector 등록 JSON을 `cdc/connectors`에 둔다.
- 등록/조회 스크립트는 `cdc/scripts`에 둔다.
- 가능하면 `ecommerce` 본 환경 전에 별도 검증 경로를 우선 사용한다.
- 현재는 기존 `docker-compose.yml` 에 실습 2용 `docker-compose.lab2.yml` 을 겹쳐 실행하는 방식을 우선 사용한다.
- 이 방식은 기존 서비스 구성은 유지하고 Kafka/Connect/Debezium 만 추가할 수 있어 위험 범위를 줄여준다.

### 6. Kafka 토픽 메시지 확인

- 주문 생성 후 topic에 이벤트가 생성되는지 확인한다.
- 이벤트 payload가 예상 구조인지 확인한다.
- 첫 단계에서는 `orderdb.order_service_db.outbox_event` 같은 raw CDC topic 확인을 목표로 한다.

### 7. Consumer 확인

- 첫 consumer는 단순 로그 소비 형태로 시작한다.
- 별도 `event-consumer` 서비스를 사용해 Kafka 메시지를 읽고 로그로 출력한다.
- 이벤트가 실제로 소비되는지 확인한다.

### 8. 결과 문서 작성

- 성공한 명령, 로그, 스크린샷 위치를 바로 정리한다.
- 이슈와 해결 과정을 함께 남긴다.

### 9. 마지막에 필요하면 `ecommerce`와 연결 범위만 확장

- 최소 MVP가 안정적으로 동작할 때만 운영 연결 범위를 넓힌다.
- 실습 1 안정 환경을 불필요하게 흔들지 않는다.

## 중간 확인 포인트

- 주문 생성 후 Outbox row가 생기는가
- Connector 상태가 Running 인가
- Kafka 토픽 메시지가 생성되는가
- Consumer가 이벤트를 읽는가
- 기존 주문 API 동작이 깨지지 않았는가
- `ecommerce` 환경을 바로 건드리지 않고도 검증 가능한가

## 리스크

- Connector 설정 오류
- CDC 대상 테이블 누락
- Kafka 토픽 미생성
- 이벤트 포맷 불일치
- order-service 코드 변경으로 기존 주문 API 실패
- 로컬/별도 검증 없이 바로 k8s에 올려서 문제 범위가 커지는 상황

## 리스크 대응 전략

- 처음부터 결제/재고 비즈니스 로직까지 이벤트 기반으로 바꾸지 않는다.
- 첫 consumer는 단순 로그 확인 형태로 제한한다.
- 환경 체크를 먼저 하고, 가능하면 로컬 또는 별도 검증 경로를 사용한다.
- 구현 후에는 단계별로 증빙을 남겨 제출 문서와 바로 연결한다.
