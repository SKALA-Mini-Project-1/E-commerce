# event-consumer

Kafka 토픽에서 이벤트를 소비하는 Python 기반 보조 서비스입니다.

## 포함 내용

- `consumer.py`: 이벤트 소비 로직
- `requirements.txt`: Python 의존성 목록
- `Dockerfile`: 컨테이너 이미지 빌드 정의

## 역할

`order-service`가 Outbox 패턴으로 기록한 이벤트가 Debezium과 Kafka를 거쳐 최종적으로 소비되는 지점을 담당합니다.
