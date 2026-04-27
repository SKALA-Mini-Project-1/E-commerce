# Lab 1 Kubernetes Resources

## 리소스 정리 원칙

실습 1의 Kubernetes 매니페스트는 루트 `k8s/` 아래로 통합해 관리했다.
서비스별로 흩어진 `k8s/`, `kustomize/` 구조 대신 제출본에서 한 번에 보기 쉽게 정리한 것이 특징이다.

위치:

- `/Users/yangyewon/workspace/shopping-mall-k8s-lab/E-commerce/k8s`

## 주요 파일 구성

### 공통 리소스

- `namespace.yaml`
  - `ecommerce` 네임스페이스 생성
- `configmap.yaml`
  - 서비스 공통 환경 변수 관리
- `secret.yaml`
  - MariaDB 계정 정보 등 민감 정보 관리
- `ingress.yaml`
  - API 및 프론트엔드 외부 노출
- `priorityclass.yaml`
  - 스케줄링 우선순위 설정

### 서비스별 리소스

- `user/`
  - `deployment.yaml`
  - `service.yaml`
- `product/`
  - `deployment.yaml`
  - `service.yaml`
  - `scaledobject.yaml`
- `order/`
  - `deployment.yaml`
  - `service.yaml`
  - `scaledobject.yaml`
- `payment/`
  - `deployment.yaml`
  - `service.yaml`
  - `scaledobject.yaml`

### 데이터베이스 리소스

- `database/pvc.yaml`
- `database/deployment.yaml`
- `database/service.yaml`
- `database/init-job.yaml`

## 각 리소스의 역할

### Deployment

- 서비스 컨테이너 실행
- image, env, probe, resources 정의
- rolling update 전략 적용

### Service

- ClusterIP 를 통한 내부 통신
- 서비스 간 고정 이름 기반 접근

### ConfigMap / Secret

- DB host, DB name, base URL, profile 등의 설정을 코드와 분리
- Secret 은 루트 공용 secret 으로 통합 관리

### Ingress

- 외부 host/path 기반 라우팅
- API, Swagger, Actuator, 프론트 접근 통합

### ScaledObject

- KEDA 기반 scale-out 트리거 정의
- 메모리 / Prometheus 지표 기준 오토스케일 실험에 사용

## 운영 과정에서의 파일 상태

실습 중에는 일부 파일을 `.disabled` 형태로 분리해 관리했다.

예:

- Canary 실험용 파일
- 모니터링 실험용 일부 파일
- 당장 적용하지 않을 기능 파일

이 방식은 삭제 대신 "현재 제출 범위에서 제외"를 분명히 보여주는 데 도움이 됐다.

## 제출 시 강조 포인트

실습 1 제출에서는 단순히 YAML 개수가 많은 것이 중요한 게 아니라,
아래처럼 역할 분리가 잘 보이는지가 중요하다.

- 공통 리소스와 서비스 리소스가 분리됨
- DB 리소스가 별도 폴더로 정리됨
- autoscaling 리소스가 서비스별로 분리됨
- 운영 중 제외한 파일도 `.disabled` 로 의도가 드러남
