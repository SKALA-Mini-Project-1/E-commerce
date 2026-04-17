#!/bin/bash
# test-qdrant-grpc.sh

GRPC_HOST="qdrant-grpc.skala25a.project.skala-ai.com:443"
API_KEY='Skala25a!23$'  # API Key가 있는 경우

echo "Qdrant gRPC 서버 테스트: ${GRPC_HOST}"
echo "========================================"

# 1. 서비스 목록 확인
echo ""
echo "1. 사용 가능한 서비스 목록:"
grpcurl -plaintext ${GRPC_HOST} list

# 2. Qdrant 서비스 메서드 확인
echo ""
echo "2. Qdrant 서비스 메서드:"
grpcurl -plaintext ${GRPC_HOST} list qdrant.Qdrant

# 3. Health Check (GetCollections로 대체)
echo ""
echo "3. Health Check (GetCollections):"
grpcurl -plaintext \
  ${GRPC_HOST} \
  qdrant.Collections/List

# API Key가 있는 경우
# grpcurl -plaintext \
#   -H "api-key: ${API_KEY}" \
#   ${GRPC_HOST} \
#   qdrant.Collections/List

echo ""
echo "✅ 테스트 완료!"
