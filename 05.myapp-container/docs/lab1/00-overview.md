# Lab 1 Overview

## 실습 목표

실습 1의 목표는 사용자, 상품, 주문, 결제, 프론트엔드로 나뉜 쇼핑몰 서비스를
Kubernetes 환경에 배포하고, 대량 요청 상황에서도 운영 가능한 형태로 구성하는 것이다.

이 과정에서 아래 요소를 실제로 적용했다.

- 서비스별 Deployment / Service 분리
- ConfigMap / Secret 기반 설정 주입
- MariaDB 연동
- Ingress 기반 외부 접근
- Liveness / Readiness / Startup Probe
- HPA / KEDA 기반 오토스케일링 적용 및 검증
- 운영 중 발생한 이슈에 대한 안정화 조치

## 구성 서비스

실습 1에서 다룬 주요 서비스는 아래와 같다.

- `frontend`
- `user-service`
- `product-service`
- `order-service`
- `payment-service`
- `mariadb`

## 배포 환경

- Namespace: `ecommerce`
- Host: `skala3-cloud1-team4.cloud.skala-ai.com`
- Ingress class: `nginx`

## 최종 운영 판단

실습 중 HPA / KEDA 적용과 scale-out 검증은 수행했지만,
클러스터 CPU 부족과 일부 이미지 pull 문제로 인해 최종 상태는 "안정 버전 + autoscaling pause" 형태로 정리했다.

즉, 실습 1 제출 시에는 아래 메시지가 가장 정확하다.

- Kubernetes 기반 서비스 배포 및 외부 노출은 완료
- 오토스케일링 리소스는 적용 및 검증 완료
- 운영 안정성을 위해 최종 제출 상태는 1 replica 중심의 안정 버전으로 정리

## 관련 위치

- Kubernetes 리소스: `/Users/yangyewon/workspace/shopping-mall-k8s-lab/E-commerce/k8s`
- 실습 1 아키텍처 문서: `/Users/yangyewon/workspace/shopping-mall-k8s-lab/E-commerce/05.myapp-container/docs/lab1/01-architecture.md`
- 실습 1 검증 문서: `/Users/yangyewon/workspace/shopping-mall-k8s-lab/E-commerce/05.myapp-container/docs/lab1/03-deployment-and-validation.md`
