# 백엔드 MSA 디렉토리 구조 제안

## 전제

- 대상 백엔드는 `user`, `product`, `order`, `payment` 서비스다.
- 기존 Kubernetes 설정 파일의 값은 변경하지 않는다.
- `k8s` 관련 파일은 내용 수정 없이 위치만 재정리하는 것을 기준으로 한다.
- 지금 있는 샘플 코드는 향후 MSA 서비스별로 분리해 관리하기 쉽게 재배치하는 방향으로 본다.

## 권장 최상위 구조

```text
app/
├── services/
│   ├── user-service/
│   │   ├── src/
│   │   ├── pom.xml
│   │   ├── Dockerfile
│   │   ├── docker-build.sh
│   │   ├── docker-push.sh
│   │   ├── env.properties
│   │   ├── README.md
│   │   ├── k8s/
│   │   └── kustomize/
│   │
│   ├── product-service/
│   │   ├── src/
│   │   ├── pom.xml
│   │   ├── Dockerfile
│   │   ├── docker-build.sh
│   │   ├── docker-push.sh
│   │   ├── env.properties
│   │   ├── README.md
│   │   ├── k8s/
│   │   └── kustomize/
│   │
│   ├── order-service/
│   │   ├── src/
│   │   ├── pom.xml
│   │   ├── Dockerfile
│   │   ├── docker-build.sh
│   │   ├── docker-push.sh
│   │   ├── env.properties
│   │   ├── README.md
│   │   ├── k8s/
│   │   └── kustomize/
│   │
│   └── payment-service/
│       ├── src/
│       ├── pom.xml
│       ├── Dockerfile
│       ├── docker-build.sh
│       ├── docker-push.sh
│       ├── env.properties
│       ├── README.md
│       ├── k8s/
│       └── kustomize/
│
├── frontend/
│   ├── vue-app/
│   └── static-web/
│
├── infra/
│   ├── databases/
│   │   ├── mariadb/
│   │   ├── mongodb/
│   │   ├── postgresql-pgvector/
│   │   ├── qdrant/
│   │   └── redis/
│   │
│   └── base-images/
│       └── ai-slim-base/
│
├── docs/
│   ├── order-payment-msa-analysis.md
│   └── backend-msa-directory-structure.md
│
└── archive/
    ├── python-sample/
    └── old-frontend-samples/
```

## 기존 디렉토리 기준 매핑

현재 저장소의 디렉토리를 아래처럼 보는 것이 가장 자연스럽다.

### 1. 기존 Spring Boot

기존:

```text
01.springboot/
```

변경 후:

```text
services/user-service/
```

설명:

- 현재 `01.springboot`에는 사용자/지역 API가 이미 구현돼 있으므로 `user-service`의 시작점으로 쓰기 적합하다.
- `k8s/`, `kustomize/`, `Dockerfile`, `docker-build.sh`, `docker-push.sh`는 그대로 함께 이동한다.
- 이 경우 k8s 파일 안의 값은 손대지 않고, 파일의 위치만 `services/user-service/` 아래로 바뀐다.

### 2. 신규 제품 서비스

신규 생성:

```text
services/product-service/
```

설명:

- 상품, 가격, 재고 조회를 담당한다.
- 초기에는 `user-service`와 동일한 Spring Boot 골격을 복제해서 시작하는 방식이 가장 빠르다.
- 이후 도메인만 `product` 기준으로 교체한다.

### 3. 신규 주문 서비스

신규 생성:

```text
services/order-service/
```

설명:

- 주문 생성, 주문 상세 조회, 주문 상태 변경을 담당한다.
- 결제 전후의 상태 전이를 관리하는 핵심 서비스다.

### 4. 신규 결제 서비스

신규 생성:

```text
services/payment-service/
```

설명:

- 결제 요청, 승인, 취소, 실패 기록을 담당한다.
- 주문 서비스와 분리해서 관리해야 결제 변경 영향이 주문 쪽으로 번지지 않는다.

### 5. Vue 프론트

기존:

```text
02.vuejs/
03.frontend/
```

변경 후:

```text
frontend/vue-app/
frontend/static-web/
```

설명:

- 프론트 샘플은 백엔드와 분리해 `frontend/` 아래에 모으는 편이 관리가 쉽다.
- 현재 요청은 백엔드 중심이지만, 프론트 위치도 함께 정리해 두면 저장소 구조가 읽기 쉬워진다.

### 6. 데이터베이스 관련 자산

기존:

```text
06.database/
```

변경 후:

```text
infra/databases/
```

설명:

- MariaDB, Redis, MongoDB, PostgreSQL, Qdrant는 모두 애플리케이션이 아니라 인프라 자산이다.
- 따라서 `infra/databases/` 아래로 모으는 것이 책임 분리에 맞다.
- 이번 프로젝트에서는 실제 주력 DB가 MariaDB이므로 `infra/databases/mariadb/`가 핵심이 된다.

### 7. Python 샘플

기존:

```text
04.python/
```

변경 후:

```text
archive/python-sample/
```

설명:

- 현재 목적이 제품/주문/결제/사용자 기반 Spring Boot MSA 구축이라면 FastAPI 샘플은 주력 경로가 아니다.
- 바로 삭제하지 않고 `archive/`로 옮겨 참고용으로 남기는 편이 안전하다.

### 8. AI 베이스 이미지

기존:

```text
05.ai-slim-base/
```

변경 후:

```text
infra/base-images/ai-slim-base/
```

설명:

- 이것도 서비스 코드가 아니라 이미지 빌드 자산이므로 `infra` 성격이 더 맞다.

## 서비스별 내부 구조

각 백엔드 서비스는 같은 규칙으로 맞추는 것을 권장한다.

```text
services/order-service/
├── src/
│   ├── main/
│   │   ├── java/com/example/orderservice/
│   │   │   ├── controller/
│   │   │   ├── service/
│   │   │   ├── domain/
│   │   │   ├── repository/
│   │   │   ├── dto/
│   │   │   ├── client/
│   │   │   ├── config/
│   │   │   └── exception/
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-local.yml
│   │       ├── application-prod.yml
│   │       └── application-mariadb.yml
│   └── test/
├── Dockerfile
├── pom.xml
├── k8s/
├── kustomize/
└── README.md
```

이 구조를 `user-service`, `product-service`, `order-service`, `payment-service`에 동일하게 적용한다.

## 각 디렉토리의 역할

### `services/`

- 실제 비즈니스 백엔드 서비스 소스코드를 둔다.
- 서비스별로 독립 빌드, 독립 배포, 독립 수정이 가능해야 한다.

### `services/user-service/`

- 사용자, 사용자 지역, 사용자 조회/수정 API 담당
- 현재 `01.springboot`의 직접적인 후속 위치

### `services/product-service/`

- 상품, 가격, 재고 API 담당

### `services/order-service/`

- 주문 생성, 주문 상세, 주문 상태 전이 API 담당

### `services/payment-service/`

- 결제 승인, 결제 이력, 결제 취소 API 담당

### `frontend/`

- 프론트엔드 애플리케이션을 모아 둔다.
- 백엔드와 분리해서 배포 단위를 명확히 한다.

### `infra/`

- 서비스 코드가 아닌 공통 인프라 자산을 둔다.
- DB, Helm 값, 베이스 이미지 같은 운영 자산을 모은다.

### `infra/databases/mariadb/`

- MariaDB 설치 스크립트, values 파일, 운영 문서 위치
- 주문/결제/제품/사용자 서비스의 공통 RDB 인프라

### `docs/`

- 아키텍처, API 설계, 배포 구조 문서를 둔다.
- 구현 전에 합의할 내용을 여기에 정리한다.

### `archive/`

- 현재 주력 구조에서 빠진 샘플이나 예전 예제를 보관한다.
- 바로 삭제하기 부담스러운 코드의 임시 보관소 역할이다.

## 이 구조의 장점

- 서비스 코드와 인프라 자산이 분리된다.
- 백엔드 서비스가 늘어나도 `services/` 아래에서 규칙적으로 관리할 수 있다.
- k8s 설정 파일은 값 변경 없이 서비스 디렉토리 아래로만 이동 가능하다.
- 현재 사용자 샘플을 `user-service`로 자연스럽게 승격할 수 있다.
- MariaDB를 중심으로 MSA를 확장할 때 저장소 구조가 덜 혼란스럽다.

## 실제 이동 기준 요약

```text
01.springboot                      -> services/user-service
02.vuejs                           -> frontend/vue-app
03.frontend                        -> frontend/static-web
04.python                          -> archive/python-sample
05.ai-slim-base                    -> infra/base-images/ai-slim-base
06.database/mariadb                -> infra/databases/mariadb
06.database/mongodb                -> infra/databases/mongodb
06.database/postgresql-pgvector    -> infra/databases/postgresql-pgvector
06.database/qdrant                 -> infra/databases/qdrant
06.database/redis                  -> infra/databases/redis
```

## 권장 다음 단계

1. `01.springboot`를 `services/user-service`로 옮긴다.
2. 그 구조를 기준으로 `product-service`, `order-service`, `payment-service`를 생성한다.
3. 각 서비스의 `k8s/`, `kustomize/`, `Dockerfile`은 같은 패턴으로 유지한다.
4. 그다음 도메인 모델과 API를 각 서비스별로 채운다.
