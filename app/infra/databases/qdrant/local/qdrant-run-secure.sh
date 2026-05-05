#!/bin/bash
# Qdrant Vector Database Docker Container 설정 (API Key 보안 강화 버전)
CONTAINER_NAME="qdrant-vector-db"
IMAGE="qdrant/qdrant:latest"
VOLUME_NAME="qdrant-data"
NETWORK_NAME="kafka-net"  # ← Kafka와 동일한 네트워크
REST_PORT="6333"          # REST API 포트
GRPC_PORT="6334"          # gRPC 포트

# Qdrant API 키 설정 (보안 강화)
QDRANT_API_KEY="Skala25a!23$"

# 기존 컨테이너 확인 및 중지/삭제
if [ "$(docker ps -aq -f name=${CONTAINER_NAME})" ]; then
    echo "기존 컨테이너를 중지하고 삭제합니다..."
    docker stop ${CONTAINER_NAME}
    docker rm ${CONTAINER_NAME}
fi

# 네트워크 존재 확인 (없으면 생성)
if ! docker network inspect ${NETWORK_NAME} &> /dev/null; then
    echo "네트워크 ${NETWORK_NAME}이 없습니다. 생성합니다..."
    docker network create ${NETWORK_NAME}
fi

# 볼륨 생성 (존재하지 않는 경우)
if ! docker volume inspect ${VOLUME_NAME} &> /dev/null; then
    echo "볼륨 ${VOLUME_NAME}을 생성합니다..."
    docker volume create ${VOLUME_NAME}
fi

# Qdrant 설정 파일 생성 (API Key 포함)
cat > /tmp/qdrant-config-secure.yaml << EOF
service:
  # REST API 설정
  http_port: 6333
  # gRPC 설정
  grpc_port: 6334
  # 모든 인터페이스에서 접속 허용
  host: 0.0.0.0
  # API 키 인증 활성화
  api_key: ${QDRANT_API_KEY}

storage:
  # 스토리지 경로
  storage_path: /qdrant/storage
  # 스냅샷 경로
  snapshots_path: /qdrant/snapshots
  # Write-Ahead Log (WAL) 설정
  wal:
    wal_capacity_mb: 32
    wal_segments_ahead: 0

# 클러스터 설정 (단일 노드에서는 비활성화)
cluster:
  enabled: false

# 로그 레벨 설정
log_level: INFO
EOF

# API 키를 파일로 저장 (나중에 참조용)
echo "${QDRANT_API_KEY}" > /tmp/qdrant-api-key.txt
chmod 600 /tmp/qdrant-api-key.txt

echo "Qdrant 컨테이너를 시작합니다..."

# Docker 컨테이너 실행
docker run -d \
  --name ${CONTAINER_NAME} \
  --network ${NETWORK_NAME} \
  -p ${REST_PORT}:6333 \
  -p ${GRPC_PORT}:6334 \
  -e QDRANT__SERVICE__API_KEY="${QDRANT_API_KEY}" \
  -v ${VOLUME_NAME}:/qdrant/storage \
  -v /tmp/qdrant-config-secure.yaml:/qdrant/config/production.yaml:ro \
  ${IMAGE}

# 컨테이너 시작 대기
echo "Qdrant가 시작될 때까지 대기 중..."
sleep 5

# 상태 확인
if docker ps | grep -q ${CONTAINER_NAME}; then
    echo ""
    echo "=========================================="
    echo "Qdrant가 성공적으로 시작되었습니다!"
    echo "=========================================="
    echo ""
    echo "⚠️  중요: API 키가 활성화되었습니다!"
    echo "   API Key: ${QDRANT_API_KEY}"
    echo "   (이 키는 /tmp/qdrant-api-key.txt 에 저장되었습니다)"
    echo ""
    echo "접속 정보:"
    echo "  REST API: http://localhost:${REST_PORT}"
    echo "  gRPC: localhost:${GRPC_PORT}"
    echo "  Dashboard: http://localhost:${REST_PORT}/dashboard"
    echo ""
    echo "API 테스트 (API Key 포함):"
    echo "  Health Check:"
    echo "    curl -H 'api-key: ${QDRANT_API_KEY}' http://localhost:${REST_PORT}/health"
    echo ""
    echo "  컬렉션 목록 조회:"
    echo "    curl -H 'api-key: ${QDRANT_API_KEY}' http://localhost:${REST_PORT}/collections"
    echo ""
    echo "  예제 컬렉션 생성:"
    echo "    curl -X PUT http://localhost:${REST_PORT}/collections/test_collection \\"
    echo "      -H 'api-key: ${QDRANT_API_KEY}' \\"
    echo "      -H 'Content-Type: application/json' \\"
    echo "      -d '{\"vectors\": {\"size\": 384, \"distance\": \"Cosine\"}}'"
    echo ""
    echo "Python 클라이언트 예제 (API Key 사용):"
    echo "  from qdrant_client import QdrantClient"
    echo "  client = QdrantClient("
    echo "      host='localhost',"
    echo "      port=${REST_PORT},"
    echo "      api_key='${QDRANT_API_KEY}'"
    echo "  )"
    echo ""
    echo "Spring AI 설정 (application.yml):"
    echo "  spring:"
    echo "    ai:"
    echo "      vectorstore:"
    echo "        qdrant:"
    echo "          host: localhost"
    echo "          port: ${REST_PORT}"
    echo "          api-key: ${QDRANT_API_KEY}"
    echo "          collection-name: your_collection"
    echo ""
    echo "컨테이너 로그 확인:"
    echo "  docker logs ${CONTAINER_NAME}"
    echo ""
    echo "컨테이너 중지:"
    echo "  docker stop ${CONTAINER_NAME}"
    echo ""
    echo "컨테이너 삭제:"
    echo "  docker rm ${CONTAINER_NAME}"
    echo ""
    echo "볼륨 삭제 (데이터 완전 삭제):"
    echo "  docker volume rm ${VOLUME_NAME}"
    echo ""
else
    echo ""
    echo "오류: Qdrant 시작에 실패했습니다."
    echo "로그 확인: docker logs ${CONTAINER_NAME}"
    exit 1
fi
