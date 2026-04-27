# E-commerce Lab Submission

이 저장소는 쇼핑몰 MSA를 기반으로 진행한 두 개의 실습 결과를 함께 정리한 제출본

## 실습 구성

- 실습 1: Kubernetes 기반으로 쇼핑몰 서비스를 배포하고 운영 요소를 적용
- 실습 2: Kafka 기반 CDC와 Outbox 패턴을 적용해 주문 생성 이벤트를 EDA 구조로 확장

## 어디부터 보면 좋은가

- 최종 제출 요약
  - `/Users/yangyewon/workspace/shopping-mall-k8s-lab/E-commerce/05.myapp-container/docs/submission/00-final-summary.md`
- 실습 1 문서
  - `/Users/yangyewon/workspace/shopping-mall-k8s-lab/E-commerce/05.myapp-container/docs/lab1`
- 실습 2 문서
  - `/Users/yangyewon/workspace/shopping-mall-k8s-lab/E-commerce/05.myapp-container/docs/lab2`

## 주요 자산 위치

- Kubernetes 매니페스트
  - `/Users/yangyewon/workspace/shopping-mall-k8s-lab/E-commerce/k8s`
- CDC / Connector / 재현 스크립트
  - `/Users/yangyewon/workspace/shopping-mall-k8s-lab/E-commerce/cdc`
- 로컬 실습 2 실행 파일
  - `/Users/yangyewon/workspace/shopping-mall-k8s-lab/E-commerce/05.myapp-container/docker-compose.yml`
  - `/Users/yangyewon/workspace/shopping-mall-k8s-lab/E-commerce/05.myapp-container/docker-compose.lab2.yml`

## 제출 포인트

- 실습 1은 Kubernetes 배포, Ingress, 서비스 연결, HPA/KEDA 적용 및 안정화 과정을 중심으로 정리
- 실습 2는 `order-service` 의 주문 생성 이벤트를 Outbox + Debezium CDC + Kafka + Consumer 로 연결한 EDA MVP를 중심으로 정리
