# Lab 2 Folder Structure And Locations

실습 2는 코드, 운영 설정, CDC 설정, 제출 문서를 분리해서 관리한다.

## 어디에 무엇을 두는가

- 애플리케이션 코드
  - `/Users/yangyewon/workspace/shopping-mall-k8s-lab/E-commerce/05.myapp-container/services/order-service`
  - Outbox 관련 엔티티, 레포지토리, 서비스 로직은 여기서 관리

- 소비자 코드
  - 기존 서비스를 재사용하면 해당 서비스 내부에 구현
  - 별도 실습용 consumer는 `/Users/yangyewon/workspace/shopping-mall-k8s-lab/E-commerce/05.myapp-container/services/event-consumer` 에 둔다

- CDC / Connector 설정
  - `/Users/yangyewon/workspace/shopping-mall-k8s-lab/E-commerce/cdc`
  - Debezium connector JSON, 등록 스크립트, 확인 명령을 관리

- Kubernetes 매니페스트
  - `/Users/yangyewon/workspace/shopping-mall-k8s-lab/E-commerce/k8s`
  - 실습 2에서 추가되는 Kafka, Connect, CDC 관련 리소스는 필요 시 하위 폴더로 분리

- 문서
  - `/Users/yangyewon/workspace/shopping-mall-k8s-lab/E-commerce/05.myapp-container/docs/lab2`
  - 시나리오, 설계, 구현 계획, 결과를 문서화

## 현재 기준 추천 구조

```text
E-commerce/
├── 05.myapp-container/
│   ├── services/
│   │   ├── order-service/
│   │   ├── payment-service/
│   │   ├── product-service/
│   │   ├── user-service/
│   │   └── event-consumer/
│   └── docs/
│       └── lab2/
│           ├── README.md
│           ├── 00-structure-and-locations.md
│           ├── 01-scenario.md
│           ├── 02-architecture.md
│           ├── 03-implementation-plan.md
│           ├── 04-environment-check.md
│           └── 05-results-and-evidence.md
├── cdc/
│   ├── connectors/
│   ├── scripts/
│   └── README.md
└── k8s/
    ├── kafka/                        # 필요 시 추가
    ├── connect/                      # 필요 시 추가
    └── cdc/                          # 필요 시 추가
```

## 문서와 구현 연결 원칙

- 시나리오 문서는 "왜 하는가"를 설명한다.
- 아키텍처 문서는 "어떻게 연결되는가"를 설명한다.
- 구현 계획 문서는 "오늘 어디까지 할 것인가"를 제한한다.
- 결과 문서는 "무엇을 확인했고 어떤 한계가 있었는가"를 남긴다.
- 환경 체크 문서는 "구현 전에 무엇을 검증했고 어떤 판단을 했는가"를 남긴다.

## 시작 순서

1. `01-scenario.md` 작성
2. `02-architecture.md` 작성
3. `03-implementation-plan.md` 작성
4. `04-environment-check.md` 작성
5. `order-service`에 Outbox 뼈대 추가
6. `cdc/connectors`에 connector 설정 추가
7. 결과 확인 후 `05-results-and-evidence.md` 정리
