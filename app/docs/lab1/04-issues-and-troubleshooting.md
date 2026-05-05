# Lab 1 Issues And Troubleshooting

## 1. CPU 부족으로 인한 Pending Pod

문제:

- autoscaling 적용 후 `payment-service`, `product-service` Pod 일부가 `Pending`
- `kubectl describe pod` 결과 `Insufficient cpu` 확인

해결:

- 서비스별 `requests` 와 `replicas`, `maxReplicaCount` 를 조정
- 필요 시 scale-out 범위를 축소
- 최종적으로는 autoscaling pause 로 안정화

의미:

- HPA/KEDA 가 실제로 scale-out 을 시도했음을 확인했다.
- 동시에 클러스터 자원 한계도 운영 이슈로 문서화할 수 있었다.

## 2. KEDA external metrics 문제

문제:

- `external.metrics.k8s.io` 가 `MissingEndpoints`
- HPA target 이 `<unknown>`
- KEDA / Prometheus Pod 일부 `Pending`

해결:

- KEDA values 를 낮은 리소스 기준으로 조정
- Prometheus / KEDA Pod 가 실제로 `Running` 하도록 복구
- 이후 `external.metrics.k8s.io = True` 확인

의미:

- 단순 리소스 생성이 아니라 external metrics 기반 autoscaling 복구 과정을 경험했다.

## 3. 사설 레지스트리 이미지 pull 실패

문제:

- `payment-service`, `product-service` 신규 Pod 에서 `ImagePullBackOff`
- 레지스트리 요청 시 `EOF`
- 로컬 `docker pull` 도 동일 증상

해결:

- 문제를 Kubernetes 설정이 아니라 registry/image 접근 문제로 판단
- rollout undo 와 안정 버전 유지 전략 사용
- 새 revision 대신 정상 동작 중인 기존 Pod 기준으로 서비스 안정화

의미:

- 운영 실습 관점에서 "항상 최신 revision 유지"보다 "안정 버전 유지"가 더 중요할 수 있음을 보여줬다.

## 4. rollout 이 끝나지 않는 문제

문제:

- `old replicas are pending termination`
- 신규 Pod 가 준비되지 못해 rollout 이 길어짐

해결:

- 원인 Pod 상태를 `describe` 로 확인
- image pull 이슈와 연결해 rollback / recreate / pause 판단

## 5. 최종 안정화 전략

실습 1 마지막에는 아래 원칙으로 정리했다.

- `ScaledObject` 삭제 대신 pause 사용
- replica 를 1 기준으로 안정화
- 핵심 서비스가 모두 `Running` 인 상태 확보
- 불안정한 revision RS 는 `DESIRED 0` 으로 정리

이 전략 덕분에 실습 2로 넘어가기 전 안정된 Kubernetes 상태를 확보할 수 있었다.
