# 05.myapp-container

## 이 저장소는 무엇을 위한 실습인가?
- 다양한 애플리케이션(Spring Boot, Vue, 정적 Frontend, Python FastAPI, Redis 연동)을 컨테이너화하고 Kubernetes에 배포하는 실습 모음입니다.
- 디렉토리별로 기술 스택이 다르며, 공통적으로 Docker 이미지 빌드/푸시와 K8s 매니페스트 적용 흐름을 연습합니다.

## 디렉토리별 목적
- `01.springboot`: Spring Boot 백엔드 컨테이너/Kubernetes 배포 실습
- `02.vuejs`: Vue 3 + Vite 프런트엔드 컨테이너 배포 실습
- `03.frontend`: 순수 HTML/CSS/JS 정적 웹 배포 기초 실습
- `04.python`: FastAPI 서버(health/metrics 포함) 배포 실습
- `05.ai-slim-base`: 공통 AI/Python 베이스 이미지 제작 실습
- `06.app-with-redis`: Spring Boot + Redis Key-Value API 실습

## 학습 가이드(추천 순서)
- 1) `03.frontend`로 정적 웹 컨테이너 배포 흐름 익히기
- 2) `02.vuejs`로 프런트엔드 빌드/배포 확장
- 3) `01.springboot`로 백엔드 API + K8s 운영요소(probe, ingress, pvc) 학습
- 4) `04.python`으로 health/metrics 기반 운영 패턴 학습
- 5) `05.ai-slim-base`로 공통 베이스 이미지 전략 이해
- 6) `06.app-with-redis`로 외부 스토리지(Redis) 연동 실습
