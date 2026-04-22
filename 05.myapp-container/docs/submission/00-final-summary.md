# Final Submission Summary

## 1. 프로젝트 개요

이 제출본은 쇼핑몰 MSA를 대상으로 진행한 두 개의 실습 결과를 함께 정리한 것이다.

- 실습 1: Kubernetes 기반 대량 요청 처리 가능한 쇼핑몰 서비스 배포 및 운영 요소 적용
- 실습 2: Kafka 기반 CDC + Outbox 적용을 통한 EDA 구조화

## 2. 팀 / 환경 요약

- Namespace: `ecommerce`
- Host: `skala3-cloud1-team4.cloud.skala-ai.com`
- 주요 서비스:
  - `frontend`
  - `user-service`
  - `product-service`
  - `order-service`
  - `payment-service`
  - `mariadb`

## 3. 실습 1 요약

실습 1에서는 쇼핑몰 서비스를 Kubernetes 환경에 배포하고,
외부 접근, 서비스 간 통신, Probe, autoscaling 리소스, 운영 안정화까지 수행했다.

핵심 결과:

- 서비스별 Deployment / Service 분리 완료
- Ingress 기반 외부 노출 완료
- ConfigMap / Secret / PVC / Job 구성 완료
- HPA / KEDA 적용 및 scale-out 검증 완료
- 최종 제출 상태는 autoscaling pause 기반의 안정 버전으로 정리

관련 문서:

- `/Users/yangyewon/workspace/shopping-mall-k8s-lab/E-commerce/05.myapp-container/docs/lab1/00-overview.md`
- `/Users/yangyewon/workspace/shopping-mall-k8s-lab/E-commerce/05.myapp-container/docs/lab1/01-architecture.md`
- `/Users/yangyewon/workspace/shopping-mall-k8s-lab/E-commerce/05.myapp-container/docs/lab1/02-k8s-resources.md`
- `/Users/yangyewon/workspace/shopping-mall-k8s-lab/E-commerce/05.myapp-container/docs/lab1/03-deployment-and-validation.md`
- `/Users/yangyewon/workspace/shopping-mall-k8s-lab/E-commerce/05.myapp-container/docs/lab1/04-issues-and-troubleshooting.md`

## 4. 실습 2 요약

실습 2에서는 `order-service`의 주문 생성 시나리오를 기준으로
Outbox + Debezium CDC + Kafka + Consumer 흐름을 구현했다.

핵심 결과:

- 주문 생성 시 `outbox_event` 저장
- Debezium source connector 등록 및 실행
- Kafka topic `orderdb.order_service_db.outbox_event` 생성
- `event-consumer` 가 해당 topic 메시지 수신

즉, 주문 생성 시나리오 기준 EDA MVP를 검증했다.

관련 문서:

- `/Users/yangyewon/workspace/shopping-mall-k8s-lab/E-commerce/05.myapp-container/docs/lab2/01-scenario.md`
- `/Users/yangyewon/workspace/shopping-mall-k8s-lab/E-commerce/05.myapp-container/docs/lab2/02-architecture.md`
- `/Users/yangyewon/workspace/shopping-mall-k8s-lab/E-commerce/05.myapp-container/docs/lab2/03-implementation-plan.md`
- `/Users/yangyewon/workspace/shopping-mall-k8s-lab/E-commerce/05.myapp-container/docs/lab2/04-environment-check.md`
- `/Users/yangyewon/workspace/shopping-mall-k8s-lab/E-commerce/05.myapp-container/docs/lab2/05-results-and-evidence.md`

## 5. 폴더 구조 안내

- Kubernetes 자산
  - `/Users/yangyewon/workspace/shopping-mall-k8s-lab/E-commerce/k8s`
- CDC / Connector / 스크립트
  - `/Users/yangyewon/workspace/shopping-mall-k8s-lab/E-commerce/cdc`
- 로컬 실습 2 실행 파일
  - `/Users/yangyewon/workspace/shopping-mall-k8s-lab/E-commerce/05.myapp-container/docker-compose.yml`
  - `/Users/yangyewon/workspace/shopping-mall-k8s-lab/E-commerce/05.myapp-container/docker-compose.lab2.yml`
- 서비스 코드
  - `/Users/yangyewon/workspace/shopping-mall-k8s-lab/E-commerce/05.myapp-container/services`

## 6. 재현 방법 요약

### 실습 1

- `k8s/` 아래 공통 리소스와 서비스별 리소스를 기준으로 배포
- 최종 운영 상태는 autoscaling pause 기준 안정 버전

### 실습 2

1. 로컬 compose 기동
2. Kafka Connect 확인
3. MariaDB binlog 확인
4. connector 등록
5. 주문 생성
6. topic 확인
7. consumer 로그 확인

상세 재현 절차:

- `/Users/yangyewon/workspace/shopping-mall-k8s-lab/E-commerce/cdc/README.md`

## 7. 이슈와 해결 요약

### 실습 1

- CPU 부족으로 Pending Pod 발생
- KEDA external metrics 복구 필요
- image pull EOF 발생
- autoscaling pause 와 안정 버전 유지로 최종 정리

### 실습 2

- Outbox payload 컬럼 길이 문제
- Kafka Connect 포트 충돌
- Debezium 이미지 태그 수정 필요
- MariaDB binlog 비활성화
- Debezium 계정 권한 부족

각 이슈는 문서에 원인과 해결을 함께 남겼다.

## 8. 한계와 다음 단계

- 실습 1:
  - 최종 제출 상태는 안정 운영 중심
  - autoscaling 리소스는 유지하되 pause 상태
- 실습 2:
  - 로컬 검증 환경 기준 EDA MVP 완료
  - Kubernetes 환경으로의 Kafka / Connect 확장은 다음 단계
  - Debezium raw CDC envelope 대신 Outbox SMT 적용은 향후 개선 가능
