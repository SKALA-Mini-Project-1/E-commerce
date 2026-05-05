# infra

애플리케이션 실행에 필요한 공통 인프라 자산을 모아둔 디렉토리입니다.

## 하위 디렉토리

- `databases`: MariaDB, Redis, MongoDB, PostgreSQL/pgvector, Qdrant 관련 설치 자료
- `base-images`: 공통 기반 컨테이너 이미지 정의

## 목적

서비스 코드와 분리해서 인프라 설치, 업그레이드, 테스트 스크립트를 관리하기 위한 영역입니다.
