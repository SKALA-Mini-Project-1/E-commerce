# k8s/database

데이터베이스 관련 Kubernetes 매니페스트를 모아둔 디렉토리입니다.

## 포함 파일

- `statefulset.yaml`: 데이터베이스 파드 실행 정의
- `service.yaml`: 클러스터 내부 접근용 서비스
- `init-job.yaml`: 초기화 또는 스키마 준비 작업

## 역할

애플리케이션 서비스가 의존하는 데이터 저장소를 클러스터 안에서 실행하고 초기화합니다.
