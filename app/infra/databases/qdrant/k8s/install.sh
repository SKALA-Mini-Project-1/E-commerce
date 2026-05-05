#!/bin/bash
# install-qdrant.sh - 간단한 Qdrant Helm 설치 스크립트

set -e
USER_NAME=sk199
NAMESPACE=skala-practice

# 기본 설정
RELEASE_NAME=${USER_NAME}-qdrant
ENVIRONMENT="${1:-dev}"  # 첫 번째 인자 또는 기본값 dev

echo "Qdrant 설치 시작 (환경: ${ENVIRONMENT})"

# 1. Helm repo 추가
echo "[1/4] Helm repository 추가..."
helm repo add qdrant https://qdrant.github.io/qdrant-helm
helm repo update

# 2. Namespace 생성
echo "[2/4] Namespace 생성..."
kubectl create namespace ${NAMESPACE} --dry-run=client -o yaml | kubectl apply -f -

# 3. StorageClass 확인
echo "[3/4] StorageClass 확인..."
if ! kubectl get storageclass efs-sc-shared &> /dev/null; then
    echo "오류: StorageClass 'efs-sc-shared'가 없습니다."
    exit 1
fi

# 4. Helm 설치
echo "[4/4] Qdrant 설치..."
helm upgrade --install ${RELEASE_NAME} qdrant/qdrant \
  --namespace ${NAMESPACE} \
  --values values-${ENVIRONMENT}.yaml \
  --wait

echo ""
echo "✅ 설치 완료!"
echo ""
echo "접속 테스트:"
echo "  kubectl port-forward -n ${NAMESPACE} svc/${RELEASE_NAME} 6333:6333"
echo ""
echo "상태 확인:"
echo "  kubectl get pods -n ${NAMESPACE}"
echo ""
