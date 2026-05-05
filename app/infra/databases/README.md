# databases

실습에 사용되는 데이터 저장소별 설치 스크립트와 설정 파일을 모아둔 디렉토리입니다.

## 하위 디렉토리

- `mariadb`: 주요 애플리케이션 데이터 저장소
- `redis`: 캐시 또는 세션 저장소 실험용 자산
- `mongodb`: 문서형 저장소 실험용 자산
- `postgresql-pgvector`: 벡터 확장 포함 PostgreSQL 실험 자산
- `qdrant`: 벡터 데이터베이스 실험 자산

## 공통 패턴

대부분의 디렉토리는 `install.sh`, `upgrade.sh`, `rollback.sh`, `uninstall.sh` 같은 운영 스크립트와 `custom-values.yaml`을 포함합니다.
