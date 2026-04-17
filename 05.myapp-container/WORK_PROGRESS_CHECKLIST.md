# 작업 진행 체크리스트

## 1. 구조 재정리

- [x] 기존 Spring Boot 샘플을 `services/user-service`로 재배치
- [x] 기존 Vue 프론트를 `frontend/vue-app`으로 재배치
- [x] 기존 정적 프론트를 `frontend/static-web`으로 재배치
- [x] 기존 FastAPI 샘플을 `archive/python-sample`로 재배치
- [x] 기존 DB 관련 자산을 `infra/databases/` 아래로 재배치
- [x] 기존 베이스 이미지 자산을 `infra/base-images/` 아래로 재배치
- [x] 루트 README를 새 디렉토리 구조 기준으로 갱신

## 2. 백엔드 MSA 서비스 골격 생성

- [x] `user-service`를 기준으로 `product-service` 생성
- [x] `user-service`를 기준으로 `order-service` 생성
- [x] `user-service`를 기준으로 `payment-service` 생성
- [x] 각 서비스 README를 현재 역할에 맞게 갱신
- [x] `k8s`, `kustomize`, Docker 관련 파일은 값 변경 없이 유지

## 3. 도메인 코드 분리

### user-service

- [x] 기존 사용자 API 유지
- [x] `user-service`에서 Region 도메인 제거
- [x] 주문 생성 시 사용자 존재 검증용 API 연계 반영

### product-service

- [x] 사용자/지역 도메인 코드 제거
- [x] `Product` 엔티티 추가
- [x] `ProductRepository` 추가
- [x] `ProductService` 추가
- [x] `ProductController` 추가
- [x] 상품 초기 데이터로 `DataInitializer` 교체

### order-service

- [x] 사용자/지역 도메인 코드 제거
- [x] `CustomerOrder` 엔티티 추가
- [x] `OrderItem` 엔티티 추가
- [x] `OrderStatus` 추가
- [x] 주문 생성 요청 DTO 추가
- [x] 주문 상태 변경 DTO 추가
- [x] `CustomerOrderRepository` 추가
- [x] `OrderService` 추가
- [x] `OrderController` 추가
- [x] 주문 초기 데이터로 `DataInitializer` 교체

### payment-service

- [x] 사용자/지역 도메인 코드 제거
- [x] `Payment` 엔티티 추가
- [x] `PaymentMethod` 추가
- [x] `PaymentStatus` 추가
- [x] 결제 생성/취소 DTO 추가
- [x] `PaymentRepository` 추가
- [x] `PaymentService` 추가
- [x] `PaymentController` 추가
- [x] 결제 초기 데이터로 `DataInitializer` 교체

## 4. 빌드 및 의존성

- [x] `product-service`에 MariaDB 드라이버 의존성 추가
- [x] `order-service`에 MariaDB 드라이버 의존성 추가
- [x] `payment-service`에 MariaDB 드라이버 의존성 추가
- [x] `product-service` 테스트 설정 보정
- [x] `order-service` 테스트 설정 보정
- [x] `payment-service` 테스트 설정 보정
- [x] `product-service` `./mvnw test` 통과 확인
- [x] `order-service` `./mvnw test` 통과 확인
- [x] `payment-service` `./mvnw test` 통과 확인

## 5. 문서화

- [x] 현재 API 및 MSA 방향 분석 문서 작성
- [x] 백엔드 MSA 디렉토리 구조 문서 작성
- [x] 현재까지 진행/남은 작업 체크리스트 작성

## 6. 앞으로 남은 작업

### 서비스 정리

- [x] `product-service`, `order-service`, `payment-service`의 패키지명 분리
- [x] `product-service`, `order-service`, `payment-service`의 애플리케이션 클래스명 분리
- [x] 각 서비스의 Maven `artifactId`와 빌드 산출물 이름 정리
- [x] 서비스별 README를 실제 API 스펙 기준으로 상세화

### 데이터베이스

- [x] MariaDB 기준 서비스별 스키마 전략 확정
- [x] `user-service`용 스키마 정의
- [x] `product-service`용 스키마 정의
- [x] `order-service`용 스키마 정의
- [x] `payment-service`용 스키마 정의
- [x] 서비스별 `application-mariadb.yaml` 값 점검
- [x] 서비스별 MariaDB DB 이름 분리(`user_service_db`, `product_service_db`, `order_service_db`, `payment_service_db`)
- [x] 서비스 간 호출용 mariadb 프로파일 base URL 정리
- [x] 결제 DB에 주문/제품 스냅샷 저장 구조 반영

### 서비스 간 연동

- [x] 주문 생성 시 사용자 존재 여부 검증 방식 정의
- [x] 주문 생성 시 상품/재고 확인 방식 정의
- [x] `order-service`에서 `user-service`, `product-service` REST 호출 클라이언트 반영
- [x] 주문 생성 요청을 `userId + productId + quantity` 기준으로 정리
- [x] 주문 생성 시 상품 재고 차감 흐름 반영
- [x] 결제 생성 시 주문 존재 여부 검증 반영
- [x] 결제 생성 시 주문 상태/금액 검증 반영
- [x] 결제 DB에 주문/제품 스냅샷 데이터 저장 반영
- [x] 결제 성공 시 주문 상태 변경 흐름 구현
- [x] 결제 취소 시 주문 상태 보정 흐름 구현
- [x] 결제 취소 시 주문 취소와 상품 재고 복구 보상 흐름 구현
- [x] `payment-service`에서 `order-service` REST 호출 클라이언트 반영
- [x] `payment-service`, `order-service` PATCH 호출 지원 RestTemplate 설정 반영
- [x] 재고 차감 실패 시 재고 복구 보상 로직 반영
- [x] 주문 저장 실패 시 재고 복구 보상 로직 반영
- [x] `product-service`에 재고 복구 API 반영
- [x] 현재 서비스 간 호출 방식을 REST 기준으로 반영
- [x] 결제 생성 시 중복 활성 결제 방지 검증 반영

### API/도메인 고도화

- [x] 제품 DTO와 검증 규칙 고도화
- [x] 주문 DTO와 응답 모델 분리
- [x] 결제 DTO와 응답 모델 분리
- [x] 공통 예외 처리(`@ControllerAdvice`) 추가
- [x] 서비스별 Swagger 문서 정리
- [x] 서비스별 입력값 검증 어노테이션(`@Valid`, Bean Validation) 정리
- [x] 서비스 간 호출 실패 시 에러 코드/메시지 표준화

### Kubernetes / 배포

- [x] 서비스별 Dockerfile 운영형 멀티스테이지 빌드 구조로 정리
- [x] 서비스별 k8s 파일 이름과 위치 정리 방향 확정
- [x] Ingress에서 `user-service`, `product-service`, `order-service`, `payment-service` 라우팅 템플릿 정리
- [x] MariaDB 연결 기준 ConfigMap/Secret 템플릿 정리
- [ ] 로컬/개발/운영 프로파일별 배포 흐름 정리
- [x] 서비스별 Deployment/Service가 분리된 DB 스키마 환경변수를 참조하도록 매핑 정리
- [x] Ingress 및 Service DNS 기준 서비스 간 내부 호출 주소 템플릿 정리

### 프론트 연동

- [ ] `frontend/vue-app`에서 제품 목록 조회 화면 추가
- [ ] 주문 생성 화면 추가
- [ ] 결제 요청/상태 확인 화면 추가
- [ ] 사용자 서비스와 주문/결제 흐름 연결

### 품질

- [ ] 컨트롤러/서비스 단위 테스트 확장
- [ ] API 예시 요청/응답 문서화
- [ ] 서비스별 실행 방법 문서화
- [ ] 주문 생성부터 결제 승인/취소까지 통합 시나리오 테스트 추가
- [ ] 재고 차감 실패 및 보상 처리 시나리오 테스트 추가
- [x] 실제 MariaDB 기반 로컬 실행 검증

### 운영 보완

- [ ] 서비스별 MariaDB 초기 스키마 SQL 또는 마이그레이션 도구(Flyway/Liquibase) 도입
- [ ] 결제 승인 후 주문 상태 변경 실패에 대한 재처리 전략 정리
- [ ] 재고 복구 호출 실패에 대한 재시도 또는 보상 이벤트 저장 전략 정리
- [ ] `user-service` 테스트의 Mockito inline mock-maker 설정 보정 또는 테스트 의존성 재구성
- [ ] 서비스 간 호출 타임아웃/재시도/서킷브레이커 정책 반영
