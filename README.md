# E-commerce Lab Submission

쇼핑몰 MSA를 바탕으로 Kubernetes 배포 구성과 CDC 기반 이벤트 연동 구성을 함께 정리한 저장소입니다.

## 저장소에서 보는 순서

1. `app/docs/submission/00-final-summary.md`
2. `app/docs/lab1`
3. `app/docs/lab2`
4. `k8s`
5. `cdc`

## 디렉토리 개요

- `app`: 애플리케이션 소스, 프런트엔드, 서비스, 프로젝트 문서
- `k8s`: Kubernetes 배포 매니페스트
- `cdc`: Debezium/Kafka Connect 커넥터와 등록 스크립트
- `scripts`: KEDA 관측 및 부하 테스트 보조 스크립트
- `tmp`: 로컬 테스트 중 생성되는 임시 파일

## 주요 범위

- Kubernetes 배포, Ingress, 서비스 노출, 오토스케일링 적용
- `order-service` Outbox 이벤트를 Debezium CDC와 Kafka로 전달하고 소비
