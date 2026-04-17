# Qdrant Docker 실행 스크립트 가이드

## 개요
Qdrant는 벡터 데이터베이스로, RAG(Retrieval-Augmented Generation) 시스템이나 시맨틱 검색에 사용됩니다.
이 스크립트는 Qdrant를 로컬 Docker 환경에서 쉽게 실행할 수 있도록 합니다.

## 파일 구성

### 1. `qdrant-run.sh` (기본 버전)
- API 키 없이 실행되는 버전
- 개발/테스트 환경에 적합
- 빠른 프로토타이핑에 유용

### 2. `qdrant-run-secure.sh` (보안 강화 버전)
- 자동 생성된 API 키로 보호
- 프로덕션 환경에 적합
- 무단 접근 방지

## 실행 방법

### 기본 버전 실행
```bash
./qdrant-run.sh
```

### 보안 강화 버전 실행
```bash
./qdrant-run-secure.sh
```

## 주요 설정

### 포트
- **6333**: REST API (HTTP)
- **6334**: gRPC

### 볼륨
- **qdrant-data**: 벡터 데이터 영구 저장

### 네트워크
- **kafka-net**: Kafka 및 다른 서비스와 통신

## 주요 기능

### 1. Health Check
```bash
# 기본 버전
curl http://localhost:6333/health

# 보안 버전
curl -H 'api-key: YOUR_API_KEY' http://localhost:6333/health
```

### 2. 컬렉션 생성
```bash
curl -X PUT http://localhost:6333/collections/my_collection \
  -H 'Content-Type: application/json' \
  -H 'api-key: Skala25a!23$' \
  -d '{
    "vectors": {
      "size": 384,
      "distance": "Cosine"
    }
  }'
```

### 3. 벡터 추가
```bash
# Python으로 384차원 벡터 생성 예시
vector=$(python3 -c "import random; print([random.random() for _ in range(384)])")
# 384차원 벡터 예시 (실제 벡터 크기에 맞춰 조정 필요)
curl -X PUT http://localhost:6333/collections/my_collection/points \
  -H 'Content-Type: application/json' \
  -H 'api-key: Skala25a!23$' \
  -d '{
    "points": [
      {
        "id": 1,
        "vector": ${vector},
        "payload": {"text": "Sample text"}
      }
    ]
  }'

```

### 4. 벡터 검색
```bash
curl -X POST http://localhost:6333/collections/my_collection/points/search \
  -H 'Content-Type: application/json' \
  -H 'api-key: Skala25a!23$' \
  -d '{
    "vector": ${vector},
    "limit": 5
  }'
```

## Spring AI 통합

### application.yml 설정

#### 기본 버전
```yaml
spring:
  ai:
    vectorstore:
      qdrant:
        host: localhost
        port: 6333
        collection-name: my_collection
```

#### 보안 버전
```yaml
spring:
  ai:
    vectorstore:
      qdrant:
        host: localhost
        port: 6333
        api-key: ${QDRANT_API_KEY}
        collection-name: my_collection
```

### Java 코드 예제
```java
@Configuration
public class VectorStoreConfig {
    
    @Bean
    public QdrantVectorStore vectorStore(
            EmbeddingModel embeddingModel,
            @Value("${spring.ai.vectorstore.qdrant.host}") String host,
            @Value("${spring.ai.vectorstore.qdrant.port}") int port,
            @Value("${spring.ai.vectorstore.qdrant.api-key:}") String apiKey) {
        
        QdrantClient client = new QdrantClient(
            QdrantGrpcClient.newBuilder(host, port, false)
                .withApiKey(apiKey)
                .build()
        );
        
        return new QdrantVectorStore(client, embeddingModel);
    }
}
```

## Python 클라이언트 사용

### 설치
```bash
pip install qdrant-client
```

### 기본 사용
```python
from qdrant_client import QdrantClient
from qdrant_client.models import Distance, VectorParams

# 클라이언트 생성
client = QdrantClient(host="localhost", port=6333)

# 컬렉션 생성
client.create_collection(
    collection_name="my_collection",
    vectors_config=VectorParams(size=384, distance=Distance.COSINE)
)

# 벡터 추가
client.upsert(
    collection_name="my_collection",
    points=[
        {
            "id": 1,
            "vector": [0.1] * 384,
            "payload": {"text": "Sample text"}
        }
    ]
)

# 검색
results = client.search(
    collection_name="my_collection",
    query_vector=[0.1] * 384,
    limit=5
)
```

### API Key 사용 (보안 버전)
```python
client = QdrantClient(
    host="localhost",
    port=6333,
    api_key="your-api-key-here"
)
```

## 모니터링

### 웹 대시보드
브라우저에서 접속:
```
http://localhost:6333/dashboard
```

### 로그 확인
```bash
docker logs qdrant-vector-db
```

### 실시간 로그
```bash
docker logs -f qdrant-vector-db
```

## 관리 명령어

### 컨테이너 중지
```bash
docker stop qdrant-vector-db
```

### 컨테이너 재시작
```bash
docker restart qdrant-vector-db
```

### 컨테이너 삭제
```bash
docker rm qdrant-vector-db
```

### 데이터 백업
```bash
docker run --rm \
  -v qdrant-data:/source \
  -v $(pwd)/backup:/backup \
  alpine tar czf /backup/qdrant-backup-$(date +%Y%m%d).tar.gz -C /source .
```

### 데이터 복원
```bash
docker run --rm \
  -v qdrant-data:/target \
  -v $(pwd)/backup:/backup \
  alpine tar xzf /backup/qdrant-backup-YYYYMMDD.tar.gz -C /target
```

### 볼륨 삭제 (주의: 모든 데이터 삭제)
```bash
docker volume rm qdrant-data
```

## 벡터 크기 가이드

일반적인 임베딩 모델별 벡터 크기:
- **OpenAI text-embedding-ada-002**: 1536
- **OpenAI text-embedding-3-small**: 1536
- **OpenAI text-embedding-3-large**: 3072
- **Sentence-BERT (all-MiniLM-L6-v2)**: 384
- **Sentence-BERT (all-mpnet-base-v2)**: 768
- **Cohere embed-english-v3.0**: 1024

## 보안 고려사항

1. **API Key 관리**
   - 프로덕션에서는 반드시 API Key 사용
   - 환경변수나 Secret Manager로 관리
   - 주기적으로 키 변경

2. **네트워크 격리**
   - 필요한 서비스만 같은 네트워크에 배치
   - 외부 노출이 필요하면 리버스 프록시 사용

3. **데이터 암호화**
   - 민감한 데이터는 암호화 후 저장
   - 전송 시 TLS/SSL 사용

## 문제 해결

### 컨테이너가 시작되지 않는 경우
```bash
# 로그 확인
docker logs qdrant-vector-db

# 포트 충돌 확인
lsof -i :6333
lsof -i :6334
```

### 메모리 부족
```bash
# 메모리 사용량 확인
docker stats qdrant-vector-db

# 컨테이너 재시작
docker restart qdrant-vector-db
```

### 데이터가 저장되지 않는 경우
```bash
# 볼륨 확인
docker volume inspect qdrant-data

# 권한 확인
docker exec qdrant-vector-db ls -la /qdrant/storage
```

## 참고 자료

- [Qdrant 공식 문서](https://qdrant.tech/documentation/)
- [Qdrant Python 클라이언트](https://github.com/qdrant/qdrant-client)
- [Spring AI Qdrant](https://docs.spring.io/spring-ai/reference/api/vectordbs/qdrant.html)
- [Vector Database 비교](https://qdrant.tech/benchmarks/)

## 활용 사례

1. **RAG 시스템**: LLM과 결합하여 정확한 정보 제공
2. **시맨틱 검색**: 의미 기반 문서 검색
3. **추천 시스템**: 유사 아이템 추천
4. **이미지 검색**: 비전 모델과 결합
5. **이상 탐지**: 패턴 인식 및 이상 감지
