# 05.ai-slim-base

## 이 코드는 무엇을 위한 실습인가?
- AI/ML 실행에 필요한 Python 패키지를 포함한 경량 베이스 컨테이너 이미지를 만드는 실습 코드입니다.
- 이후 서비스 이미지가 공통 베이스를 재사용하도록 구성하는 패턴을 연습합니다.

## 이 디렉토리 기준 구조/파일 설명
- `Dockerfile`: 멀티 스테이지 기반 경량 Python 런타임 이미지 설정
- `requirements.txt`: AI/웹 관련 Python 패키지 목록(torch, fastapi 등)
- `docker-build.sh`, `docker-push.sh`: 베이스 이미지 빌드/푸시 스크립트

## 학습 가이드(추천 순서)
- 1) `requirements.txt`로 공통 런타임 의존성 확인
- 2) `Dockerfile` 멀티 스테이지 구조 확인
- 3) `docker-build.sh`로 베이스 이미지 생성/검증
- 4) 다른 실습 디렉토리에서 베이스 이미지 재사용
