# k8s

프로젝트를 Kubernetes에 배포하기 위한 매니페스트를 모아둔 디렉토리입니다.

## 공통 파일

- `namespace.yaml`: 네임스페이스 정의
- `configmap.yaml`: 공통 설정
- `secret.yaml`: 비밀값 정의
- `priorityclass.yaml`: 우선순위 클래스
- `ingress.yaml`: 외부 진입점

## 하위 디렉토리

- `user`, `product`, `order`, `payment`: 서비스별 Deployment/Service/오토스케일 설정
- `database`: 데이터베이스 초기화 및 서비스 자산
- `frontend`: 프런트엔드 관련 매니페스트 보관
- `monitoring`: ServiceMonitor와 대시보드 파일

## 읽는 순서

`namespace.yaml` → 공통 설정 파일 → 서비스별 디렉토리 → `ingress.yaml` 순서로 보면 전체 배포 구성이 보입니다.
