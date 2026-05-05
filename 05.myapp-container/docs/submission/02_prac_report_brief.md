# 실습 프로젝트 제출 요약

## 1. 팀명 및 팀원명

- 팀명: `등차수열`
- 팀원: `서지윤`, `양예원`, `박지현`, `장재훈`

## 2. 결과 정리 자료

### 2-1. 아키텍처 구조 및 비즈니스 애플리케이션 목록 및 역할

본 프로젝트는 쇼핑몰 MSA를 Kubernetes에 배포하고, 주문 생성 흐름에 대해 CDC + Outbox Pattern을 추가 적용한 구조다. 외부 요청은 Ingress를 통해 진입하고, 내부에서는 서비스별 ClusterIP 통신으로 연결했다. 데이터는 MariaDB를 공용 인스턴스로 두되 서비스별 DB 스키마를 분리해 논리적 경계를 유지했다.

| 구성 요소 | 핵심 역할 |
| --- | --- |
| `frontend` | 상품 조회, 장바구니, 주문/사용자 화면 제공 |
| `user-service` | 사용자 조회 및 관리 API 제공 |
| `product-service` | 상품 조회, 재고 차감/복원 처리 |
| `order-service` | 주문 생성/조회/취소, 사용자/상품 서비스 연동, Outbox 이벤트 적재 |
| `payment-service` | 주문 기준 결제 요청/조회/상태 처리 |
| `mariadb` | 서비스별 DB 스키마 저장 |
| `KEDA + HPA` | 부하 기반 오토스케일링 |
| `Debezium + Kafka + event-consumer` | Outbox 이벤트 CDC 전파 및 소비 |

주요 호출 관계는 `order-service -> user-service`, `order-service -> product-service`, `payment-service -> order-service`다.

### 2-2. CDC & Outbox Pattern 적용 구조

`order-service`는 주문 생성 시 주문 데이터 저장과 함께 같은 트랜잭션 안에서 `outbox_event` 테이블에 `ORDER_CREATED` 이벤트를 저장한다. 이후 Debezium source connector가 `order_service_db.outbox_event` 테이블 변경을 감지해 Kafka topic `orderdb.order_service_db.outbox_event`로 발행하고, `event-consumer`가 이를 구독해 로그로 수신한다.

핵심 의미는 주문 저장과 이벤트 발행을 분리하지 않고, DB 트랜잭션 안에서 안전하게 기록한 뒤 CDC로 전파한다는 점이다.

### 2-3. ScaledObject 기반 부하 적용 및 적용 결과

부하 테스트는 각 서비스의 `/api/load-cpu`, `/api/load-memory` 엔드포인트를 사용해 CPU/메모리 사용량과 Prometheus 요청 수 메트릭을 증가시키는 방식으로 진행했다. `product-service`, `order-service`, `payment-service`에는 KEDA `ScaledObject`를 적용했고, 메모리와 Prometheus 쿼리 2개 트리거를 함께 사용했다.

| 서비스 | 설정 값 | 결과 |
| --- | --- | --- |
| `order-service` | `min 1 / max 3`, memory `409Mi`, Prometheus threshold `600` | `HPA`가 활성 상태이며 Pod가 `3개`까지 확장되어 동작 중 |
| `product-service` | `min 1 / max 3`, memory `307Mi`, Prometheus threshold `600` | 테스트 후 `paused-replicas=1`로 안정화 |
| `payment-service` | `min 1 / max 3`, memory `409Mi`, Prometheus threshold `600` | 테스트 후 `paused-replicas=1`로 안정화 |

정리하면 KEDA-HPA 연동과 실제 scale-out 자체는 검증했고, 운영 안정성을 위해 일부 서비스는 pause 상태로 유지했다.

## 3. 소스코드 및 k8s 리소스

- 애플리케이션 소스코드: `05.myapp-container/services/user-service`, `product-service`, `order-service`, `payment-service`
- 프론트엔드: `05.myapp-container/frontend/vue-app`
- CDC / Connector / 스크립트: `cdc/connectors/order-outbox-source.json`, `cdc/scripts/register-order-outbox-connector.sh`, `cdc/scripts/check-connectors.sh`
- 로컬 CDC 검증 환경: `05.myapp-container/docker-compose.lab2.yml`
- Kubernetes 공통 리소스: `k8s/namespace.yaml`, `k8s/configmap.yaml`, `k8s/secret.yaml`, `k8s/priorityclass.yaml`, `k8s/ingress.yaml`
- Kubernetes 서비스 리소스: `k8s/user`, `k8s/product`, `k8s/order`, `k8s/payment`
- 데이터베이스 리소스: `k8s/database/service.yaml`, `k8s/database/statefulset.yaml`, `k8s/database/init-job.yaml`

## 4. 배포 결과 캡처

본 항목은 별도 첨부로 정리한다.

첨부 대상:
- `kubectl get pod -n ecommerce`
- `kubectl get svc -n ecommerce`
- `kubectl get ingress -n ecommerce`
- `kubectl get hpa -n ecommerce`
- `kubectl get scaledobject -n ecommerce`
- `kubectl get kafkaconnect -A`
- `kubectl get kafkaconnector -A`

## 5. 배포 환경 정보

- 클러스터명: `skala3-cloud1-team4`
- kubectl context: `skala3-cloud1-team4`
- namespace: `ecommerce`
- Ingress host: `http://skala3-cloud1-team4.cloud.skala-ai.com`
- 주요 경로: `/frontend`, `/api/users`, `/api/products`, `/api/orders`, `/api/payments`
- 클러스터 접속 방식: 수업 환경에서 발급된 `kubeconfig`와 EKS 인증 권한 필요
- 민감 정보 관리: DB 계정/비밀번호는 `k8s/secret.yaml` 및 로컬 compose 환경변수로 분리 관리

CDC 로컬 검증 환경 포트는 아래와 같다.

- `order-service`: `http://localhost:18083`
- `Kafka Connect`: `http://localhost:18093`
- `Kafka UI`: `http://localhost:18085`
- `Kafka bootstrap`: `localhost:19092`

## 6. 실습 과정을 통해 배운 레슨런

- 오토스케일링은 YAML만 배포한다고 끝나지 않고, KEDA, Prometheus, HPA 메트릭 체인이 모두 정상이어야 실제 동작한다.
- scale-out 성공 여부는 애플리케이션 로직보다 클러스터 자원, 이미지 레지스트리 상태, probe 설정 영향을 크게 받는다.
- Outbox Pattern은 비즈니스 트랜잭션과 이벤트 발행 사이의 정합성 문제를 줄이는 데 효과적이었다.
- CDC는 애플리케이션 코드 수정 없이 이벤트를 외부로 전달할 수 있어 EDA 확장에 유리했다.
- 실습 관점에서는 “최신 상태 유지”보다 “안정적으로 동작하는 상태를 남기는 것”이 더 중요했다.

## 7. 주요 이슈 및 해결 방안

- `Pending Pod`
  `product-service`, `payment-service` scale-out 시 `Insufficient cpu`가 발생해 `requests`, `maxReplicaCount`를 조정하고 일부 ScaledObject는 pause로 안정화했다.

- `external.metrics.k8s.io / HPA target <unknown>`
  KEDA와 Prometheus 상태가 불안정할 때 발생했고, 관련 컴포넌트를 정상화한 뒤 external metrics를 복구했다.

- `ImagePullBackOff / EOF`
  신규 Pod가 사설 레지스트리에서 이미지를 받지 못해 확장 실패가 발생했고, 최신 revision 고집 대신 안정 revision 유지와 rollback 전략으로 대응했다.

- `Outbox payload 길이 문제`
  주문 상세 JSON 저장 시 컬럼 길이 부족 문제가 있어 `payload` 컬럼을 `LONGTEXT`로 확장했다.

- `CDC 사전 조건 미비`
  Debezium 동작을 위해 MariaDB `binlog`, `ROW` 포맷, 계정 권한이 필요했고, 이를 compose 설정과 DB 권한 부여로 해결했다.