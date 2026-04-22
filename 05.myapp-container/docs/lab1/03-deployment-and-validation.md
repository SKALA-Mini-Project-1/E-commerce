# Lab 1 Deployment And Validation

## 적용 흐름

실습 1은 아래 흐름으로 진행했다.

1. Namespace / ConfigMap / Secret / DB 리소스 배포
2. 서비스별 Deployment / Service 배포
3. Ingress 배포 및 외부 접근 확인
4. HPA / KEDA 적용 및 scale-out 검증
5. 운영 이슈 발생 시 rollback / pause 를 통한 안정화

## 배포 후 확인한 핵심 상태

최종 안정화 시점 기준 확인 내용은 아래와 같다.

- 핵심 Pod 들이 모두 `Running`
- `ImagePullBackOff` 상태 제거
- 문제였던 새 ReplicaSet 은 `DESIRED 0`
- `ScaledObject` 는 유지하되 `PAUSED=True`
- Ingress 는 동일 host 아래 정상 생성

예시 최종 안정 상태:

```text
frontend-v1-7c7f9bf664-hkmbt       1/1 Running
mariadb-8f448f5dc-cpbwf            1/1 Running
order-service-7bd49ddfff-wwvfb     1/1 Running
payment-service-5596cd4d7f-6c5rx   1/1 Running
product-service-699d46cf59-kqhc5   1/1 Running
user-service-6b584c8695-x2jcb      1/1 Running
```

ReplicaSet 최종 상태 예시:

```text
payment-service-5596cd4d7f   1 1 1
payment-service-7558b6fc79   0 0 0
product-service-699d46cf59   1 1 1
product-service-787d79865    0 0 0
```

## 외부 접근 검증

Ingress 확인 결과:

```text
ecommerce-api-ingress      nginx   skala3-cloud1-team4.cloud.skala-ai.com
frontend-web-ingress       nginx   skala3-cloud1-team4.cloud.skala-ai.com
user-swagger-ingress       nginx   skala3-cloud1-team4.cloud.skala-ai.com
product-swagger-ingress    nginx   skala3-cloud1-team4.cloud.skala-ai.com
order-swagger-ingress      nginx   skala3-cloud1-team4.cloud.skala-ai.com
payment-swagger-ingress    nginx   skala3-cloud1-team4.cloud.skala-ai.com
```

또한 `/api/users` 호출 시 샘플 사용자 데이터가 정상 반환되는 것도 확인했다.
이는 서비스 연결과 외부 접근이 함께 동작했다는 증거로 사용했다.

## 오토스케일링 검증

실습 중 `ScaledObject`, `HPA`, `Prometheus`, `KEDA` 를 실제로 연결해 scale-out 을 검증했다.

중간 확인 결과:

- `external.metrics.k8s.io` 복구 성공
- KEDA operator / metrics apiserver `Running`
- Prometheus `Running`
- HPA target 값 확인 가능
- 실제 replica 증가 시도 확인

다만 운영 중 아래 이슈가 있었다.

- 클러스터 CPU 부족으로 일부 Pod `Pending`
- 신규 이미지 pull `EOF`
- unstable revision 남아 rollout 이 길어짐

이 때문에 최종 제출 상태는 autoscaling 을 pause 한 안정 버전으로 정리했다.

## 최종 운영 판단

실습 1은 "오토스케일링을 적용하지 않았다"가 아니라,
"적용하고 검증한 뒤, 제출 시점에는 안정 운영 상태로 정리했다"로 설명하는 것이 맞다.

즉 최종 상태는 아래 의미를 가진다.

- autoscaling 리소스는 유지
- 현재는 `paused-replicas=1` 로 고정
- 실험 결과와 운영 안정성을 모두 문서로 남김
