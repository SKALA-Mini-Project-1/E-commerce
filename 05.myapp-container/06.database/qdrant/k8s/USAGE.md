# Qdrant 사용 가이드

## 환경 정보

- **Namespace**: qdrant
- **Service**: qdrant
- **Ingress**: https://qdrant.skala25a.project.skala-ai.com
- **API Key**: Skala25a!23$

## 접속 방법

### 1. Ingress 접속 (기본)

```bash
# Health Check
curl -k -H "api-key: Skala25a!23$" https://qdrant.skala25a.project.skala-ai.com/

# Dashboard
https://qdrant.skala25a.project.skala-ai.com/dashboard
```

### 2. Port Forward 접속

```bash
# Port Forward 시작
kubectl port-forward -n qdrant svc/qdrant 6333:6333

# 별도 터미널에서 접속
curl -H "api-key: Skala25a!23$" http://localhost:6333/
```

## 테스트 스크립트

### 자동 테스트 (권장)

```bash
# Ingress 테스트
./qdrant-test.sh -k 'Skala25a!23$'

# Ingress 테스트 + 컬렉션 삭제
./qdrant-test.sh -k 'Skala25a!23$' -d

# Port Forward 테스트
./qdrant-test.sh -p -k 'Skala25a!23$'
```

## 수동 테스트

### 컬렉션 생성

```bash
curl -k -X PUT https://qdrant.skala25a.project.skala-ai.com/collections/test_collection \
  -H 'Content-Type: application/json' \
  -H 'api-key: Skala25a!23$' \
  -d '{
    "vectors": {
      "size": 384,
      "distance": "Cosine"
    }
  }'
```

### 컬렉션 목록 조회

```bash
curl -k -H 'api-key: Skala25a!23$' \
  https://qdrant.skala25a.project.skala-ai.com/collections
```

### 벡터 추가

```bash
# 랜덤 벡터 생성 (384차원)
vector=$(python3 -c "import random; print([random.random() for _ in range(384)])")

# 포인트 추가
curl -k -X PUT https://qdrant.skala25a.project.skala-ai.com/collections/test_collection/points \
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

### 벡터 검색

```bash
# 검색 실행
curl -k -X POST https://qdrant.skala25a.project.skala-ai.com/collections/test_collection/points/search \
  -H 'Content-Type: application/json' \
  -H 'api-key: Skala25a!23$' \
  -d '{
    "vector": ${vector},
    "limit": 5
  }'
```

### 컬렉션 삭제

```bash
curl -k -X DELETE https://qdrant.skala25a.project.skala-ai.com/collections/test_collection \
  -H 'api-key: Skala25a!23$'
```

## Spring Boot 연동

### application.yml

```yaml
spring:
  ai:
    vectorstore:
      qdrant:
        host: qdrant.skala25a.project.skala-ai.com
        port: 443
        use-tls: true
        api-key: Skala25a!23$
        collection-name: my_collection
```

### Java 코드

```java
@Configuration
public class QdrantConfig {
    
    @Bean
    public QdrantClient qdrantClient() {
        return new QdrantClient(
            QdrantGrpcClient.newBuilder(
                "qdrant.skala25a.project.skala-ai.com",
                443,
                true
            )
            .withApiKey("Skala25a!23$")
            .build()
        );
    }
}
```

## Python 연동

```python
from qdrant_client import QdrantClient

# 클라이언트 생성
client = QdrantClient(
    host="qdrant.skala25a.project.skala-ai.com",
    port=443,
    https=True,
    api_key="Skala25a!23$"
)

# 컬렉션 생성
from qdrant_client.models import Distance, VectorParams

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
            "payload": {"text": "Sample"}
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

## 상태 확인

```bash
# Pod 상태
kubectl get pods -n qdrant

# Service 확인
kubectl get svc -n qdrant

# Ingress 확인
kubectl get ingress -n qdrant

# 로그 확인
kubectl logs -f -n qdrant qdrant-0
```
