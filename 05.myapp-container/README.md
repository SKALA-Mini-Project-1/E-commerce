# 05.myapp-container

## 이 저장소는 무엇을 위한 실습인가?
- 제품, 주문, 결제, 사용자 서비스를 중심으로 백엔드 MSA 구조를 구성하고 Kubernetes 배포 흐름을 연습하는 저장소입니다.
- 기존 샘플 디렉토리를 재배치해 서비스 코드, 프론트엔드, 인프라 자산이 분리된 형태로 관리합니다.

## 디렉토리별 목적
- `services/user-service`: 기존 사용자/지역 API를 포함한 Spring Boot 서비스
- `services/product-service`: `user-service` 골격을 복제한 제품 서비스 스캐폴드
- `services/order-service`: `user-service` 골격을 복제한 주문 서비스 스캐폴드
- `services/payment-service`: `user-service` 골격을 복제한 결제 서비스 스캐폴드
- `frontend/vue-app`: Vue 3 + Vite 프런트엔드
- `frontend/static-web`: 정적 HTML/CSS/JS 프런트 샘플
- `infra/databases`: MariaDB, Redis, MongoDB, PostgreSQL, Qdrant 관련 배포 자산
- `infra/base-images`: 공통 베이스 이미지 자산
- `archive/python-sample`: 현재 주력 구조에서 제외된 FastAPI 샘플
- `docs`: 아키텍처/구조 문서

## 학습 가이드(추천 순서)
- 1) `services/user-service` 구조로 현재 Spring Boot 골격 파악
- 2) `services/product-service`, `services/order-service`, `services/payment-service`에 도메인 코드 채우기
- 3) `frontend/vue-app`에서 백엔드 API 연동 확장
- 4) `infra/databases/mariadb` 기준으로 MariaDB 배포 흐름 정리
- 5) 서비스별 `k8s/`, `kustomize/` 구조를 그대로 유지하며 배포 단위 관리
