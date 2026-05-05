# keda

KEDA 오토스케일링 동작을 로컬에서 관찰하고 재현하기 위한 스크립트 디렉토리입니다.

## 포함 파일

- `load-memory.sh`: 메모리 부하 유도
- `load-traffic-hey.sh`: HTTP 트래픽 부하 유도
- `start-local-observability.sh`: 로컬 관측 도구 실행
- `stop-local-observability.sh`: 관측 도구 종료
- `start-local-service-access.sh`: 서비스 접근용 포트 포워딩/프록시 실행
- `stop-local-service-access.sh`: 접근용 보조 프로세스 종료

## 목적

KEDA 스케일 아웃/인 조건을 재현하고 Prometheus/Grafana 지표를 함께 확인하기 위한 보조 도구 모음입니다.
