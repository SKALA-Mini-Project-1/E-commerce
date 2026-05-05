# k8s/monitoring

모니터링 연동에 필요한 Kubernetes 자산을 모아둔 디렉토리입니다.

## 포함 내용

- `servicemonitor-*.yaml`: Prometheus 수집 대상 정의
- `grafana-dashboard-keda-scaling.json`: KEDA 스케일링 관찰용 대시보드
- `.disabled` 파일: 실험 중 비활성화한 대체 설정

## 역할

오토스케일링, 요청량, 메트릭 수집 상태를 Prometheus와 Grafana에서 확인할 수 있게 해줍니다.
