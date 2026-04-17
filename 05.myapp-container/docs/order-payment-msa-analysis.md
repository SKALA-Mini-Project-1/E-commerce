# 주문/결제 MSA 프로젝트 사전 분석

## 1. 현재 프로젝트 구성 요약

- `services/user-service`: JPA 기반 Spring Boot API 서버
- `services/product-service`: `user-service` 골격을 복제한 제품 서비스 스캐폴드
- `services/order-service`: `user-service` 골격을 복제한 주문 서비스 스캐폴드
- `services/payment-service`: `user-service` 골격을 복제한 결제 서비스 스캐폴드
- `frontend/vue-app`: Vue 3 + Vite 프론트엔드
- `frontend/static-web`: 정적 HTML/CSS/JS 예제
- `archive/python-sample`: FastAPI 기반 운영 실습 서버
- `infra/databases`: MariaDB, PostgreSQL+pgvector, MongoDB, Redis, Qdrant의 설치/운영 스크립트

현재 저장소는 "주문/결제 MSA"가 이미 구현된 상태는 아니고, 컨테이너화와 Kubernetes 배포 실습용 샘플들이 모여 있는 구조다.

## 2. 현재 확인된 API

### 2.1 Spring Boot API (`services/user-service`)

기본 prefix는 `/api` 이다.

#### User API

- `GET /api/users`
  - 전체 사용자 조회
  - `name` 쿼리 파라미터로 이름 필터 가능
- `GET /api/users/{id}`
  - 사용자 단건 조회
- `POST /api/users`
  - 사용자 생성
- `PUT /api/users/{id}`
  - 사용자 수정
- `DELETE /api/users/{id}`
  - 사용자 삭제

#### 운영/관리 API

- `GET /api/probe`
  - 현재 liveness/readiness 상태 조회
- `POST /api/probe`
  - liveness/readiness 상태 변경
- `GET /api/developer-info`
  - 개발자/팀 정보 조회
- `GET /api/load-cpu`
  - CPU 부하 유발
- `GET /api/load-memory`
  - Memory 부하 유발

#### Actuator / Swagger

- Swagger UI: `/swagger/swagger-ui`
- OpenAPI docs: `/swagger/swagger-docs`
- Prometheus metrics: `/actuator/prometheus`
- Health: `/actuator/health`, `/actuator/health/liveness`, `/actuator/health/readiness`

### 2.2 Vue 프론트엔드 API 사용 (`frontend/vue-app`)

현재 Vue 프론트는 실질적으로 아래 API만 사용한다.

- `GET /api/users`

로컬 개발에서는 Vite proxy가 `/api` 요청을 `http://localhost:8080` 으로 전달한다.

### 2.3 FastAPI (`archive/python-sample`)

이 서버는 주문/결제 도메인 서버가 아니라, 운영 연습용 API에 가깝다.

- `GET /`
  - HTML 상태 페이지
- `POST /`
  - 상태값 갱신
- `GET /metrics`
  - CPU/Memory JSON 조회
- `GET /prometheus`
  - Prometheus 포맷 메트릭
- `GET /healthz`
  - liveness 용도
- `GET /ready`
  - readiness 용도
- `GET /info`
  - 환경변수 기반 정보 조회
- `GET /pod-ip`
  - Pod IP 조회
- `GET /hostname`
  - 호스트명 조회

## 3. 현재 데이터 모델

### Spring Boot DB 모델

- `users`
  - `id`
  - `name`
  - `email`

개발 기본 프로파일은 H2 in-memory DB를 사용하고, MariaDB 프로파일도 이미 존재한다.

### 현재 DB 자산

- MariaDB
- PostgreSQL + pgvector
- MongoDB
- Redis
- Qdrant

즉, 저장소에는 주문/결제 MSA를 만들 때 필요한 "DB 선택지"와 "K8s 배포 자산"은 이미 어느 정도 준비되어 있다.

## 4. 주문/결제 MSA로 확장할 때 권장 서비스 분리

현재 구조를 그대로 확장한다면 아래 구성이 가장 현실적이다.

### 최소 권장 서비스

1. `user-service`
   - 기존 `01.springboot`의 사용자 예제를 확장하거나 별도 분리
2. `order-service`
   - 주문 생성, 주문 조회, 주문 상태 관리
3. `payment-service`
   - 결제 요청, 결제 승인/실패/취소 처리
4. `product-service` 또는 `catalog-service`
   - 상품, 가격, 재고 기본 조회
5. `gateway`
   - 외부 진입점
6. `frontend`
   - Vue 화면

### 선택 서비스

- `inventory-service`
  - 주문 시 재고 차감이 필요한 경우 분리
- `notification-service`
  - 결제 완료/실패 알림
- `discovery/config`
  - 학습 목적이면 생략 가능, 운영형이면 고려

## 5. 주문/결제 MSA에서 필요한 핵심 API 초안

### Order Service

- `POST /api/orders`
  - 주문 생성
- `GET /api/orders`
  - 주문 목록 조회
- `GET /api/orders/{orderId}`
  - 주문 상세 조회
- `PATCH /api/orders/{orderId}/status`
  - 주문 상태 변경
- `POST /api/orders/{orderId}/cancel`
  - 주문 취소

예시 주문 상태:

- `CREATED`
- `PAYMENT_PENDING`
- `PAID`
- `FAILED`
- `CANCELLED`

### Payment Service

- `POST /api/payments`
  - 결제 요청
- `GET /api/payments/{paymentId}`
  - 결제 상세 조회
- `GET /api/payments/order/{orderId}`
  - 주문 기준 결제 조회
- `POST /api/payments/{paymentId}/confirm`
  - 결제 승인
- `POST /api/payments/{paymentId}/cancel`
  - 결제 취소
- `POST /api/payments/webhook`
  - 외부 PG 콜백 수신

예시 결제 상태:

- `REQUESTED`
- `APPROVED`
- `DECLINED`
- `CANCELLED`
- `REFUNDED`

### Product Service

- `GET /api/products`
- `GET /api/products/{productId}`
- `POST /api/products`
- `PATCH /api/products/{productId}/stock`

## 6. 서비스 간 호출 흐름 권장안

### 동기 기반 최소 구현

1. 프론트가 `gateway` 또는 `order-service`에 주문 생성 요청
2. `order-service`가 주문 생성 후 `PAYMENT_PENDING` 상태 저장
3. `payment-service`에 결제 요청
4. 결제 성공 시 `order-service` 상태를 `PAID`로 변경
5. 실패 시 `FAILED`로 변경

학습용 1차 구현은 동기 REST 호출로 충분하다.

### 운영형 확장

- 주문 생성: 동기 REST
- 결제 완료 이벤트 반영: 비동기 메시지 브로커(Kafka/RabbitMQ)
- 보상 트랜잭션: Saga 패턴 고려

## 7. DB 권장안

현재 저장소 자산 기준으로는 아래 조합이 가장 무난하다.

### 권장 조합

- `order-service`: PostgreSQL 또는 MariaDB
- `payment-service`: PostgreSQL 또는 MariaDB
- `product-service`: PostgreSQL 또는 MariaDB
- `Redis`: 캐시, 세션, idempotency key 저장

### 이 저장소에서의 현실적인 선택

- 이미 Spring Boot + JPA 샘플이 있으므로 주문/결제 서비스는 Spring Boot로 만드는 것이 가장 빠르다.
- 관계형 트랜잭션이 중요하므로 1차 구현 DB는 `PostgreSQL` 또는 `MariaDB`가 적합하다.
- `MongoDB`, `Qdrant`, `pgvector`는 주문/결제 핵심 기능보다는 확장 기능에 가깝다.
  - MongoDB: 로그, 비정형 문서
  - Redis: 캐시/락/중복 요청 방지
  - Qdrant/pgvector: 추천, 검색, AI 기능

## 8. Kubernetes 배포 관점 정리

현재 저장소에는 이미 다음 요소들이 있다.

- Deployment 템플릿
- Service 템플릿
- Ingress 템플릿
- ConfigMap/Secret 예시
- readiness/liveness probe 예제
- Prometheus scrape annotation 예제
- DB Helm 설치 스크립트

즉, 주문/결제 MSA를 올릴 때 필요한 쿠버네티스 기본 골격은 재사용 가능하다.

### 권장 배포 단위

- `frontend` Deployment + Service
- `gateway` Deployment + Service
- `order-service` Deployment + Service
- `payment-service` Deployment + Service
- `product-service` Deployment + Service
- `postgres` 또는 `mariadb` Helm release
- `redis` Helm release
- Ingress 1개 이상

### 서비스별 필수 설정

- `readinessProbe`
- `livenessProbe`
- `resources.requests/limits`
- `ConfigMap`
  - DB host
  - DB name
  - 외부 API URL
- `Secret`
  - DB password
  - 결제 키

### Ingress 예시 역할

- `/` -> frontend
- `/api/orders` -> order-service
- `/api/payments` -> payment-service
- `/api/products` -> product-service

### 네임스페이스 권장

- `msa-shop-dev`
- `msa-shop-prod`

## 9. 현재 프로젝트를 기반으로 한 추천 구현 순서

1. `01.springboot`를 복제 또는 모듈 분리해서 `order-service` 생성
2. 같은 방식으로 `payment-service` 생성
3. `02.vuejs`에서 주문/결제 화면 추가
4. DB는 우선 PostgreSQL 또는 MariaDB 하나로 시작
5. 서비스별 Dockerfile/Deployment/Service 작성
6. Ingress로 프론트와 API 라우팅 연결
7. 이후 Redis 캐시와 결제 웹훅, 주문 상태 전이 고도화

## 10. 결론

현재 저장소는 "주문/결제 MSA 완성본"은 아니지만, 아래 기반은 이미 갖춰져 있다.

- Spring Boot API 샘플
- Vue 프론트 샘플
- FastAPI 운영 샘플
- 여러 DB의 K8s 설치 자산
- 컨테이너 이미지 빌드/푸시 스크립트
- K8s 배포 템플릿

따라서 다음 단계는 기존 샘플을 재사용해 `order-service`, `payment-service`, `frontend`, `gateway` 중심으로 실제 도메인 API를 구현하는 것이다.
