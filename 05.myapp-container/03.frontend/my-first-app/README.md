# my-first-app

## 이 코드는 무엇을 위한 실습인가?
- 03.frontend와 같은 형태의 정적 웹 앱을 별도 샘플로 배포해보는 보조 실습 코드입니다.

## 이 디렉토리 기준 구조/파일 설명
- `src/`: 정적 웹 소스
- `default.conf`: Nginx 설정
- `Dockerfile`: 컨테이너 이미지 설정
- `run.sh`: 실행 보조 스크립트
- `docker-build.sh`, `docker-push.sh`: 이미지 빌드/푸시
- `deploy/`: Kubernetes 배포 매니페스트
